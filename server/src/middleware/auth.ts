import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';

let ephemeralDevSecret: string | null = null;

export function getJwtSecret(): string {
  const secret = process.env.JWT_SECRET?.trim();
  if (secret && secret.length >= 16) {
    return secret;
  }

  if (process.env.NODE_ENV === 'production') {
    throw new Error('FATAL: JWT_SECRET environment variable is missing or insecure in production. Minimum 16 characters required.');
  }

  // Development/Test ephemeral random key generated once per server instance (never hardcoded, never predictable)
  if (!ephemeralDevSecret) {
    ephemeralDevSecret = crypto.randomBytes(32).toString('hex');
  }
  return ephemeralDevSecret;
}

export function validateAuthConfig(): void {
  if (process.env.NODE_ENV === 'production') {
    const secret = process.env.JWT_SECRET?.trim();
    if (!secret || secret.length < 16) {
      console.error('[FATAL CONFIG ERROR] JWT_SECRET must be set with at least 16 characters in production.');
      process.exit(1);
    }
  }
}

export interface AuthenticatedUser {
  id: string;
  email: string;
  role: string;
}

export interface AuthenticatedRequest extends Request {
  user?: AuthenticatedUser;
}

export function generateToken(user: AuthenticatedUser): string {
  return jwt.sign(
    { id: user.id, email: user.email, role: user.role },
    getJwtSecret(),
    { expiresIn: '7d' }
  );
}

export function authenticate(req: AuthenticatedRequest, res: Response, next: NextFunction) {
  const authHeader = req.headers.authorization;

  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({
      error: {
        code: 'UNAUTHORIZED',
        message: 'Authentication required. Please provide a valid Bearer token.'
      }
    });
  }

  const token = authHeader.split(' ')[1];

  try {
    const decoded = jwt.verify(token, getJwtSecret()) as AuthenticatedUser;
    if (!decoded || !decoded.id) {
      return res.status(401).json({
        error: {
          code: 'INVALID_TOKEN',
          message: 'Malformed or invalid authentication token.'
        }
      });
    }

    req.user = {
      id: decoded.id,
      email: decoded.email,
      role: decoded.role
    };

    next();
  } catch (err: any) {
    if (err.name === 'TokenExpiredError') {
      return res.status(401).json({
        error: {
          code: 'TOKEN_EXPIRED',
          message: 'Your session has expired. Please log in again.'
        }
      });
    }

    return res.status(401).json({
      error: {
        code: 'INVALID_TOKEN',
        message: 'Invalid authentication credentials.'
      }
    });
  }
}
