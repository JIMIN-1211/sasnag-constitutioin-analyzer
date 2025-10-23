const express = require('express');
const router = express.Router();
const { pool } = require('../db/pool');
const { requireAuth } = require('../middleware/authn');


async function getCaloriePerMinuteRate(exerciseId, connection) {
    // exerciseId가 유효한지 확인하고 분당 칼로리 정보를 가져옴. (master_exercise_types 테이블 사용)
    const [rows] = await connection.query(
        'SELECT cal_per_min FROM master_exercise_types WHERE id = ?',
        [exerciseId]
    );
    if (rows.length === 0) {
        // master_exercise_types에 해당 ID가 없으면 오류 처리
        throw new Error(`Exercise ID ${exerciseId} is invalid or not found in master_exercise_types.`);
    }
    // 분당 소모 칼로리를 반환
    return rows[0].cal_per_min;
}

async function upsertHealthRecord(userId, targetDate, connection) {
    // targetDate는 'YYYY-MM-DD' 형식이어야 합니다.
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

async function calculateTotalExerciseCalories(recordId, connection) {
    // 1. health_records ID로부터 user_id와 날짜를 가져옴.
    const [recordInfo] = await connection.query(
        'SELECT user_id, recorded_at FROM health_records WHERE id = ?',
        [recordId]
    );
    
    if (recordInfo.length === 0) return 0;

    const { user_id, recorded_at } = recordInfo[0];
    
    // 2. exercise_logs 테이블에서 해당 사용자/날짜의 calories_burned_session을 합산.
    const [totalRows] = await connection.query(
        `SELECT SUM(calories_burned_session) AS total_calories 
         FROM exercise_logs 
         WHERE user_id = ? AND DATE(recorded_at) = ?`,
        [user_id, recorded_at] // recorded_at는 health_records에서 DATE 타입.
    );
    
    // 합산 결과를 반환 (null이면 0 반환)
    return totalRows[0].total_calories || 0;
}

// --- 라우터 정의 ---

// POST /v1/exercise : 운동 상세 기록 추가
router.post('/', requireAuth, async (req, res) => {
    const connection = await pool.getConnection();
    try {
        const userId = req.user.id;
        // exercise_id, duration_minutes, targetDate를 받음.
        const { targetDate, exercise_id, duration_minutes } = req.body; 
        
        // 필수 값 및 유효성 검증
        if (!targetDate || !exercise_id || duration_minutes === undefined || typeof duration_minutes !== 'number' || duration_minutes <= 0) {
            return res.status(400).json({ message: "targetDate, exercise_id, duration_minutes는 필수 항목이며, duration_minutes는 0보다 커야 합니다." });
        }

        await connection.beginTransaction();

        // 1. master_exercise_types에서 분당 칼로리 소모율을 조회.
        const calPerMin = await getCaloriePerMinuteRate(exercise_id, connection);
        
        // 2. 칼로리 계산: 운동 시간(분) * 분당 칼로리
        const caloriesBurnedSession = duration_minutes * calPerMin; 

        // 3. health_records ID 가져오기 (총 칼로리 업데이트를 위해 필요)
        const recordId = await upsertHealthRecord(userId, targetDate, connection); 

        // 4. exercise_logs에 상세 운동 기록을 추가.
        // recorded_at은 현재 시각으로 자동 기록됩니다.
        await connection.query(
            'INSERT INTO exercise_logs (user_id, exercise_type_id, duration_min, calories_burned_session) VALUES (?, ?, ?, ?)',
            [userId, exercise_id, duration_minutes, caloriesBurnedSession]
        );
        // 삽입된 운동 기록의 고유 ID를 확보
        const [result] = await connection.query('SELECT LAST_INSERT_ID() as id');
        const newWorkoutId = result[0].id;


        // 5. 해당 날짜의 총 소모 칼로리를 다시 계산.
        const totalCalories = await calculateTotalExerciseCalories(recordId, connection);

        // 6. health_records의 총 운동 소모 칼로리(exercise_calories)를 업데이트.
        await connection.query(
            'UPDATE health_records SET exercise_calories = ? WHERE id = ?',
            [totalCalories, recordId]
        );

        await connection.commit();

        res.json({ 
            message: "운동 기록 및 칼로리 업데이트 성공", 
            workout_log_id: newWorkoutId, // 프론트엔드에서 삭제 시 사용될 고유 ID
            calculated_burned_calories: caloriesBurnedSession,
            total_exercise_calories: totalCalories
        });

    } catch (error) {
        await connection.rollback();
        console.error('운동 기록 처리 오류:', error);
        // master_exercise_types 조회 오류 포함
        if (error.message.includes('Exercise ID') || error.message.includes('master_exercise_types')) {
            return res.status(404).json({ message: error.message });
        }
        res.status(500).json({ message: "운동 기록 중 서버 오류가 발생했습니다." });
    } finally {
        connection.release();
    }
});


// GET /v1/exercise : 특정 날짜의 운동 기록 조회
router.get('/', requireAuth, async (req, res) => {
    try {
        const userId = req.user.id;
        const targetDate = req.query.date || new Date().toISOString().split('T')[0]; // 'YYYY-MM-DD' 형식

        // 1. health_records 조회 (해당 날짜의 총 운동 소모 칼로리 및 기록 ID 확인)
        const [healthRecordRows] = await pool.query(
            'SELECT id, exercise_calories FROM health_records WHERE user_id = ? AND recorded_at = ?',
            [userId, targetDate]
        );
        
        const total_exercise_calories = healthRecordRows.length > 0 ? (healthRecordRows[0].exercise_calories || 0) : 0;

        // 2. exercise_logs 조회 쿼리 구성 (master_exercise_types와 JOIN)
        let workoutListQuery = `
            SELECT el.id, el.exercise_type_id, met.exercise_name AS exercise_name, el.duration_min, el.calories_burned_session, el.recorded_at
            FROM exercise_logs el
            JOIN master_exercise_types met ON el.exercise_type_id = met.id
            WHERE el.user_id = ? 
              AND DATE(el.recorded_at) = ?
            ORDER BY el.recorded_at ASC
        `;
        const queryParams = [userId, targetDate];

        const [workoutListRows] = await pool.query(workoutListQuery, queryParams);

        // 반환 구조: 각 운동 항목의 고유 ID(id) 포함
        res.json({
            message: "운동 기록 조회 성공",
            target_date: targetDate,
            total_exercise_calories: total_exercise_calories,
            workout_list: workoutListRows 
        });

    } catch (error) {
        console.error('운동 기록 조회 오류:', error);
        res.status(500).json({ message: "운동 기록 조회 중 서버 오류가 발생했습니다." });
    }
});


// DELETE /v1/exercise/:workoutId : 운동 상세 기록 삭제
router.delete('/:workoutId', requireAuth, async (req, res) => {
    const connection = await pool.getConnection();
    try {
        const userId = req.user.id;
        const workoutId = req.params.workoutId; // 삭제할 운동 기록의 고유 ID

        await connection.beginTransaction();

        // 1. 삭제하려는 exercise_log가 사용자 소유인지 확인하고, health_record ID를 가져옴.
        const [workoutCheckRows] = await connection.query(
            // health_records의 recorded_at과 exercise_logs의 DATE(recorded_at)이 일치하는 레코드를 찾습니다.
            `SELECT el.id, hr.id AS record_id 
             FROM exercise_logs el
             JOIN health_records hr ON el.user_id = hr.user_id 
             WHERE el.id = ? 
               AND el.user_id = ? 
               AND DATE(el.recorded_at) = hr.recorded_at`, 
            [workoutId, userId]
        );

        if (workoutCheckRows.length === 0) {
            await connection.rollback();
            return res.status(404).json({ message: "삭제할 운동 기록을 찾을 수 없거나 권한이 없습니다." });
        }

        const recordId = workoutCheckRows[0].record_id;

        // 2. exercise_logs에서 해당 기록 삭제
        const [deleteResult] = await connection.query('DELETE FROM exercise_logs WHERE id = ? AND user_id = ?', [workoutId, userId]); 

        if (deleteResult.affectedRows === 0) {
            await connection.rollback();
            // 이미 삭제된 경우를 대비하여 200 OK 대신 404를 반환하거나 여기서 에러를 발생시킬 수 있으나,
            // 트랜잭션 시작 전의 체크로 충분하다고 판단하고 진행합니다.
            return res.status(404).json({ message: "삭제할 운동 기록을 찾을 수 없거나 이미 삭제되었습니다." });
        }

        // 3. 해당 health_record ID를 기반으로 총 칼로리 재계산
        const totalCalories = await calculateTotalExerciseCalories(recordId, connection);

        // 4. health_records의 총 소모 칼로리(exercise_calories)를 업데이트.
        await connection.query(
            'UPDATE health_records SET exercise_calories = ? WHERE id = ?',
            [totalCalories, recordId]
        );

        await connection.commit();

        res.json({ 
            message: "운동 기록 삭제 및 칼로리 업데이트 성공",
            deleted_workout_id: workoutId,
            total_exercise_calories: totalCalories
        });

    } catch (error) {
        await connection.rollback();
        console.error('운동 기록 삭제 오류:', error);
        res.status(500).json({ message: "운동 기록 삭제 중 서버 오류가 발생했습니다." });
    } finally {
        connection.release();
    }
});


module.exports = router;
