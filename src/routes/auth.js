const express = require('express');
const { pool } = require('../db/pool');
const { hash, verify } = require('../lib/password');
const { signAccess } = require('../lib/jwt');

const router = express.Router();

// 회원가입
router.post('/auth/register', async (req, res) => {
  try {
    const { username, email, password, name } = req.body || {};
    if (!username || !email || !password) {
      return res.status(400).json({ error: { code: 'INVALID_PARAM', message: 'username/email/password required' } });
    }
    const [dup] = await pool.query('SELECT id FROM users WHERE username=? OR email=? LIMIT 1', [username, email]);
    if (dup[0]) {
      return res.status(409).json({ error: { code: 'CONFLICT', message: 'username or email exists' } });
    }
    const pw = await hash(password);
    await pool.query(
      'INSERT INTO users (username,email,password_hash,name) VALUES (?,?,?,?)',
      [username, email, pw, name || null]
    );
    return res.status(201).json({ message: '회원 가입 성공' });
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

// 로그인
router.post('/auth/login', async (req, res) => {
  try {
    const { username, password } = req.body || {};
    if (!username || !password) {
      return res.status(400).json({ error: { code: 'INVALID_PARAM', message: 'username/password required' } });
    }
    const [rows] = await pool.query('SELECT id, password_hash FROM users WHERE username=? LIMIT 1', [username]);
    const u = rows?.[0];
    if (!u) return res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'wrong credentials' } });

    const ok = await verify(password, u.password_hash);
    if (!ok) return res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'wrong credentials' } });

    const token = signAccess({ sub: u.id });
    return res.json({ message: '로그인 성공', token });
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

module.exports = router;
