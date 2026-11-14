import assert from 'node:assert/strict';
import { createCorsOriginCallback } from '../src/config/cors.js';

function isAllowed(
  policy: ReturnType<typeof createCorsOriginCallback>,
  origin: string | undefined
): Promise<boolean> {
  return new Promise(resolve => {
    policy(origin, (error, allowed) => resolve(!error && allowed === true));
  });
}

const productionPolicy = createCorsOriginCallback(
  'https://app.careerhub.ai, https://admin.careerhub.ai',
  'production'
);

assert.equal(await isAllowed(productionPolicy, 'https://app.careerhub.ai'), true);
assert.equal(await isAllowed(productionPolicy, 'https://admin.careerhub.ai'), true);
assert.equal(await isAllowed(productionPolicy, 'https://untrusted.example'), false);
assert.equal(await isAllowed(productionPolicy, undefined), true);

assert.throws(
  () => createCorsOriginCallback('*', 'production'),
  /explicit origins/
);
assert.throws(
  () => createCorsOriginCallback(undefined, 'production'),
  /CORS_ORIGIN/
);
assert.throws(
  () => createCorsOriginCallback('*', 'development'),
  /explicit origins/
);

const developmentPolicy = createCorsOriginCallback(undefined, 'development');
assert.equal(await isAllowed(developmentPolicy, 'http://localhost:5173'), true);
assert.equal(await isAllowed(developmentPolicy, 'https://untrusted.example'), false);

const testPolicy = createCorsOriginCallback(undefined, 'test');
assert.equal(await isAllowed(testPolicy, 'https://any-test-origin.example'), true);

console.log('CORS POLICY TEST PASSED: explicit allowlists, wildcard rejection, production configuration, and test mode verified.');
