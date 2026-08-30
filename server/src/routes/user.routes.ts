import { Router, Response } from 'express';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { getDatabase } from '../db/database.js';

const router = Router();

// Delete User Account & All Associated Data (GDPR / Privacy Compliance)
router.delete('/delete', authenticate, (req: AuthenticatedRequest, res: Response) => {
  const userId = req.user?.id;
  if (!userId) {
    return res.status(401).json({ error: { code: 'UNAUTHORIZED', message: 'Authentication required' } });
  }

  const db = getDatabase();

  try {
    const deleteTransaction = db.transaction((uid: string) => {
      // Cascading deletion
      db.prepare('DELETE FROM resume_analysis WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM resumes WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM roadmap_items WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM roadmaps WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM projects WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM interview_answers WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM interview_questions WHERE interview_id IN (SELECT id FROM interviews WHERE user_id = ?)').run(uid);
      db.prepare('DELETE FROM interviews WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM learning_progress WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM user_skills WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM skill_gaps WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM career_goals WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM integrations WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM analytics_events WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM ai_generation_cache WHERE user_id = ?').run(uid);
      db.prepare('DELETE FROM profiles WHERE user_id = ?').run(uid);
      const userResult = db.prepare('DELETE FROM users WHERE id = ?').run(uid);

      return userResult.changes;
    });

    const changes = deleteTransaction(userId);
    if (changes === 0) {
      return res.status(404).json({ error: { code: 'USER_NOT_FOUND', message: 'User account not found.' } });
    }

    console.log(`[Privacy] User ${userId} and all associated data permanently erased.`);
    return res.json({
      success: true,
      deletedUserId: userId,
      message: 'User account and all associated career records permanently deleted.',
      timestamp: new Date().toISOString()
    });
  } catch (err: any) {
    return res.status(500).json({ error: { code: 'DELETION_FAILED', message: err.message } });
  }
});

export default router;
