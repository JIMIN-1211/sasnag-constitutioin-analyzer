const { verifyAccess } = require('../lib/jwt');

function requireAuth(req, res, next) {
  const h = req.headers.authorization || '';
  const token = h.startsWith('Bearer ') ? h.slice(7) : '';
  if (!token) return res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'no token' } });
  try {
    const payload = verifyAccess(token);
    req.user = { id: payload.sub };
    next();
  } catch (e) {
    return res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'invalid token' } });
  }
}

module.exports = { requireAuth };
