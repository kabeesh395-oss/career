import assert from 'node:assert/strict';
import { errorHandler } from '../src/middleware/error.js';

const originalEnv = process.env.NODE_ENV;
process.env.NODE_ENV = 'production';

let statusCode = 0;
let payload: any;
let logOutput = '';
const originalConsoleError = console.error;
console.error = (...args: unknown[]) => {
  logOutput += args.map(String).join(' ');
};
const response = {
  status(code: number) {
    statusCode = code;
    return response;
  },
  json(body: unknown) {
    payload = body;
    return response;
  }
} as any;

errorHandler(
  Object.assign(new Error('database password=should-not-leak'), {
    statusCode: 500,
    code: 'INTERNAL_SERVER_ERROR',
    details: { connectionString: 'secret-value' }
  }),
  { method: 'GET', path: '/api/private', originalUrl: '/api/private?email=person@example.com' } as any,
  response,
  (() => undefined) as any
);

assert.equal(statusCode, 500);
assert.equal(payload.error.code, 'INTERNAL_SERVER_ERROR');
assert.equal(payload.error.message, 'An unexpected server error occurred. Please try again.');
assert.equal(payload.error.details, undefined);
assert.equal(JSON.stringify(payload).includes('should-not-leak'), false);
assert.equal(JSON.stringify(payload).includes('secret-value'), false);
assert.equal(logOutput.includes('should-not-leak'), false);
assert.equal(logOutput.includes('person@example.com'), false);

console.error = originalConsoleError;
if (originalEnv === undefined) delete process.env.NODE_ENV;
else process.env.NODE_ENV = originalEnv;
console.log('ERROR HANDLER SECURITY TEST PASSED: production 5xx messages and details are redacted.');
