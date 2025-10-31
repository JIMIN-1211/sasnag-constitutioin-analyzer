const express = require('express');
const router = express.Router();
const { pool } = require('../db/pool');
const { requireAuth } = require('../middleware/authn');

// 유효한 식사 유형 목록을 정의.
const VALID_MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK'];

// master_foods 테이블에서 100g당 칼로리 밀도를 조회하는 함수
async function getCalorieDensity(foodId, connection) {
    // foodId가 유효한지 확인하고 100g당 칼로리 정보를 가져옴.
    const [rows] = await connection.query(
        'SELECT cal_per_gram FROM master_foods WHERE id = ?',
        [foodId]
    );
    if (rows.length === 0) {
        // master_foods에 해당 ID가 없으면 오류 처리
        throw new Error(`Food ID ${foodId} is invalid or not found.`);
    }
    return rows[0].cal_per_gram;
}

// health_records에 해당 날짜의 기록이 있는지 확인하고 ID를 반환하거나 새로 생성.
async function upsertHealthRecord(userId, targetDate, connection) {
    // targetDate는 'YYYY-MM-DD' 형식이어야 .
    const [existingRows] = await connection.query(
        'SELECT id FROM health_records WHERE user_id = ? AND recorded_at = ?',
        [userId, targetDate]
    );

    if (existingRows.length > 0) {
        return existingRows[0].id;
    }

    // 기록이 없으면 새로 생성 (intake_calories, exercise_calories 초기화)
    const [result] = await connection.query(
        'INSERT INTO health_records (user_id, recorded_at, intake_calories, exercise_calories, sleep_duration_hours) VALUES (?, ?, 0, 0, NULL)', 
        [userId, targetDate]
    );
    return result.insertId;
}

// health_records ID에 해당하는 날짜의 meal_logs를 합산하여 총 섭취 칼로리를 계산.
async function calculateTotalCalories(recordId, connection) {
    // 1. health_records ID로부터 user_id와 날짜를 가져옴.
    const [recordInfo] = await connection.query(
        'SELECT user_id, recorded_at FROM health_records WHERE id = ?',
        [recordId]
    );
    
    if (recordInfo.length === 0) return 0;

    const { user_id, recorded_at } = recordInfo[0];
    let targetDateString = recorded_at;
    if (recorded_at instanceof Date) {
        const year = recorded_at.getFullYear();
        const month = String(recorded_at.getMonth() + 1).padStart(2, '0');
        const day = String(recorded_at.getDate()).padStart(2, '0');
        targetDateString = `${year}-${month}-${day}`;
    }
    
    // 2. meal_logs 테이블에서 해당 사용자/날짜의 calories_consumed_session을 합산.
    const [totalRows] = await connection.query(
        `SELECT SUM(calories_consumed_session) AS total_calories 
         FROM meal_logs 
         WHERE user_id = ? AND DATE(recorded_at) = ?`,
        [user_id, targetDateString] // recorded_at는 health_records에서 DATE 타입.
    );
    
    return totalRows[0].total_calories || 0;
}

// --- 라우터 정의 ---

// POST /v1/meal : 식단 상세 기록 추가 (meal_type 포함)
router.post('/', requireAuth, async (req, res) => {
    const connection = await pool.getConnection();
    try {
        const userId = req.user.id;
        // food_id, intake_gram, meal_type을 받음.
        const { targetDate, food_id, intake_gram, meal_type } = req.body; 
        
        // 필수 값 및 meal_type 유효성 검증
        if (!targetDate || !food_id || intake_gram === undefined || typeof intake_gram !== 'number' || intake_gram <= 0 || !meal_type) {
            return res.status(400).json({ message: "targetDate, food_id, intake_gram, meal_type은 필수 항목이며, intake_gram은 0보다 커야 ." });
        }
        if (!VALID_MEAL_TYPES.includes(meal_type.toUpperCase())) {
            return res.status(400).json({ message: `유효하지 않은 식사 유형. 유효한 유형: ${VALID_MEAL_TYPES.join(', ')}` });
        }

        await connection.beginTransaction();

        // 1. master_foods에서 칼로리 밀도를 조회하여 session 칼로리를 계산.
        const caloriesPerGram = await getCalorieDensity(food_id, connection);
        // 칼로리 계산: (섭취량 / 100) * 100g당 칼로리
        const caloriesConsumedSession = intake_gram * caloriesPerGram;

        // 2. health_records ID 가져오기 (총 칼로리 업데이트를 위해 필요)
        const recordId = await upsertHealthRecord(userId, targetDate, connection); 
        console.log("recordID: ",recordId);

        // 3. meal_logs에 상세 식단 기록을 추가. (meal_type 컬럼에 값 저장)
        await connection.query(
            'INSERT INTO meal_logs (user_id, food_id, intake_gram, calories_consumed_session, meal_type) VALUES (?, ?, ?, ?, ?)',
            [userId, food_id, intake_gram, caloriesConsumedSession, meal_type.toUpperCase()]
        );
        const newMealId = (await connection.query('SELECT LAST_INSERT_ID() as id'))[0][0].id;

        // 4. 해당 날짜의 총 칼로리를 다시 계산.
        const totalCalories = await calculateTotalCalories(recordId, connection);
        console.log(totalCalories);
        console.log(recordId);

        // 5. health_records의 총 섭취 칼로리를 업데이트.
        await connection.query(
            'UPDATE health_records SET intake_calories = ? WHERE id = ?',
            [totalCalories, recordId]
        );

        await connection.commit();

        res.json({ 
            message: "식단 기록 및 칼로리 업데이트 성공", 
            meal_log_id: newMealId, 
            meal_type: meal_type.toUpperCase(),
            total_intake_calories: totalCalories
        });

    } catch (error) {
        await connection.rollback();
        console.error('식단 기록 처리 오류:', error);
        if (error.message.includes('Food ID') || error.message.includes('master_foods')) {
            return res.status(404).json({ message: error.message });
        }
        res.status(500).json({ message: "식단 기록 중 서버 오류가 발생했습니다." });
    } finally {
        connection.release();
    }
});


