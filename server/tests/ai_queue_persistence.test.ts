import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'career-hub-ai-jobs-'));
process.env.DATABASE_PATH = path.join(tempDir, 'career-hub.sqlite');

const { initDatabase, getDatabase } = await import('../src/db/database.js');
const { AiQueueManager } = await import('../src/services/aiQueue.service.js');

initDatabase();
const database = getDatabase();
database.prepare(
  'INSERT INTO users (id, email, password_hash, full_name) VALUES (?, ?, ?, ?)'
).run('durable-user', 'durable@example.test', 'test-hash', 'Durable Test User');

const firstManager = new AiQueueManager(60_000);
firstManager.shutdown();
const created = firstManager.enqueue('durable-user', 'ROADMAP_GEN', { targetRole: 'Backend Engineer' }, 'durable-key');
const stored = database.prepare('SELECT status, idempotency_key FROM ai_jobs WHERE id = ?').get(created.id) as any;
assert.equal(stored.status, 'QUEUED');
assert.equal(stored.idempotency_key, 'durable-key');

// Simulate an interrupted worker after it claimed the job.
database.prepare("UPDATE ai_jobs SET status = 'PROCESSING' WHERE id = ?").run(created.id);
const recoveredManager = new AiQueueManager(60_000);
recoveredManager.shutdown();
const recovered = recoveredManager.getJob(created.id);
assert.equal(recovered?.status, 'PROCESSING');

// The recovery loop normalizes PROCESSING to RETRYING before the next attempt.
(recoveredManager as any).recoverPendingJobs();
assert.equal(recoveredManager.getJob(created.id)?.status, 'RETRYING');

firstManager.shutdown();
recoveredManager.shutdown();
database.close();
fs.rmSync(tempDir, { recursive: true, force: true });
console.log('AI JOB PERSISTENCE TEST PASSED: durable enqueue and interrupted-worker recovery verified.');
