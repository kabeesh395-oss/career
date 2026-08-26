import { Router, Response } from 'express';
import { z } from 'zod';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { GitHubService } from '../services/github.service.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const GitHubConnectSchema = z.object({
  username: z.string().min(1, 'GitHub username is required')
});

// GET /api/integrations
router.get('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const integrations = db.prepare('SELECT * FROM integrations WHERE user_id = ?').all(userId);
    const formatted = integrations.map((i: any) => ({
      provider: i.provider,
      username: i.profile_username,
      isConnected: Boolean(i.is_connected),
      data: i.profile_data_json ? JSON.parse(i.profile_data_json) : null,
      lastSyncedAt: i.last_synced_at
    }));

    return res.json({ integrations: formatted });
  } catch (err) {
    next(err);
  }
});

// POST /api/integrations/github/connect (Fetches real GitHub data)
router.post('/github/connect', async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { username } = GitHubConnectSchema.parse(req.body);
    const db = getDatabase();

    // Query real GitHub API
    const analysis = await GitHubService.fetchAndAnalyzeGitHubProfile(username);
    const integrationId = `int_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    db.prepare(`
      INSERT INTO integrations (id, user_id, provider, profile_username, profile_data_json, is_connected, last_synced_at, updated_at)
      VALUES (?, ?, 'github', ?, ?, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      ON CONFLICT(user_id, provider) DO UPDATE SET
        profile_username = excluded.profile_username,
        profile_data_json = excluded.profile_data_json,
        is_connected = 1,
        last_synced_at = CURRENT_TIMESTAMP,
        updated_at = CURRENT_TIMESTAMP
    `).run(integrationId, userId, analysis.username, JSON.stringify(analysis));

    AnalyticsService.trackEvent(userId, 'github_connected', {
      username: analysis.username,
      repoCount: analysis.publicRepoCount,
      stars: analysis.totalStars
    });

    return res.json({
      integration: {
        provider: 'github',
        username: analysis.username,
        isConnected: true,
        data: analysis,
        lastSyncedAt: analysis.syncedAt
      }
    });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    return res.status(400).json({
      error: { code: 'GITHUB_FETCH_FAILED', message: err.message }
    });
  }
});

// DELETE /api/integrations/:provider
router.delete('/:provider', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { provider } = req.params;
    const db = getDatabase();

    db.prepare('DELETE FROM integrations WHERE user_id = ? AND provider = ?').run(userId, provider);

    AnalyticsService.trackEvent(userId, 'integration_disconnected', { provider });

    return res.json({ success: true, message: `${provider} integration disconnected.` });
  } catch (err) {
    next(err);
  }
});

export default router;
