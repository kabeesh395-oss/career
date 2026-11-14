import assert from 'node:assert/strict';
import http from 'node:http';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'career-hub-http-auth-'));
process.env.NODE_ENV = 'test';
process.env.JWT_SECRET = 'phase3-http-secret-with-more-than-16-chars';
process.env.DATABASE_PATH = path.join(tempDir, 'career-hub.sqlite');

const { default: app } = await import('../src/server.js');
const { getDatabase } = await import('../src/db/database.js');
const server = http.createServer(app);
await new Promise<void>((resolve) => server.listen(0, '127.0.0.1', () => resolve()));
const address = server.address();
assert.ok(address && typeof address !== 'string');
const baseUrl = `http://127.0.0.1:${address.port}`;

async function request(pathname: string, options: RequestInit = {}) {
  return fetch(`${baseUrl}${pathname}`, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(options.headers || {}) }
  });
}

try {
  const email = `http_auth_${Date.now()}@example.test`;
  const password = 'SecurePassword123';
  const signup = await request('/api/auth/signup', {
    method: 'POST',
    body: JSON.stringify({ email, password, fullName: 'HTTP Auth User' })
  });
  assert.equal(signup.status, 201);
  const signupBody = await signup.json() as { token: string; user: { email: string } };
  assert.equal(signupBody.user.email, email);
  assert.ok(signupBody.token);

  const login = await request('/api/v1/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email: email.toUpperCase(), password })
  });
  assert.equal(login.status, 200);
  const loginBody = await login.json() as { token: string };
  assert.ok(loginBody.token);

  const authHeaders = { Authorization: `Bearer ${loginBody.token}` };
  const me = await request('/api/auth/me', { headers: authHeaders });
  assert.equal(me.status, 200);
  const meBody = await me.json() as { user: { email: string } };
  assert.equal(meBody.user.email, email);

  const logout = await request('/api/v1/auth/logout', { method: 'POST', headers: authHeaders });
  assert.equal(logout.status, 200);

  const revoked = await request('/api/auth/me', { headers: authHeaders });
  assert.equal(revoked.status, 401);
  const revokedBody = await revoked.json() as { error: { code: string } };
  assert.equal(revokedBody.error.code, 'SESSION_REVOKED');

  const database = getDatabase();
  const tokenVersion = database.prepare('SELECT token_version FROM users WHERE email = ?').get(email) as { token_version: number };
  assert.equal(tokenVersion.token_version, 1);
  database.close();

  console.log('AUTH HTTP FLOW TEST PASSED: signup, login, /me, logout, and revocation verified.');
} finally {
  await new Promise<void>((resolve, reject) => server.close((error) => error ? reject(error) : resolve()));
  fs.rmSync(tempDir, { recursive: true, force: true });
}
