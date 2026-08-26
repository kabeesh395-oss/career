import { Router, Response } from 'express';
import { z } from 'zod';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const UpdateProgressSchema = z.object({
  resourceId: z.string().min(1, 'Resource ID is required'),
  status: z.enum(['not_started', 'started', 'completed']),
  progressPercent: z.number().min(0).max(100).optional()
});

// GET /api/learning/resources
router.get('/resources', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const resources = db.prepare(`
      SELECT 
        lr.*,
        COALESCE(lp.status, 'not_started') as user_status,
        COALESCE(lp.progress_percent, 0) as user_progress,
        lp.started_at,
        lp.completed_at
      FROM learning_resources lr
      LEFT JOIN learning_progress lp ON lr.id = lp.resource_id AND lp.user_id = ?
      ORDER BY lr.category, lr.title
    `).all(userId);

    return res.json({ resources });
  } catch (err) {
    next(err);
  }
});

// POST /api/learning/progress
router.post('/progress', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { resourceId, status, progressPercent } = UpdateProgressSchema.parse(req.body);
    const db = getDatabase();

    const resource = db.prepare('SELECT * FROM learning_resources WHERE id = ?').get(resourceId);
    if (!resource) {
      return res.status(404).json({
        error: { code: 'RESOURCE_NOT_FOUND', message: 'Learning resource not found.' }
      });
    }

    const calculatedPercent = progressPercent ?? (status === 'completed' ? 100 : status === 'started' ? 50 : 0);
    const startedAt = status !== 'not_started' ? new Date().toISOString() : null;
    const completedAt = status === 'completed' ? new Date().toISOString() : null;
    const progressId = `lpr_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    db.prepare(`
      INSERT INTO learning_progress (id, user_id, resource_id, status, progress_percent, started_at, completed_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)
      ON CONFLICT(user_id, resource_id) DO UPDATE SET
        status = excluded.status,
        progress_percent = excluded.progress_percent,
        started_at = COALESCE(learning_progress.started_at, excluded.started_at),
        completed_at = excluded.completed_at,
        updated_at = CURRENT_TIMESTAMP
    `).run(progressId, userId, resourceId, status, calculatedPercent, startedAt, completedAt);

    if (status === 'completed') {
      AnalyticsService.trackEvent(userId, 'learning_resource_completed', { resourceId, title: (resource as any).title });
    }

    const updated = db.prepare(`
      SELECT 
        lr.*,
        lp.status as user_status,
        lp.progress_percent as user_progress,
        lp.started_at,
        lp.completed_at
      FROM learning_resources lr
      JOIN learning_progress lp ON lr.id = lp.resource_id AND lp.user_id = ?
      WHERE lr.id = ?
    `).get(userId, resourceId);

    return res.json({ resource: updated });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

export default router;
