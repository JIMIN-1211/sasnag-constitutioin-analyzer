// src/routes/users.js
const express = require('express');
const { pool } = require('../db/pool');
const { requireAuth } = require('../middleware/authn');

const router = express.Router();

// 내 정보 조회 (로그인 필요)
router.get('/users/me', requireAuth, async (req, res) => {
  try {
    const userId = req.user.id;
    const [rows] = await pool.query(
      'SELECT id, username, email, name, created_at FROM users WHERE id=? LIMIT 1',
      [userId]
    );
    return res.json(rows[0] || null);
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

module.exports = router;
