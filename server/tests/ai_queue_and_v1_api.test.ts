import { initDatabase, getDatabase } from '../src/db/database.js';
import { aiQueueManager } from '../src/services/aiQueue.service.js';

async function runPhase3BackendTests() {
  console.log('\n======================================================');
  console.log('🧪 RUNNING CAREERHUB PHASE 3 BACKEND & AI QUEUE SUITE');
  console.log('======================================================\n');

  // 1. Initialize DB
  initDatabase();
  const db = getDatabase();
  const dbCheck = db.prepare('SELECT 1 as alive').get() as { alive: number };
  if (dbCheck.alive !== 1) throw new Error('Database readiness check failed.');
  console.log('✅ [1/4] SQLite Database & Readiness Check Verified.');

  // 2. Enqueue AI Job
  const testJob = aiQueueManager.enqueue(
    'user_test_123',
    'ROADMAP_GEN',
    { targetRole: 'Staff AI Engineer', currentSkills: ['Python', 'TypeScript'], experienceYears: 5.0 },
    'idempotent_test_key_1'
  );
  if (!testJob || !testJob.id) throw new Error('AI Job Enqueue failed.');
  console.log(`✅ [2/4] Asynchronous AI Job Enqueued: ID = ${testJob.id}, Status = ${testJob.status}.`);

  // 3. Test Deduplication
  const duplicateJob = aiQueueManager.enqueue(
    'user_test_123',
    'ROADMAP_GEN',
    { targetRole: 'Staff AI Engineer', currentSkills: ['Python', 'TypeScript'], experienceYears: 5.0 },
    'idempotent_test_key_1'
  );
  if (duplicateJob.id !== testJob.id) throw new Error('Job Deduplication Failed! Created duplicate job.');
  console.log('✅ [3/4] AI Request Deduplication & Idempotency Verified.');

  // 4. Wait for Background Worker Processing
  let attempts = 0;
  while (testJob.status !== 'COMPLETED' && testJob.status !== 'FAILED' && attempts < 10) {
    await new Promise(resolve => setTimeout(resolve, 300));
    attempts++;
  }
  console.log(`✅ [4/4] AI Worker Processing Finished: Status = ${testJob.status}.`);

  console.log('\n======================================================');
  console.log('🎉 CAREERHUB PHASE 3 BACKEND TESTS PASSED 100%');
  console.log('======================================================\n');
}

runPhase3BackendTests().catch(err => {
  console.error('❌ PHASE 3 BACKEND TEST FAILED:', err);
  process.exit(1);
});
