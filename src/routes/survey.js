const express = require('express');
const {pool} = require('../db/pool');
const {requireAuth}  = require('../middleware/authn');


const router = express.Router();

router.post('/api/constitution/analyze', requireAuth,  async(req, res)=>{
    const {userId, userInfo, answers} = req.body;

    if(!userId || !userInfo || !answers){
        return res.status(400).json({
            message : "필수 정보가 누락되었습니다."
        });
    }

    const {age, height, weight, bmi} = userInfo;

    const conn = await pool.getConnection();
    await conn.beginTransaction();

    try{
        //users 데이터 업데이트
        const  updateUserQuery = `UPDATE users SET age = ?, height = ?, weight = ? WHERE id = ? `;
        await conn.execute(updateUserQuery, [age, height, weight, userId]);

        //설문응답 저장
        const answersData = answers.map(item=> [userId, item.questionId, item.answerId]);
        const insertAnswersQuery = `INSERT INTO survey_answers (user_id, question_id, answer_id) VALUES ?`;
        await conn.query(insertAnswersQuery, [answersData]);

        const scores = {
            taeyangin : 0, 
            soyangin : 0, 
            taeumin : 0, 
            souemin : 0
        };

        answers.forEach(item => {
            const questionId = item.questionId;
            const answerId = item.answerId;

            switch (questionId) {
                case 1 :
                    if(answerId === 1){
                        scores.taeyangin+=3;
                        scores.soyangin+=3;
                    }
                    if(answerId === 3) {
                        scores.souemin++;
                    }
                    break;
                case 2 :
                    if(answerId === 1){
                        scores.taeyangin+=3;
                        scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.taeumin++;
                        scores.souemin++;
                    }
                    break;
                case 3 :
                    if(answerId === 1){
                        scores.taeyangin+=3;
                        scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                        scores.taeumin++;
                    }
                    break;
                case 4 : 
                    if(answerId === 1){
                        scores.taeyangin+=3;
                        scores.soyangin+=3; 
                    }
                    if(answerId === 3){
                        scores.souemin++;
                    }
                    break;
                case 5 : 
                    if(answerId === 1){
                       scores.taeyangin+=3;
                       scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                        scores.taeumin++;
                    }
                    break;
                case 6 : 
                    if(answerId === 1){
                        scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                        scores.taeumin++;
                    }
                    break;
                case 7: 
                    if(answerId === 1){
                        scores.taeumin+=3;
                        scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                    }
                    break;
                case 8 : 
                    if(answerId === 1){
                        scores.taeumin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                    }
                    break;
                case 9 : 
                    if(answerId === 1){
                        scores.taeumin+=3;
                    }
                    break;
                case 10 : 
                    if(answerId === 1){
                        scores.taeumin+=3;
                    }
                    if(answerId === 3){
                        scores.souemin++;
                    }
                    break;
                case 11 : 
                    if(answerId === 1){
                        scores.taeumin+=3;
                    }
                    if(answerId === 3){
                        scores.taeyangin++;
                        scores.souemin++;
                    }
                    break;
                case 12 : 
                    if(answerId === 1){
                        scores.taeumin+=3;
                    }
                    if(answerId === 3){
                        scores.taeyangin++;
                        scores.souemin++;
                    }
                    break;
                case 13 : 
                    if(answerId === 1){
                        scores.souemin++;
                        scores.taeumin++;
                    }
                    if(answerId === 3){
                        scores.soyangin+=3;
                    }
                    break;
                case 14 : 
                    if(answerId === 1){
                        scores.taeyangin+=3;
                    }
                    if(answerId === 3){
                        scores.soyangin++;
                        scores.taeumin++;
                        scores.souemin++;
                    }
                    break;
                case 15 : 
                    if(answerId === 1){
                        scores.souemin+=3;
                    }
                    if(answerId === 2){
                        scores.soyangin+=3;
                    }
                    if(answerId === 3){
                        scores.taeyangin+=3;
                    }
                    break;
                case 16 : 
                    if(answerId === 1){
                        scores.souemin+=3;
                    }
                    if(answerId === 2){
                        scores.soyangin+=3;
                        scores.taeumin++;
                    }
                    if(answerId === 3){
                        scores.taeyangin++;
                    }
            }

        });


        let constituion = 'unknown';
        let maxScore = 0;

        for(const type in scores){
            if(scores[type] > maxScore){
                maxScore = scores[type];
                constituion = type;
            }
        }

        //설문 분석 결과 및 타입 저장
        const insertConstitutionQuery = `INSERT INTO user_constitution (user_id, constitution_type, score) values (?, ?, ?)`;
        await pool.query(insertConstitutionQuery, [userId, constituion, maxScore]);

        await conn.commit();

        res.status(200).json({
            message : "체질 분석이 완료되었습니다. ", 
            constitution : constituion,
            score : maxScore
        });

    }catch(error){
        await conn.rollback();

        console.error("체질 분석 및 저장 실패 : ", error);
        res.status(500).json({message : "서버 오류로 분석에 실패했습니다. "});
    }finally{
        conn.release();
    }
});

module.exports = router;


