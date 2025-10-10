const express = require('express');
const router = express.Router();
const db = require('../db'); // 데이터베이스 연결 모듈
const authenticateToken = require('../middleware/auth'); // JWT 인증 미들웨어

//목표 운동 시간 있을 경우 변경 필요!!
const CONSTITUTION_STANDARDS = {
    '태양인' : {
        exercise_duration : 30,
        meal_calories_ratio : 0.9,
        sleep_duration : 6,
        daily_calories_factor : 0.9
    },
    '태음인' : {
        exercise_duration : 60,
        meal_calories_ratio : 0.8,
        sleep_duration : 7,
        daily_calories_factor : 1.1
    },
    '소양인' : {
        exercise_duration : 45,
        meal_calories_ratio : 1.0,
        sleep_duration : 7,
        daily_calories_factor : 1.0
    },
    '소음인' : {
        exercise_duration : 30,
        meal_calories_ratio : 1.1,
        sleep_duration : 8,
        daily_calories_factor : 1.15
    }
};

//사용자의 기록과 체질별 기준을 비교하여 점수를 계산
function getScores(constitution_type, records){
    const standards = CONSTITUTION_STANDARDS[constitution_type];
    if(!standards){
        return {meal : 0, exercise : 0, sleep : 0};
    }

    let mealScore = 0;
    if(records.meal_calories && records.meal_goal){
        const ratio = records.meal_calories / (records.meal_goal * standards.meal_calories_ratio);
        mealScore = Math.min(100, Math.round(ratio * 100));
    }
    
    let exerciseScore = 0;
    if(records.exercise_duration){
        const ratio = records.exercise_duration / standards.exercise_duration;
        exerciseScore = Math.min(100, Math.round(ratio * 100));
    }

    let sleepScore = 0;
    if(records.sleep_duration){
        const ratio = records.sleep_duration / standards.sleep_duration;
        sleepScore = Math.min(100, Math.round(ratio * 100));
    }

    return {
        meal : mealScore,
        exercise : exerciseScore,
        sleep : sleepScore
    };
    
}
// 홈 화면 데이터 가져오기 (GET /v1/home)
router.get('/', authenticateToken, async(req, res) => {
    try{
        const userId = req.user.id;

        // 1. 사용자 정보 (이름, 신체 정보)
        const [userInfoRows] = await db.query(
            `select name, gender, age, height, weight from users where id = ?`, 
            [userId]
        );
        const userInfo = userInfoRows[0];
        
        if(!userInfo || !userInfo.age || !userInfo.height || !userInfo.weight){
            return res.status(400).json({message : "사용자 정보가 부족하여 목표 칼로리를 계산할 수 없습니다. "});
        }

        // 2. 사용자의 최신 체질 정보 가져오기
        const [constitutionRows] = await db.query(
            `select constitution_type from user_constitution where id = ?`, 
            [userId]
        );
        const constituion = constitutionRows[0]?.constitution_type || '소양인'; //디폴트 체질 값

        // 3. 체질별 목표 칼로리 계산
        const {age, height, weight, gender} = userInfo;

        //미플린 - 세인트 지어 공식 (bmr = 기초 대사율)
        let bmr;
        if(gender == '남' || gender == 'male') {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) + 5;
        }else {
            bmr = (10 * weight) + (6.25 * height) - (5 * age) - 161;
        }

        //TDEE(총 일일 에너지 소비량) 계산
        const activityFactor = 1.4;
        const tdee  = bmr * activityFactor;

        const recommendedGoalCalrories = Math.round(tdee * CONSTITUTION_STANDARDS[constituion].daily_calories_factor);
        // 4. 오늘 실천 기록 (식단, 운동, 수면)
        const [recordsRows] = await db.query(
            `select
                calories as meal_calories, 
                exercise_duration, 
                sleep_records as sleep_duration
            from health_records where user_id = ? ORDER BY recorded_at desc LIMIT 1`,
            [userId]
        );
        const records = recordsRows[0];

        // 5. 체질별 점수 계산
        const scores = getScores(constituion, {
            meal_calories : records.meal_calories,
            meal_goal : recommendedGoalCalrories,
            exercise_duration : exercise_duration,
            sleep_duration : sleep_duration
        });

        // 6. 체질별 맞춤 광고 배너 및 건강 팁 (임시 데이터) 
        //---------------------------------아직 미정--------------------------------------------------------//

        // DB에서 해당 체질의 모든 팁 가져오기
        const [tipRows] = await db.query(
            `select tip_type, title, content from constitution_tips where constitution_type = ?`,
            [constituion]
        );

        //팁을 식단과 운동으로 분류
        const tipsForConstitution = tipRows.redduce((acc, tip)=>{
            const type = tip.tip_type;
            if(type === 'diet' || type === 'exercise'){
                if(!acc[type]){
                    acc[type] = [];
                }
                acc[type].push(tip);
            }
            return acc;
        }, {diet : [], excercise : []});

        //식단 팁 랜덤 선택
        const dietTips = tipsForConstitution.diet;
        let randomDietTip = { title : "식단 팁 없음", content : "등록된 식단 팁이 없습니다. "};
        if(dietTips.length > 0){
            randomDietTip = dietTips[Math.floor(Math.random() * dietTips.length)];
        }
        //운동 팁 랜덤 선택
        const exerciseTips = tipsForConstitution.excercise;
        let randomExerciseTip = { title : "운동 팁 없음", content : "등록된 운동 팁이 없습니다. "};
        if(exerciseTips.length > 0){
            randomExerciseTip = exerciseTips[Math.floor(Math.random() * exerciseTips.length)];
        }

        //최종 healthTips 베열 구성
        const healthTips = [
            {
                title : `[식단] ${randomDietTip.title}`,
                content : randomDietTip.content,
            },
            {
                title : `[운동] ${randomExerciseTip.title}`,
                content : randomExerciseTip.content,
            }
        ];

        // 프론트엔드에 순수 데이터를 전송
        res.json({
            user_info : {
                name : userInfo.name,
                constituion : constituion,
                condition : 'default'
            },
            today_report : {
                meal_calories : records.meal_calories,
                meal_goal : recommendedGoalCalrories,
                sleep_duraition : records.sleep_duration,
                exercise_duration : records.exercise_duration,
                exercise_calories : records.exercise_calories,
                //식단, 수면, 운동 점수
                health_balance : {
                    meal : scores.meal,
                    sleep : scores.sleep,
                    exercise : scores.exercise
                }
            },
            daily_records : {
                meal : {
                    calories : records.meal_calories || 0, 
                    goal : recommendedGoalCalrories,
                    is_recorded : records.meal_calories !== null
                },
                exercise : {
                    is_recorded : records.exercise_duration !== null
                },
                sleep : {
                    is_recorded : true
                }
            },
            health_tips : healthTips
            //추천 제품 광고 -- 6번과 연동
        });
    }catch (error) {
        console.error('Error fetching home data : ', error);
        res.status(500).json({message : "서버 오류"});
    }
});

module.exports = router;
