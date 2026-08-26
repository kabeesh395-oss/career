import { Router, Response } from 'express';
import bcrypt from 'bcryptjs';
import crypto from 'crypto';
import { z } from 'zod';
import { getDatabase } from '../db/database.js';
import { generateToken, authenticate, AuthenticatedRequest } from '../middleware/auth.js';
import { AnalyticsService } from '../services/analytics.service.js';

const router = Router();

const SignupSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(6, 'Password must be at least 6 characters'),
  fullName: z.string().min(2, 'Full name is required')
});

const LoginSchema = z.object({
  email: z.string().email('Invalid email address'),
  password: z.string().min(1, 'Password is required')
});

// POST /api/auth/signup
router.post('/signup', async (req, res, next) => {
  try {
    const { email, password, fullName } = SignupSchema.parse(req.body);
    const db = getDatabase();

    const normalizedEmail = email.toLowerCase().trim();

    // Check if user already exists
    const existing = db.prepare('SELECT id FROM users WHERE email = ?').get(normalizedEmail);
    if (existing) {
      return res.status(409).json({
        error: {
          code: 'USER_EXISTS',
          message: 'An account with this email address already exists.'
        }
      });
    }

    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);
    const userId = `usr_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;
    const profileId = `prf_${Date.now()}_${crypto.randomBytes(4).toString('hex')}`;

    // Transaction for atomic user creation
    const createUserTx = db.transaction(() => {
      db.prepare(`
        INSERT INTO users (id, email, password_hash, full_name, role, created_at, updated_at)
        VALUES (?, ?, ?, ?, 'user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(userId, normalizedEmail, passwordHash, fullName.trim());

      db.prepare(`
        INSERT INTO profiles (id, user_id, headline, bio, experience_years, target_role, onboarding_completed, created_at, updated_at)
        VALUES (?, ?, 'Software Engineer', '', 1, 'Full Stack Engineer', 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
      `).run(profileId, userId);
    });

    createUserTx();

    const userPayload = { id: userId, email: normalizedEmail, role: 'user' };
    const token = generateToken(userPayload);

    AnalyticsService.trackEvent(userId, 'user_signup', { email: normalizedEmail, fullName });

    return res.status(201).json({
      user: {
        id: userId,
        email: normalizedEmail,
        fullName: fullName.trim(),
        role: 'user'
      },
      token
    });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: {
          code: 'VALIDATION_ERROR',
          message: err.errors.map((e: any) => e.message).join(', ')
        }
      });
    }
    next(err);
  }
});

// POST /api/auth/login
router.post('/login', async (req, res, next) => {
  try {
    const { email, password } = LoginSchema.parse(req.body);
    const db = getDatabase();

    const normalizedEmail = email.toLowerCase().trim();
    const user = db.prepare('SELECT * FROM users WHERE email = ?').get(normalizedEmail) as any;

    if (!user) {
      return res.status(401).json({
        error: {
          code: 'INVALID_CREDENTIALS',
          message: 'Invalid email or password.'
        }
      });
    }

    const isMatch = await bcrypt.compare(password, user.password_hash);
    if (!isMatch) {
      return res.status(401).json({
        error: {
          code: 'INVALID_CREDENTIALS',
          message: 'Invalid email or password.'
        }
      });
    }

    const userPayload = { id: user.id, email: user.email, role: user.role };
    const token = generateToken(userPayload);

    AnalyticsService.trackEvent(user.id, 'user_login', { email: user.email });

    return res.json({
      user: {
        id: user.id,
        email: user.email,
        fullName: user.full_name,
        role: user.role,
        avatarUrl: user.avatar_url
      },
      token
    });
  } catch (err: any) {
    if (err.name === 'ZodError') {
      return res.status(422).json({
        error: {
          code: 'VALIDATION_ERROR',
          message: err.errors.map((e: any) => e.message).join(', ')
        }
      });
    }
    next(err);
  }
});

// GET /api/auth/me (Session restoration)
router.get('/me', authenticate, (req: AuthenticatedRequest, res: Response, next) => {
  try {
    const userId = req.user!.id;
    const db = getDatabase();

    const user = db.prepare('SELECT id, email, full_name, role, avatar_url, created_at FROM users WHERE id = ?').get(userId) as any;
    if (!user) {
      return res.status(404).json({
        error: {
          code: 'USER_NOT_FOUND',
          message: 'User session not found in database.'
        }
      });
    }

    const profile = db.prepare('SELECT * FROM profiles WHERE user_id = ?').get(userId) as any;

    return res.json({
      user: {
        id: user.id,
        email: user.email,
        fullName: user.full_name,
        role: user.role,
        avatarUrl: user.avatar_url,
        createdAt: user.created_at
      },
      profile: profile || null
    });
  } catch (err) {
    next(err);
  }
});

export default router;
