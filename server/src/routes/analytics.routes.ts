import { Router, Response } from 'express';
import { z } from 'zod';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const TrackEventSchema = z.object({
  eventName: z.string().min(1, 'Event name is required'),
  eventData: z.record(z.any()).optional(),
  idempotencyKey: z.string().optional()
});

// GET /api/analytics/dashboard
router.get('/dashboard', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const analytics = AnalyticsService.getUserDashboardAnalytics(userId);
    return res.json({ analytics });
  } catch (err) {
    next(err);
  }
});

// POST /api/analytics/event
router.post('/event', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { eventName, eventData, idempotencyKey } = TrackEventSchema.parse(req.body);

    AnalyticsService.trackEvent(userId, eventName, eventData || {}, idempotencyKey);
    return res.status(201).json({ success: true });
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
