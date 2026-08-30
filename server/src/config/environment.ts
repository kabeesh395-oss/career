import dotenv from 'dotenv';

dotenv.config();

export interface AppConfig {
  env: 'development' | 'staging' | 'production';
  port: number;
  jwtSecret: string;
  geminiApiKey: string;
  databasePath: string;
  corsOrigin: string;
  rateLimitMax: number;
  aiRateLimitMax: number;
}

function loadConfig(): AppConfig {
  const env = (process.env.NODE_ENV as AppConfig['env']) || 'development';
  const isProd = env === 'production';

  const jwtSecret = process.env.JWT_SECRET || (isProd ? '' : 'careerpilot-production-secret-key-2026-secure-jwt');
  const geminiApiKey = process.env.GEMINI_API_KEY || '';

  // Fail fast in production if critical secrets are missing
  if (isProd && !jwtSecret) {
    throw new Error('[FATAL] JWT_SECRET must be defined in production environment variables.');
  }

  return {
    env,
    port: parseInt(process.env.PORT || '5000', 10),
    jwtSecret,
    geminiApiKey,
    databasePath: process.env.DATABASE_PATH || './careerpilot.db',
    corsOrigin: process.env.CORS_ORIGIN || '*',
    rateLimitMax: parseInt(process.env.RATE_LIMIT_MAX || '100', 10),
    aiRateLimitMax: parseInt(process.env.AI_RATE_LIMIT_MAX || '10', 10)
  };
}

export const config = loadConfig();
