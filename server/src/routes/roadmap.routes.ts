import { Router, Response } from 'express';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AIService } from '../services/ai.service.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

// GET /api/roadmap
router.get('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const roadmap = db.prepare('SELECT * FROM roadmaps WHERE user_id = ? ORDER BY created_at DESC LIMIT 1').get(userId) as any;
    if (!roadmap) {
      return res.json({ roadmap: null, items: [] });
    }

    const items = db.prepare(`
      SELECT * FROM roadmap_items 
      WHERE roadmap_id = ? AND user_id = ? 
      ORDER BY phase_number ASC, order_index ASC
    `).all(roadmap.id, userId);

    return res.json({ roadmap, items });
  } catch (err) {
    next(err);
  }
});

// POST /api/roadmap/generate
router.post('/generate', async (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;
    const targetRole = req.body.targetRole || profile?.target_role || 'Full Stack Engineer';

    // Fetch existing skill gaps for user
    let skillGaps = db.prepare(`
      SELECT skill_name as skillName, required_level as requiredLevel, current_level as currentLevel, priority
      FROM skill_gaps 
      WHERE user_id = ? AND target_role = ?
    `).all(userId, targetRole) as Array<{ skillName: string; requiredLevel: number; currentLevel: number; priority: string }>;

    // If no skill gaps exist yet, generate them first
    if (skillGaps.length === 0) {
      const userSkills = db.prepare(`
        SELECT s.name, us.proficiency_level
        FROM user_skills us
        JOIN skills s ON us.skill_id = s.id
        WHERE us.user_id = ?
      `).all(userId) as Array<{ name: string; proficiency_level: number }>;

      const analysis = await AIService.analyzeCareerReadinessAndGaps(userSkills, targetRole, profile?.experience_years || 1);
      skillGaps = analysis.skillGaps.map(g => ({
        skillName: g.skillName,
        requiredLevel: g.requiredLevel,
        currentLevel: g.currentLevel,
        priority: g.priority
      }));
    }

    // Generate personalized roadmap
    const generated = await AIService.generatePersonalizedRoadmap(targetRole, skillGaps);

    const roadmapId = `rdm_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    let totalTasks = 0;

    const createRoadmapTx = db.transaction(() => {
      // Archive or remove old roadmaps for user
      db.prepare('DELETE FROM roadmaps WHERE user_id = ?').run(userId);
      db.prepare('DELETE FROM roadmap_items WHERE user_id = ?').run(userId);

      const insertItemStmt = db.prepare(`
        INSERT INTO roadmap_items (
          id, roadmap_id, user_id, phase_number, phase_title, title, description, category, estimated_hours, order_index, status, created_at, updated_at
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'pending', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `);

      for (const phase of generated.phases) {
        for (const item of phase.items) {
          totalTasks++;
          const itemId = `rit_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
          insertItemStmt.run(
            itemId,
            roadmapId,
            userId,
            phase.phaseNumber,
            phase.phaseTitle,
            item.title,
            item.description,
            item.category,
            item.estimatedHours,
            item.orderIndex
          );
        }
      }

      db.prepare(`
        INSERT INTO roadmaps (id, user_id, title, target_role, summary, total_tasks, completed_tasks, progress_percent, status, created_at, updated_at)
        VALUES (?, ?, ?, ?, ?, ?, 0, 0.0, 'in_progress', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(roadmapId, userId, generated.title, targetRole, generated.summary, totalTasks);
    });

    createRoadmapTx();

    AnalyticsService.trackEvent(userId, 'roadmap_generated', {
      roadmapId,
      targetRole,
      totalTasks,
      modelUsed: generated.modelUsed
    });

    const activeRoadmap = db.prepare('SELECT * FROM roadmaps WHERE id = ?').get(roadmapId);
    const items = db.prepare('SELECT * FROM roadmap_items WHERE roadmap_id = ? ORDER BY phase_number ASC, order_index ASC').all(roadmapId);

    return res.status(201).json({
      roadmap: activeRoadmap,
      items
    });
  } catch (err) {
    next(err);
  }
});

// PATCH /api/roadmap/items/:itemId (Toggle task completion)
router.patch('/items/:itemId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { itemId } = req.params;
    const { status } = req.body; // 'completed' | 'pending' | 'in_progress'
    const db = getDatabase();

    // Verify ownership
    const item = db.prepare('SELECT * FROM roadmap_items WHERE id = ? AND user_id = ?').get(itemId, userId) as any;
    if (!item) {
      return res.status(404).json({
        error: { code: 'TASK_NOT_FOUND', message: 'Roadmap task not found or you do not have permission to modify it.' }
      });
    }

    const newStatus = status || (item.status === 'completed' ? 'pending' : 'completed');
    const completedAt = newStatus === 'completed' ? new Date().toISOString() : null;

    const toggleTx = db.transaction(() => {
      db.prepare(`
        UPDATE roadmap_items 
        SET status = ?, completed_at = ?, updated_at = CURRENT_TIMESTAMP 
        WHERE id = ? AND user_id = ?
      `).run(newStatus, completedAt, itemId, userId);

      // Recalculate roadmap progress accurately
      const stats = db.prepare(`
        SELECT 
          COUNT(*) as total,
          SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed
        FROM roadmap_items 
        WHERE roadmap_id = ? AND user_id = ?
      `).get(item.roadmap_id, userId) as { total: number; completed: number };

      const progressPercent = stats.total > 0 ? (stats.completed / stats.total) * 100 : 0;
      const roadmapStatus = stats.completed === stats.total ? 'completed' : 'in_progress';

      db.prepare(`
        UPDATE roadmaps 
        SET total_tasks = ?, completed_tasks = ?, progress_percent = ?, status = ?, updated_at = CURRENT_TIMESTAMP 
        WHERE id = ? AND user_id = ?
      `).run(stats.total, stats.completed, progressPercent, roadmapStatus, item.roadmap_id, userId);
    });

    toggleTx();

    if (newStatus === 'completed') {
      AnalyticsService.trackEvent(userId, 'task_completed', { itemId, taskTitle: item.title });
    }

    const updatedItem = db.prepare('SELECT * FROM roadmap_items WHERE id = ?').get(itemId);
    const updatedRoadmap = db.prepare('SELECT * FROM roadmaps WHERE id = ?').get(item.roadmap_id);

    return res.json({
      item: updatedItem,
      roadmap: updatedRoadmap
    });
  } catch (err) {
    next(err);
  }
});

export default router;
