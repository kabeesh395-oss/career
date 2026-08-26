import { Router, Response } from 'express';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AIService } from '../services/ai.service.js';
import { NextBestActionService } from '../services/nextBestAction.service.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

// GET /api/career/skills-catalog
router.get('/skills-catalog', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const db = getDatabase();
    const skills = db.prepare('SELECT * FROM skills ORDER BY category, name').all();
    return res.json({ skills });
  } catch (err) {
    next(err);
  }
});

// GET /api/career/skill-gaps
router.get('/skill-gaps', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const gaps = db.prepare('SELECT * FROM skill_gaps WHERE user_id = ? ORDER BY gap_score DESC, priority ASC').all(userId);
    return res.json({ gaps });
  } catch (err) {
    next(err);
  }
});

// POST /api/career/analyze (Runs Career Readiness & Skill Gap Analysis)
router.post('/analyze', async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    const targetRole = req.body.targetRole || profile?.target_role || 'Full Stack Engineer';
    const experienceYears = profile?.experience_years || 1;

    // Fetch user skills
    const userSkills = db.prepare(`
      SELECT s.name, us.proficiency_level
      FROM user_skills us
      JOIN skills s ON us.skill_id = s.id
      WHERE us.user_id = ?
    `).all(userId) as Array<{ name: string; proficiency_level: number }>;

    // Perform real AI / heuristic analysis
    const analysis = await AIService.analyzeCareerReadinessAndGaps(userSkills, targetRole, experienceYears);

    // Persist skill gaps & readiness score in transaction
    const saveAnalysisTx = db.transaction(() => {
      // Clear previous gaps for this target role
      db.prepare('DELETE FROM skill_gaps WHERE user_id = ? AND target_role = ?').run(userId, targetRole);

      // Insert new gaps
      const insertGapStmt = db.prepare(`
        INSERT INTO skill_gaps (id, user_id, target_role, skill_name, category, required_level, current_level, gap_score, priority, recommendation, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
      `);

      for (const gap of analysis.skillGaps) {
        const gapId = `gap_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
        insertGapStmt.run(
          gapId,
          userId,
          targetRole,
          gap.skillName,
          gap.category,
          gap.requiredLevel,
          gap.currentLevel,
          gap.gapScore,
          gap.priority,
          gap.recommendation
        );
      }

      // Update profile readiness score and target role
      db.prepare(`
        UPDATE profiles 
        SET current_readiness_score = ?, target_role = ?, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ?
      `).run(analysis.readinessScore, targetRole, userId);
    });

    saveAnalysisTx();

    AnalyticsService.trackEvent(userId, 'career_analysis_generated', {
      targetRole,
      readinessScore: analysis.readinessScore,
      skillGapsCount: analysis.skillGaps.length,
      modelUsed: analysis.modelUsed
    });

    return res.json({
      targetRole,
      readinessScore: analysis.readinessScore,
      marketDemandRating: analysis.marketDemandRating,
      roleRequirementsSummary: analysis.roleRequirementsSummary,
      skillGaps: analysis.skillGaps,
      modelUsed: analysis.modelUsed
    });
  } catch (err) {
    next(err);
  }
});

// GET /api/career/next-best-action
router.get('/next-best-action', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const action = NextBestActionService.calculateNextBestAction(userId);
    return res.json({ action });
  } catch (err) {
    next(err);
  }
});

export default router;
