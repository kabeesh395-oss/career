import { Router, Response } from 'express';
import { z } from 'zod';
import crypto from 'crypto';
import { getDatabase } from '../db/database.js';
import { authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();
router.use(authenticate);

const ProjectSchema = z.object({
  title: z.string().min(2, 'Project title must be at least 2 characters'),
  description: z.string().optional(),
  repository_url: z.string().url('Invalid repository URL').optional().or(z.literal('')),
  live_url: z.string().url('Invalid live URL').optional().or(z.literal('')),
  status: z.enum(['planning', 'in_progress', 'completed']).default('in_progress'),
  technologies: z.string().optional(),
  skills_targeted: z.string().optional()
});

// GET /api/projects
router.get('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const projects = db.prepare('SELECT * FROM projects WHERE user_id = ? ORDER BY created_at DESC').all(userId);
    return res.json({ projects });
  } catch (err) {
    next(err);
  }
});

// POST /api/projects
router.post('/', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const body = ProjectSchema.parse(req.body);
    const db = getDatabase();

    const projectId = `prj_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    db.prepare(`
      INSERT INTO projects (id, user_id, title, description, repository_url, live_url, status, technologies, skills_targeted, created_at, updated_at)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    `).run(
      projectId,
      userId,
      body.title,
      body.description || '',
      body.repository_url || '',
      body.live_url || '',
      body.status,
      body.technologies || '',
      body.skills_targeted || ''
    );

    AnalyticsService.trackEvent(userId, 'project_created', { projectId, title: body.title });

    const newProject = db.prepare('SELECT * FROM projects WHERE id = ?').get(projectId);
    return res.status(201).json({ project: newProject });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

// PUT /api/projects/:projectId
router.put('/:projectId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { projectId } = req.params;
    const body = ProjectSchema.parse(req.body);
    const db = getDatabase();

    const existing = db.prepare('SELECT id FROM projects WHERE id = ? AND user_id = ?').get(projectId, userId);
    if (!existing) {
      return res.status(404).json({
        error: { code: 'PROJECT_NOT_FOUND', message: 'Project not found or you do not have permission to edit it.' }
      });
    }

    db.prepare(`
      UPDATE projects 
      SET title = ?, description = ?, repository_url = ?, live_url = ?, status = ?, technologies = ?, skills_targeted = ?, updated_at = CURRENT_TIMESTAMP
      WHERE id = ? AND user_id = ?
    `).run(
      body.title,
      body.description || '',
      body.repository_url || '',
      body.live_url || '',
      body.status,
      body.technologies || '',
      body.skills_targeted || '',
      projectId,
      userId
    );

    const updatedProject = db.prepare('SELECT * FROM projects WHERE id = ?').get(projectId);
    return res.json({ project: updatedProject });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: { code: 'VALIDATION_ERROR', message: err.errors.map((e: any) => e.message).join(', ') }
      });
    }
    next(err);
  }
});

// DELETE /api/projects/:projectId
router.delete('/:projectId', (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const { projectId } = req.params;
    const db = getDatabase();

    const existing = db.prepare('SELECT id FROM projects WHERE id = ? AND user_id = ?').get(projectId, userId);
    if (!existing) {
      return res.status(404).json({
        error: { code: 'PROJECT_NOT_FOUND', message: 'Project not found or you do not have permission to delete it.' }
      });
    }

    db.prepare('DELETE FROM projects WHERE id = ? AND user_id = ?').run(projectId, userId);
    return res.json({ success: true, message: 'Project deleted successfully.' });
  } catch (err) {
    next(err);
  }
});

export default router;
