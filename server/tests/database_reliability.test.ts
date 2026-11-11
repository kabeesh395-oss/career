import assert from 'node:assert/strict';
import fs from 'node:fs';
import os from 'node:os';
import path from 'node:path';

const tempDir = fs.mkdtempSync(path.join(os.tmpdir(), 'career-hub-db-'));
const databasePath = path.join(tempDir, 'career-hub.sqlite');
const backupPath = path.join(tempDir, 'backup.sqlite');
process.env.DATABASE_PATH = databasePath;

const { initDatabase, checkDatabaseHealth, backupDatabase, verifyDatabaseBackup } = await import('../src/db/database.js');

const database = initDatabase();
const health = checkDatabaseHealth();
assert.equal(health.path, databasePath);
assert.equal(health.journalMode.toLowerCase(), 'wal');
assert.equal(health.foreignKeysEnabled, true);
assert.equal(health.integrity, 'ok');

const migration = database
  .prepare('SELECT migration_id FROM schema_migrations WHERE migration_id = ?')
  .get('001_roadmap_item_detail_fields') as { migration_id?: string } | undefined;
assert.equal(migration?.migration_id, '001_roadmap_item_detail_fields');

const backup = backupDatabase(backupPath);
assert.equal(backup, backupPath);
assert.equal(fs.existsSync(backupPath), true);
assert.ok(fs.statSync(backupPath).size > 0);
assert.deepEqual(verifyDatabaseBackup(backupPath), {
  path: backupPath,
  integrity: 'ok',
  schemaPresent: true
});

const readinessPlan = database.prepare(
  'EXPLAIN QUERY PLAN SELECT * FROM readiness_snapshots WHERE user_id = ? ORDER BY created_at DESC LIMIT 1'
).all('user-test') as Array<{ detail?: string }>;
assert.ok(readinessPlan.some((row) => row.detail?.includes('idx_readiness_snapshots_user_created')));

const aiJobsPlan = database.prepare(
  "EXPLAIN QUERY PLAN SELECT * FROM ai_jobs WHERE status IN ('QUEUED', 'RETRYING') ORDER BY created_at ASC"
).all() as Array<{ detail?: string }>;
assert.ok(aiJobsPlan.some((row) => row.detail?.includes('idx_ai_jobs_status_updated')));

console.log('DATABASE RELIABILITY TEST PASSED: configured path, WAL, foreign keys, integrity, migration ledger, and backup verified.');

database.close();
fs.rmSync(tempDir, { recursive: true, force: true });
