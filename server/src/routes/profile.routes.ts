import { Router, Response } from 'express';
import { z } from 'zod';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const ProfileUpdateSchema = z.object({
  headline: z.string().optional(),
  bio: z.string().optional(),
  location: z.string().optional(),
  education: z.string().optional(),
  experience_years: z.number().min(0).max(50).optional(),
  target_role: z.string().min(2, 'Target role is required').optional(),
  target_industry: z.string().optional(),
  target_salary: z.string().optional()
});

const AddSkillSchema = z.object({
  skillId: z.string().min(1, 'Skill ID is required'),
  proficiencyLevel: z.number().min(1).max(5)
});

// GET /api/profile
router.get('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId);
    const goals = db.prepare('SELECT * FROM career_goals WHERE user_id = ? ORDER BY created_at DESC').all(userId);
    
    // User skills joined with canonical skill metadata
    const skills = db.prepare(`
      SELECT us.id, us.skill_id, us.proficiency_level, us.verified, us.source, s.name, s.category, s.demand_weight
      FROM user_skills us
      JOIN skills s ON us.skill_id = s.id
      WHERE us.user_id = ?
      ORDER BY us.proficiency_level DESC
    `).all(userId);

    return res.json({
      profile,
      goals,
      skills
    });
  } catch (err) {
    next(err);
  }
});

// PUT /api/profile
router.put('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const body = ProfileUpdateSchema.parse(req.body);
    const db = getDatabase();

    const existing = db.prepare('SELECT id FROM profiles WHERE user_id = ?').get(userId);
    if (!existing) {
      const profileId = `prf_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
      db.prepare(`
        INSERT INTO profiles (id, user_id, headline, bio, location, education, experience_years, target_role, target_industry, target_salary, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
      `).run(
        profileId,
        userId,
        body.headline || '',
        body.bio || '',
        body.location || '',
        body.education || '',
        body.experience_years ?? 1,
        body.target_role || 'Full Stack Engineer',
        body.target_industry || 'Tech',
        body.target_salary || ''
      );
    } else {
      db.prepare(`
        UPDATE profiles
        SET headline = COALESCE(?, headline),
            bio = COALESCE(?, bio),
            location = COALESCE(?, location),
            education = COALESCE(?, education),
            experience_years = COALESCE(?, experience_years),
            target_role = COALESCE(?, target_role),
            target_industry = COALESCE(?, target_industry),
            target_salary = COALESCE(?, target_salary),
            updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ?
      `).run(
        body.headline,
        body.bio,
        body.location,
        body.education,
        body.experience_years,
        body.target_role,
        body.target_industry,
        body.target_salary,
        userId
      );
    }

    const updatedProfile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId);
    AnalyticsService.trackEvent(userId, 'profile_updated', { target_role: body.target_role });

    return res.json({ profile: updatedProfile });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

// POST /api/profile/skills (Upsert user skill)
router.post('/skills', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { skillId, proficiencyLevel } = AddSkillSchema.parse(req.body);
    const db = getDatabase();

    const skillExists = db.prepare('SELECT id FROM skills WHERE id = ?').get(skillId);
    if (!skillExists) {
      return res.status(404).json({
        error: { code: 'SKILL_NOT_FOUND', message: `Canonical skill with ID ${skillId} does not exist.` }
      });
    }

    const id = `usk_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    db.prepare(`
      INSERT INTO user_skills (id, user_id, skill_id, proficiency_level, verified, source, updated_at)
      VALUES (?, ?, ?, ?, 1, 'self_reported', CURRENT_TIMESTAMP)
      ON CONFLICT(user_id, skill_id) DO UPDATE SET
        proficiency_level = excluded.proficiency_level,
        updated_at = CURRENT_TIMESTAMP
    `).run(id, userId, skillId, proficiencyLevel);

    const userSkills = db.prepare(`
      SELECT us.id, us.skill_id, us.proficiency_level, us.verified, us.source, s.name, s.category, s.demand_weight
      FROM user_skills us
      JOIN skills s ON us.skill_id = s.id
      WHERE us.user_id = ?
      ORDER BY us.proficiency_level DESC
    `).all(userId);

    AnalyticsService.trackEvent(userId, 'skill_added', { skillId, proficiencyLevel });

    return res.status(201).json({ skills: userSkills });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

// DELETE /api/profile/skills/:skillId
router.delete('/skills/:skillId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { skillId } = req.params;
    const db = getDatabase();

    db.prepare('DELETE FROM user_skills WHERE user_id = ? AND (skill_id = ? OR id = ?)').run(userId, skillId, skillId);

    const userSkills = db.prepare(`
      SELECT us.id, us.skill_id, us.proficiency_level, us.verified, us.source, s.name, s.category, s.demand_weight
      FROM user_skills us
      JOIN skills s ON us.skill_id = s.id
      WHERE us.user_id = ?
      ORDER BY us.proficiency_level DESC
    `).all(userId);

    return res.json({ skills: userSkills });
  } catch (err) {
    next(err);
  }
});

// POST /api/profile/onboarding
router.post('/onboarding', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { targetRole, experienceYears, selectedSkillIds } = req.body;
    const db = getDatabase();

    const onboardingTx = db.transaction(() => {
      // Update profile
      db.prepare(`
        UPDATE profiles 
        SET target_role = ?, experience_years = ?, onboarding_completed = 1, updated_at = CURRENT_TIMESTAMP
        WHERE user_id = ?
      `).run(targetRole || 'Full Stack Engineer', experienceYears || 1, userId);

      // Insert skills if provided
      if (Array.isArray(selectedSkillIds)) {
        for (const skillId of selectedSkillIds) {
          const id = `usk_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
          db.prepare(`
            INSERT OR IGNORE INTO user_skills (id, user_id, skill_id, proficiency_level, source, updated_at)
            VALUES (?, ?, ?, 3, 'onboarding', CURRENT_TIMESTAMP)
          `).run(id, userId, skillId);
        }
      }
    });

    onboardingTx();

    AnalyticsService.trackEvent(userId, 'onboarding_completed', { targetRole, experienceYears });

    const updatedProfile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId);
    return res.json({ profile: updatedProfile });
  } catch (err) {
    next(err);
  }
});

export default router;
