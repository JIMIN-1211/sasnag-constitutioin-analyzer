// routes/mypage.js
const express = require('express');
const router = express.Router();

const { pool } = require('../db/pool');
const { requireAuth } = require('../middleware/authn');

// 공통: 당사 DB에서 최신 체질 1건 가져오는 헬퍼
async function getLatestConstitution(userId) {
  const [rows] = await pool.query(
    `SELECT constitution_type, score, created_at
       FROM user_constitution
      WHERE user_id = ?
      ORDER BY created_at DESC
      LIMIT 1`,
    [userId]
  );
  return rows[0] || null;
}

/**
 * 1) 허브용 요약
 * GET /v1/me/summary
 * - 이름/이메일, 최근 체질 여부만 리턴
 */
router.get('/me/summary', requireAuth, async (req, res) => {
  try {
    const userId = req.user.sub; // JWT에서 온 사용자 id

    const [[u]] = await pool.query(
      `SELECT id, name, email, alert_enabled
         FROM users WHERE id = ? LIMIT 1`,
      [userId]
    );

    if (!u) return res.status(404).json({ message: '사용자를 찾을 수 없습니다.' });

    const latest = await getLatestConstitution(userId);

    return res.json({
      id: u.id,
      name: u.name,
      email: u.email,
      avatarUrl: null, // 아직 컬럼 없으니 null
      badges: {
        hasConstitution: !!latest,
        latestConstitution: latest ? latest.constitution_type : null,
        unreadNotices: 0
      }
    });
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: '서버 오류' });
  }
});

/**
 * 2) 마이페이지 개요
 * GET /v1/me/details
 * - 상단 프로필 + 최근 체질 + 최근 7일 요약(health_records가 없으면 0)
 */
router.get('/me/details', requireAuth, async (req, res) => {
  try {
    const userId = req.user.sub;

    // 프로필
    const [[u]] = await pool.query(
      `SELECT id, name, email, alert_enabled
         FROM users WHERE id = ? LIMIT 1`,
      [userId]
    );
    if (!u) return res.status(404).json({ message: '사용자를 찾을 수 없습니다.' });

    // 최근 체질
    const latest = await getLatestConstitution(userId);

    // 최근 7일 건강 요약
    const [sum] = await pool.query(
      `SELECT
          SUM(COALESCE(exercise_records,0))          AS exercise_min,
          AVG(NULLIF(sleep_records,0))               AS avg_sleep_h,
          SUM(COALESCE(calories,0))                  AS total_calories,
          SUM(COALESCE(calories_burned,0))           AS total_burned
        FROM health_records
       WHERE user_id = ?
         AND recorded_at >= DATE_SUB(NOW(), INTERVAL 7 DAY)`,
      [userId]
    );
    const s = sum[0] || {};
    return res.json({
      profile: {
        name: u.name,
        email: u.email,
        phone: null,
        avatarUrl: null
      },
      activity: {
        latestConstitution: latest
          ? { type: latest.constitution_type, score: latest.score, at: latest.created_at }
          : null,
        recordsSummary: {
          last7d: {
            exercise_min: Number(s.exercise_min || 0),
            avg_sleep_h: s.avg_sleep_h ? Number(s.avg_sleep_h.toFixed(1)) : 0,
            total_calories: Number(s.total_calories || 0),
            total_burned: Number(s.total_burned || 0)
          }
        }
      },
      settings: {
        alert_enabled: !!u.alert_enabled
      }
    });
  } catch (e) {
    console.error(e);
    res.status(500).json({ message: '서버 오류' });
  }
});

module.exports = router;
