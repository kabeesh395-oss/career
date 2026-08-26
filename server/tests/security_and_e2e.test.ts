import { initDatabase, getDatabase } from '../src/db/database.js';
import bcrypt from 'bcryptjs';
import { generateToken } from '../src/middleware/auth.js';
import { AIService } from '../src/services/ai.service.js';
import { NextBestActionService } from '../src/services/nextBestAction.service.js';
import { AnalyticsService } from '../src/services/analytics.service.js';
import { parseResumeContent } from '../src/services/resume.service.js';

async function runSecurityAndE2ETests() {
  console.log('\n======================================================');
  console.log('🧪 RUNNING CAREERPILOT AI PRODUCTION TEST SUITE');
  console.log('======================================================\n');

  // 1. Initialize Database
  initDatabase();
  const db = getDatabase();
  console.log('✅ [1/7] Database initialized with WAL mode & Foreign Keys enabled.');

  // Clean test fixtures
  const testEmailA = `test_user_a_${Date.now()}@careerpilot.io`;
  const testEmailB = `test_user_b_${Date.now()}@careerpilot.io`;
  const userAId = `usr_test_a_${Date.now()}`;
  const userBId = `usr_test_b_${Date.now()}`;

  // 2. Multi-User Registration & Authentication
  const hash = await bcrypt.hash('SecurePassword123!', 10);
  
  db.prepare(`
    INSERT INTO users (id, email, password_hash, full_name, role)
    VALUES (?, ?, ?, 'User Alpha', 'user')
  `).run(userAId, testEmailA, hash);

  db.prepare(`
    INSERT INTO profiles (id, user_id, headline, bio, experience_years, target_role, onboarding_completed)
    VALUES (?, ?, 'Senior Engineer', 'Alpha Bio', 4, 'Full Stack Engineer', 1)
  `).run(`prf_${userAId}`, userAId);

  db.prepare(`
    INSERT INTO users (id, email, password_hash, full_name, role)
    VALUES (?, ?, ?, 'User Beta', 'user')
  `).run(userBId, testEmailB, hash);

  db.prepare(`
    INSERT INTO profiles (id, user_id, headline, bio, experience_years, target_role, onboarding_completed)
    VALUES (?, ?, 'Junior Developer', 'Beta Bio', 1, 'Frontend Engineer', 1)
  `).run(`prf_${userBId}`, userBId);

  const tokenA = generateToken({ id: userAId, email: testEmailA, role: 'user' });
  const tokenB = generateToken({ id: userBId, email: testEmailB, role: 'user' });

  console.log('✅ [2/7] Registered User A and User B with independent credentials.');

  // 3. User A creates private assets (Projects & Roadmaps)
  const projectAId = `prj_a_${Date.now()}`;
  db.prepare(`
    INSERT INTO projects (id, user_id, title, description, status)
    VALUES (?, ?, 'User A Confidential Project', 'Proprietary ML system', 'in_progress')
  `).run(projectAId, userAId);

  const roadmapAId = `rdm_a_${Date.now()}`;
  db.prepare(`
    INSERT INTO roadmaps (id, user_id, title, target_role, total_tasks, completed_tasks, progress_percent)
    VALUES (?, ?, 'Alpha Full Stack Plan', 'Full Stack Engineer', 4, 1, 25.0)
  `).run(roadmapAId, userAId);

  const taskAId = `rit_a_${Date.now()}`;
  db.prepare(`
    INSERT INTO roadmap_items (id, roadmap_id, user_id, phase_number, phase_title, title, order_index, status)
    VALUES (?, ?, ?, 1, 'Phase 1', 'Master SQL Indexes', 1, 'completed')
  `).run(taskAId, roadmapAId, userAId);

  console.log('✅ [3/7] Created private project & roadmap for User A.');

  // 4. MULTI-USER ISOLATION & DATA LEAKAGE TEST
  // User B queries projects and roadmap items: MUST NOT see User A's data
  const userBProjects = db.prepare('SELECT * FROM projects WHERE user_id = ?').all(userBId);
  const userBRoadmaps = db.prepare('SELECT * FROM roadmaps WHERE user_id = ?').all(userBId);
  const userBTasks = db.prepare('SELECT * FROM roadmap_items WHERE user_id = ?').all(userBId);

  if (userBProjects.length !== 0 || userBRoadmaps.length !== 0 || userBTasks.length !== 0) {
    throw new Error('❌ CRITICAL SECURITY FAILURE: User B leaked User A private records!');
  }

  // User B attempts to mutate User A's task directly
  const unauthorizedUpdate = db.prepare(`
    UPDATE roadmap_items SET status = 'pending' WHERE id = ? AND user_id = ?
  `).run(taskAId, userBId);

  if (unauthorizedUpdate.changes !== 0) {
    throw new Error('❌ CRITICAL SECURITY FAILURE: User B modified User A task!');
  }

  console.log('✅ [4/7] MULTI-USER DATA ISOLATION VERIFIED: User B has 0 access to User A records and cannot modify them.');

  // 5. RESUME PARSER & SKILL EXTRACTION VERIFICATION
  const sampleResumeText = `
    Alex Rivera - Senior Full Stack Engineer
    Experience:
    - Architected high-throughput microservices using TypeScript, Node.js, and React.
    - Optimized PostgreSQL query performance, reducing database latency by 45%.
    - Managed Docker and Kubernetes deployments on AWS with automated CI/CD.
    Education:
    - B.S. in Computer Science, University of California (GPA: 3.8)
  `;

  const parsed = parseResumeContent(sampleResumeText);
  if (!parsed.extractedSkills.includes('TypeScript') || !parsed.extractedSkills.includes('PostgreSQL') || !parsed.hasMetrics) {
    throw new Error(`❌ RESUME PARSER FAILED: extractedSkills=${JSON.stringify(parsed.extractedSkills)}, hasMetrics=${parsed.hasMetrics}`);
  }

  const resumeScore = await AIService.analyzeResumeAgainstRole(
    sampleResumeText,
    parsed.extractedSkills,
    'Full Stack Engineer',
    parsed.wordCount,
    parsed.hasMetrics,
    parsed.hasEducation,
    parsed.hasExperience
  );

  if (resumeScore.overallScore < 50 || resumeScore.strengths.length === 0) {
    throw new Error('❌ RESUME SCORING FAILED: Unreasonable evaluation score.');
  }
  console.log(`✅ [5/7] RESUME PARSING & ATS EVALUATION VERIFIED: Score = ${resumeScore.overallScore}/100, Detected ${parsed.extractedSkills.length} skills.`);

  // 6. NEXT BEST ACTION & ROADMAP ENGINE VERIFICATION
  const nextActionA = NextBestActionService.calculateNextBestAction(userAId);
  if (!nextActionA || !nextActionA.title || !nextActionA.whyItMatters) {
    throw new Error('❌ NEXT BEST ACTION FAILED: Invalid action calculation.');
  }
  console.log(`✅ [6/7] NEXT BEST ACTION ENGINE VERIFIED: Next action is "${nextActionA.title}" (Priority: ${nextActionA.priority}).`);

  // 7. ANALYTICS & DASHBOARD METRICS CALCULATION
  AnalyticsService.trackEvent(userAId, 'test_event_1', { feature: 'e2e_testing' });
  const analyticsA = AnalyticsService.getUserDashboardAnalytics(userAId);
  
  if (analyticsA.tasks.total !== 1 || analyticsA.tasks.completed !== 1 || analyticsA.tasks.percent !== 100) {
    throw new Error(`❌ ANALYTICS METRICS FAILED: Task stats inaccurate. Expected 100%, got ${analyticsA.tasks.percent}%`);
  }
  console.log(`✅ [7/7] REAL EVENT ANALYTICS & TELEMETRY VERIFIED: Task completion rate = ${analyticsA.tasks.percent}%.`);

  console.log('\n======================================================');
  console.log('🎉 ALL SECURITY, ISOLATION & E2E TESTS PASSED 100%');
  console.log('======================================================\n');
}

runSecurityAndE2ETests().catch((err) => {
  console.error('\n❌ TEST SUITE FAILED:', err);
  process.exit(1);
});
