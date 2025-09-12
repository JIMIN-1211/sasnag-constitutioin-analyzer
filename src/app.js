require('dotenv').config();
const express = require('express');
const cors = require('cors');
const helmet = require('helmet');
const morgan = require('morgan');

const auth = require('./routes/auth');
const users = require('./routes/users'); // 선택
const survey = requrie('./routes/survey');

const app = express();
app.use(express.json());
app.use(cors({ origin: true }));
app.use(helmet());
app.use(morgan('dev'));

app.get('/v1/healthz', (_req, res) => {
  res.json({ ok: true, time: new Date().toISOString() });
});

app.use('/v1', auth);
app.use('/v1', users); // 선택
app.use('/v1', survey);

module.exports = { app };
