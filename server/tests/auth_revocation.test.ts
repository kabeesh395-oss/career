import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'career-hub-auth-'));
process.env.DATABASE_PATH = path.join(tempDir, 'career-hub.sqlite');
process.env.JWT_SECRET = 'phase3-revocation-secret-with-more-than-16-chars';
process.env.NODE_ENV = 'test';

const { initDatabase, getDatabase } = await import('../src/db/database.js');
const { authenticate, generateToken } = await import('../src/middleware/auth.js');

initDatabase();
const database = getDatabase();
database.prepare(
  'INSERT INTO users (id, email, password_hash, full_name) VALUES (?, ?, ?, ?)'
).run('revocation-user', 'revocation@example.test', 'test-hash', 'Revocation Test User');

const token = generateToken({ id: 'revocation-user', email: 'revocation@example.test', role: 'user' });

const runAuth = () => {
  let nextCalled = false;
  let responseStatus = 200;
  let responseBody: any;
  const response = {
    status(code: number) {
      responseStatus = code;
      return response;
    },
    json(body: unknown) {
      responseBody = body;
      return response;
    }
  } as any;
  authenticate({ headers: { authorization: `Bearer ${token}` } } as any, response, () => {
    nextCalled = true;
  });
  return { nextCalled, responseStatus, responseBody };
};

assert.equal(runAuth().nextCalled, true);
database.prepare('UPDATE users SET token_version = token_version + 1 WHERE id = ?').run('revocation-user');
const revoked = runAuth();
assert.equal(revoked.nextCalled, false);
assert.equal(revoked.responseStatus, 401);
assert.equal(revoked.responseBody.error.code, 'SESSION_REVOKED');

database.close();
fs.rmSync(tempDir, { recursive: true, force: true });
console.log('AUTH REVOCATION TEST PASSED: token rejected immediately after version revocation.');
