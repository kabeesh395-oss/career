import { Router, Response } from 'express';
import { z } from 'zod';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';
import { ReadinessService } from '../services/readiness.service.js';

const router = Router();
router.use(authenticate);

const HistoryQuerySchema = z.object({
  limit: z.coerce.number().int().min(1).max(100).default(20)
});

// GET /api/readiness/current
router.get('/current', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const snapshot = ReadinessService.getCurrent(userId);
    const nextAction = ReadinessService.getNextAction(userId);
    return res.json({ snapshot, nextAction });
  } catch (err) {
    next(err);
  }
});

// GET /api/readiness/history
router.get('/history', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const { limit } = HistoryQuerySchema.parse(req.query);
    return res.json({ snapshots: ReadinessService.getHistory(req.user!.id, limit) });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({ error: { code: 'VALIDATION_ERROR', message: 'Invalid history limit.' } });
    }
    next(err);
  }
});

// POST /api/readiness/recalculate
router.post('/recalculate', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const sourceEventId = `readiness_recalculated_${Date.now()}`;
    const snapshot = ReadinessService.calculateAndPersist(userId, sourceEventId);
    AnalyticsService.trackEvent(userId, 'readiness_recalculated', {
      scoreVersion: snapshot.scoreVersion,
      overallScore: snapshot.overallScore,
      overallConfidence: snapshot.overallConfidence
    }, sourceEventId);
    return res.status(201).json({ snapshot, nextAction: ReadinessService.getNextAction(userId) });
  } catch (err) {
    next(err);
  }
});

// GET /api/readiness/evidence
router.get('/evidence', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    return res.json({ evidenceCoverage: ReadinessService.getEvidence(req.user!.id) });
  } catch (err) {
    next(err);
  }
});

// GET /api/readiness/next-action
router.get('/next-action', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    return res.json({ nextAction: ReadinessService.getNextAction(req.user!.id) });
  } catch (err) {
    next(err);
  }
});

export default router;
