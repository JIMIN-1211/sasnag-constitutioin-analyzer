const express = require('express');
const nodemailer = require('nodemailer');
const { pool } = require('../db/pool');
const { hash, verify } = require('../lib/password');
const { signAccess } = require('../lib/jwt');

const router = express.Router();

const FIXED_AUTH_CODE = '1234';

const preRegistrationData = new Map();

const transporter = nodemailer.createTransport({
  service : 'gmail',
  auth : {
    user : process.env.EMAIL_USER,
    pass : process.env.EMAIL_PASS
  }
});

//인증코드 메일 전송
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

    preRegistrationData.set(email, {
      code,
      expiresAt,
      username,
      pw,
    });

    await transporter.sendMail({
      from : process.env.EMAIL_USER,
      to : email,
      subject : "사상체질 앱 회원가입 인증 코드",
      html : `<p>안녕하세요! 사상체질 앱 회원가입을 위한 인증코드입니다. </p>`+
      `<p><b>인증코드 : ${code}</b></p>`+
      `<p>이 코드는 5분간 유효합니다. 감사합니다!</p>`,
    });

    return res.status(200).json({ message : "인증 메일 발송 완료"});
  }catch(e){
    console.log("Fail to send email : ", e);
    return res.status(500).json({ error: { code: 'INTERNAL', message: 'server error' } });
  }
});

// 회원가입
router.post('/auth/register', async (req, res) => {
  try {
    const { email, auth_code , name, birth_year, gender} = req.body || {};
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
      'INSERT INTO users (username, email, password_hash, name, gender, birth_year) VALUES (?, ?, ?, ?, ?, ?)',
      [
        storedData.username,
        email,
        storedData.pw,
        name,
        gender,
        birth_year,
      ]
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
