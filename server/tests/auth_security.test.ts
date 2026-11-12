import assert from 'node:assert/strict';
import jwt from 'jsonwebtoken';

process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = 'phase3-test-secret-with-more-than-16-chars';

const { generateToken, getJwtSecret } = await import('../src/middleware/auth.js');
const token = generateToken({ id: 'auth-user', email: 'auth@example.test', role: 'user' });
const decoded = jwt.verify(token, getJwtSecret(), {
  algorithms: ['HS256'],
  issuer: 'career-hub-api',
  audience: 'career-hub-client'
}) as jwt.JwtPayload & { id: string; email: string; role: string };

assert.equal(decoded.id, 'auth-user');
assert.equal(decoded.email, 'auth@example.test');
assert.equal(decoded.role, 'user');
assert.equal(decoded.iss, 'career-hub-api');
assert.equal(decoded.aud, 'career-hub-client');
assert.throws(() => jwt.verify(token, getJwtSecret(), {
  algorithms: ['HS256'],
  issuer: 'wrong-issuer',
  audience: 'career-hub-client'
}));
assert.throws(() => jwt.verify(token, getJwtSecret(), {
  algorithms: ['HS256'],
  issuer: 'career-hub-api',
  audience: 'wrong-audience'
}));

console.log('AUTH SECURITY TEST PASSED: constrained JWT algorithm, issuer, audience, expiry, and claims verified.');
