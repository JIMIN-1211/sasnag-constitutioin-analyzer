const jwt = require('jsonwebtoken');
const secret = process.env.JWT_SECRET;

function signAccess(payload) {
  return jwt.sign(payload, secret, { expiresIn: '30m' }); // 30분
}
function verifyAccess(token) {
  return jwt.verify(token, secret);
}

module.exports = { signAccess, verifyAccess };