// GET /v1/meal : 특정 날짜의 식단 기록 조회 (meal_type 포함하여 반환 및 필터링 지원)
router.get('/', requireAuth, async (req, res) => {
    try {
        const userId = req.user.id;
        const targetDate = req.query.date || new Date().toISOString().split('T')[0]; // 'YYYY-MM-DD' 형식
        const mealTypeFilter = req.query.meal_type ? req.query.meal_type.toUpperCase() : null; // 필터링할 식사 유형

        // 1. health_records 조회 (해당 날짜의 총 칼로리 및 기록 ID 확인)
        const [healthRecordRows] = await pool.query(
            'SELECT id, intake_calories FROM health_records WHERE user_id = ? AND recorded_at = ?',
            [userId, targetDate]
        );
        
        const total_intake_calories = healthRecordRows.length > 0 ? (healthRecordRows[0].intake_calories || 0) : 0;

        // 2. meal_logs 조회 쿼리 구성 (meal_type 필터링 조건 추가)
        let mealListQuery = `
            SELECT ml.id, ml.food_id, mf.food_name AS food_name, ml.intake_gram, ml.calories_consumed_session, ml.recorded_at, ml.meal_type
            FROM meal_logs ml
            JOIN master_foods mf ON ml.food_id = mf.id
            WHERE ml.user_id = ? 
              AND DATE(ml.recorded_at) = ?
        `;
        const queryParams = [userId, targetDate];

        if (mealTypeFilter && VALID_MEAL_TYPES.includes(mealTypeFilter)) {
            // 유효한 meal_type 필터가 있을 경우 WHERE 절에 추가
            mealListQuery += ` AND ml.meal_type = ?`;
            queryParams.push(mealTypeFilter);
        }

        mealListQuery += ` ORDER BY ml.recorded_at ASC`;

        const [mealListRows] = await pool.query(mealListQuery, queryParams);

        // 반환 구조: meal_type이 추가됩니다.
        res.json({
            message: "식단 기록 조회 성공",
            target_date: targetDate,
            total_intake_calories: total_intake_calories,
            meal_list: mealListRows // meal_type 정보 포함
        });

    } catch (error) {
        console.error('식단 기록 조회 오류:', error);
        res.status(500).json({ message: "식단 기록 조회 중 서버 오류가 발생했습니다." });
    }
});


// DELETE /v1/meal/:mealId : 식단 상세 기록 삭제
router.delete('/:mealId', requireAuth, async (req, res) => {
    const connection = await pool.getConnection();
    try {
        const userId = req.user.id;
        const mealId = req.params.mealId;

        await connection.beginTransaction();

        // 1. 삭제하려는 meal_log가 사용자 소유인지 확인하고, health_record ID를 가져옴.
        const [mealCheckRows] = await connection.query(
            `SELECT ml.id, hr.id AS record_id 
             FROM meal_logs ml
             JOIN health_records hr ON ml.user_id = hr.user_id 
             WHERE ml.id = ? 
               AND ml.user_id = ? 
               AND DATE(ml.recorded_at) = hr.recorded_at`, 
            [mealId, userId]
        );

        if (mealCheckRows.length === 0) {
            await connection.rollback();
            return res.status(404).json({ message: "삭제할 식단 기록을 찾을 수 없거나 권한이 없습니다." });
        }

        const recordId = mealCheckRows[0].record_id;

        // 2. meal_logs에서 해당 기록 삭제
        await connection.query('DELETE FROM meal_logs WHERE id = ? AND user_id = ?', [mealId, userId]); 

        // 3. 해당 health_record ID를 기반으로 총 칼로리 재계산 (날짜 기반 쿼리 사용)
        const totalCalories = await calculateTotalCalories(recordId, connection);

        // 4. health_records의 총 섭취 칼로리를 업데이트.
        await connection.query(
            'UPDATE health_records SET intake_calories = ? WHERE id = ?',
            [totalCalories, recordId]
        );

        await connection.commit();

        res.json({ 
            message: "식단 기록 삭제 및 칼로리 업데이트 성공",
            deleted_meal_id: mealId,
            total_intake_calories: totalCalories
        });

    } catch (error) {
        await connection.rollback();
        console.error('식단 기록 삭제 오류:', error);
        res.status(500).json({ message: "식단 기록 삭제 중 서버 오류가 발생했습니다." });
    } finally {
        connection.release();
    }
});


module.exports = router;
