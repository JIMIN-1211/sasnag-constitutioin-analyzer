const express = require('express');
const router = express.Router();

const nodemailer = require('nodemailer');
const fetch = require('node-fetch');
const { OAuth2Client } = require('google-auth-library');

const { pool } = require('../db/pool');
const { hash, verify } = require('../lib/password');
const { signAccess } = require('../lib/jwt');

/* ---------------- Google OAuth Client ---------------- */
const gClient = new OAuth2Client(process.env.GOOGLE_CLIENT_ID);

/* ---------------- 이메일 전송 세팅(기존) ---------------- */
const FIXED_AUTH_CODE = '1234';
const preRegistrationData = new Map();

const transporter = nodemailer.createTransport({
  service : 'gmail',
  auth : {
    user : process.env.EMAIL_USER,
    pass : process.env.EMAIL_PASS
  }
});

/* =========================
 * 1) 회원가입(이메일 인증 단계)
 * ========================= */
// 인증코드 메일 전송
router.post('/auth/register-creds', async (req, res) => {
  try{
    const {username, password, email} = req.body || {};
    if (!username || !email || !password) {
      return res.status(400).json({ error: { code: 'INVALID_PARAM', message: 'username/email/password required' } });
    }

    const [dup] = await pool.query('SELECT id FROM users WHERE username=? OR email=? LIMIT 1', [username, email]);
    if (dup[0]) {
      return res.status(409).json({ error: { code: 'CONFLICT', message: 'username or email exists' } });
    }

    const code = FIXED_AUTH_CODE;
    const expiresAt = Date.now()+5*60*1000;
    const pw = await hash(password);

    preRegistrationData.set(email, { code, expiresAt, username, pw });

    await transporter.sendMail({
      from : process.env.EMAIL_USER,
      to : email,
      subject : "사상체질 앱 회원가입 인증 코드",
      html : `<p>안녕하세요! 사상체질 앱 회원가입을 위한 인증코드입니다. </p>
              <p><b>인증코드 : ${code}</b></p>
              <p>이 코드는 5분간 유효합니다. 감사합니다!</p>`,
    });

    return res.status(200).json({ message : "인증 메일 발송 완료"});
  }catch(e){
    console.log("Fail to send email : ", e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

// 회원가입 최종
router.post('/auth/register', async (req, res) => {
  try {
    const { email, auth_code , name, birth_year, gender } = req.body || {};
    if (!email || !auth_code) {
      return res.status(400).json({ error: { code: 'INVALID_PARAM', message: 'username/email/password required' } });
    }

    const storedData = preRegistrationData.get(email);
    if(!storedData || storedData.expiresAt < Date.now()){
      if(storedData) preRegistrationData.delete(email);
      return res.status(401).json({error : {code : 'INVALID_VERIFICATION_CODE', message : '인증 코드가 만료되었거나 유효하지 않습니다.'}});
    }
    if(auth_code !== storedData.code) {
      return res.status(401).json({error : {code : 'INVALID_VERIFICATION_CODE', message : '인증 코드가 유효하지 않습니다.'}});
    }

    preRegistrationData.delete(email);

    await pool.query(
      'INSERT INTO users (username, email, password_hash, name, gender, birth_year, provider) VALUES (?, ?, ?, ?, ?, ?, ?)',
      [storedData.username, email, storedData.pw, name || null, gender || null, birth_year || null, 'local']
    );

    return res.status(201).json({ message: '회원 가입 성공' });
  } catch (e) {
    console.error(e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

/* =========================
 * 2) 로컬 로그인(기존)
 * ========================= */
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

/* =========================
 * 3) 구글 로그인 (브라우저 플로우)
 * - /v1/auth/google/start 접속 → 구글 동의 → /v1/auth/google/callback
 * ========================= */
router.get('/auth/google/start', (req, res) => {
  const authURL =
    'https://accounts.google.com/o/oauth2/v2/auth?' +
    new URLSearchParams({
      client_id: process.env.GOOGLE_CLIENT_ID,
      redirect_uri: process.env.GOOGLE_REDIRECT_URI, // http://localhost:3000/v1/auth/google/callback
      response_type: 'code',
      scope: 'openid email profile'
    }).toString();

  return res.redirect(authURL);
});

router.get('/auth/google/callback', async (req, res) => {
  const code = req.query.code;
  if (!code) return res.status(400).send('code is required');
  try {
    // 1) code -> token 교환
    const tokenRes = await fetch('https://oauth2.googleapis.com/token', {
      method: 'POST',
      headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
      body: new URLSearchParams({
        code,
        client_id: process.env.GOOGLE_CLIENT_ID,
        client_secret: process.env.GOOGLE_CLIENT_SECRET,
        redirect_uri: process.env.GOOGLE_REDIRECT_URI,
        grant_type: 'authorization_code'
      })
    });
    if (!tokenRes.ok) {
      const t = await tokenRes.text();
      console.error('google token error:', t);
      return res.status(401).send('구글 토큰 교환 실패');
    }
    const tokens = await tokenRes.json(); // { id_token, access_token, ... }
    const idToken = tokens.id_token;

    // 2) idToken 검증
    const ticket = await gClient.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID
    });
    const payload = ticket.getPayload(); // { sub, email, name, ... }

    const sub = String(payload.sub);
    const email = payload.email || null;
    const name = payload.name || null;

    // 3) DB upsert: 이메일 연결 우선 → 없으면 소셜 키 → 없으면 신규
    let userId;
    if (email) {
      const [byEmail] = await pool.query('SELECT id FROM users WHERE email=? LIMIT 1', [email]);
      if (byEmail.length > 0) {
        userId = byEmail[0].id;
        await pool.query('UPDATE users SET provider=?, provider_id=? WHERE id=?', ['google', sub, userId]);
      }
    }
    if (!userId) {
      const [bySocial] = await pool.query(
        'SELECT id FROM users WHERE provider=? AND provider_id=? LIMIT 1',
        ['google', sub]
      );
      if (bySocial.length > 0) {
        userId = bySocial[0].id;
      } else {
        const username = email ?? `google_${sub}`;
        const [r2] = await pool.query(
          'INSERT INTO users (username, email, name, provider, provider_id) VALUES (?,?,?,?,?)',
          [username, email, name, 'google', sub]
        );
        userId = r2.insertId;
      }
    }

    // 4) JWT
    const token = signAccess({ sub: userId });
    return res.send(`<h3>Google 로그인 성공</h3><p>JWT: ${token}</p>`);
  } catch (e) {
    console.error(e);
    return res.status(500).send('서버 오류');
  }
});

/* =========================
 * 4) (선택) 앱/프론트에서 idToken을 직접 보내는 방식
 * ========================= */
router.post('/auth/google', async (req, res) => {
  try {
    const { idToken } = req.body || {};
    if (!idToken) {
      return res.status(400).json({ error: { code: 'INVALID_PARAM', message: 'idToken required' } });
    }

    const ticket = await gClient.verifyIdToken({
      idToken,
      audience: process.env.GOOGLE_CLIENT_ID
    });
    const payload = ticket.getPayload();

    const sub = String(payload.sub);
    const email = payload.email || null;
    const name = payload.name || null;

    let userId;
    if (email) {
      const [byEmail] = await pool.query('SELECT id FROM users WHERE email=? LIMIT 1', [email]);
      if (byEmail.length > 0) {
        userId = byEmail[0].id;
        await pool.query('UPDATE users SET provider=?, provider_id=? WHERE id=?', ['google', sub, userId]);
      }
    }
    if (!userId) {
      const [bySocial] = await pool.query(
        'SELECT id FROM users WHERE provider=? AND provider_id=? LIMIT 1',
        ['google', sub]
      );
      if (bySocial.length > 0) {
        userId = bySocial[0].id;
      } else {
        const username = email ?? `google_${sub}`;
        const [r2] = await pool.query(
          'INSERT INTO users (username, email, name, provider, provider_id) VALUES (?,?,?,?,?)',
          [username, email, name, 'google', sub]
        );
        userId = r2.insertId;
      }
    }

    const token = signAccess({ sub: userId });
    return res.json({ message: '구글 로그인 성공', token });
  } catch (e) {
    console.error(e);
    return res.status(401).json({ message: '구글 인증 실패' });
  }
});

module.exports = router;
