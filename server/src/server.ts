import express from 'express';
import cors from 'cors';
import helmet from 'helmet';
import path from 'path';
import { fileURLToPath } from 'url';
import dotenv from 'dotenv';

import { initDatabase, getDatabase } from './db/database.js';
import { errorHandler } from './middleware/error.js';
import { apiLimiter, expensiveAiLimiter } from './middleware/rateLimit.js';
import { validateAuthConfig } from './middleware/auth.js';
import { aiQueueManager } from './services/aiQueue.service.js';

import authRoutes from './routes/auth.routes.js';
import profileRoutes from './routes/profile.routes.js';
import careerRoutes from './routes/career.routes.js';
import roadmapRoutes from './routes/roadmap.routes.js';
import resumeRoutes from './routes/resume.routes.js';
import projectsRoutes from './routes/projects.routes.js';
import interviewRoutes from './routes/interview.routes.js';
import learningRoutes from './routes/learning.routes.js';
import integrationsRoutes from './routes/integrations.routes.js';
import analyticsRoutes from './routes/analytics.routes.js';
import userRoutes from './routes/user.routes.js';
import edgeAiRoutes from './routes/edgeAi.routes.js';

dotenv.config();
validateAuthConfig();

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const app = express();
const rawPort = process.env.PORT;
const PORT = rawPort && !isNaN(Number(rawPort)) ? Number(rawPort) : 5000;

// Initialize persistent SQLite Database
initDatabase();

// Security Headers (Helmet)
app.use(helmet({
  contentSecurityPolicy: false,
  crossOriginEmbedderPolicy: false
}));

// CORS Middleware - Configurable allowed origins
const configuredOrigin = process.env.CORS_ORIGIN;
const defaultAllowedOrigins = [
  'http://localhost:3000',
  'http://localhost:5173',
  'http://127.0.0.1:3000',
  'http://127.0.0.1:5173'
];
const allowedOrigins = configuredOrigin
  ? (configuredOrigin === '*' ? ['*'] : configuredOrigin.split(',').map(o => o.trim()))
  : defaultAllowedOrigins;

app.use(cors({
  origin: (origin, callback) => {
    // Allow non-browser requests (mobile native Capacitor, server-to-server, curl)
    if (!origin) return callback(null, true);
    if (allowedOrigins.includes('*')) return callback(null, true);
    if (process.env.NODE_ENV !== 'production' || allowedOrigins.includes(origin)) {
      return callback(null, true);
    }
    return callback(new Error(`CORS policy rejection: Origin ${origin} not allowed`));
  },
  credentials: true,
  methods: ['GET', 'POST', 'PUT', 'PATCH', 'DELETE', 'OPTIONS'],
  allowedHeaders: ['Content-Type', 'Authorization', 'x-request-id']
}));

// Global Rate Limiting for standard API endpoints
app.use('/api/', apiLimiter);

// Strict Rate Limiting for computationally heavy AI and upload endpoints
const expensiveAiEndpoints = [
  '/api/resume/upload',
  '/api/v1/resume/upload',
  '/api/resume/analyze',
  '/api/v1/resume/analyze',
  '/api/career/analyze',
  '/api/v1/career/analyze',
  '/api/career/recalibrate',
  '/api/v1/career/recalibrate',
  '/api/roadmap/generate',
  '/api/v1/roadmap/generate',
  '/api/interview/evaluate',
  '/api/v1/interview/evaluate',
  '/api/edge-ai/process',
  '/api/v1/edge-ai/process'
];
app.use(expensiveAiEndpoints, expensiveAiLimiter);

app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// Correlation ID & Request Logger (suppressed during automated tests to avoid noisy output)
app.use((req, res, next) => {
  const requestId = req.headers['x-request-id'] || `req_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
  res.setHeader('x-request-id', requestId);
  const start = Date.now();
  res.on('finish', () => {
    if (process.env.NODE_ENV !== 'test') {
      const duration = Date.now() - start;
      console.log(`[HTTP] [${requestId}] ${req.method} ${req.originalUrl} - ${res.statusCode} (${duration}ms)`);
    }
  });
  next();
});

// Health check endpoints (/health & /ready)
app.get(['/health', '/api/health', '/api/v1/health'], (req, res) => {
  res.json({
    status: 'healthy',
    platform: 'CareerHub AI Production Backend',
    database: 'SQLite (WAL Mode)',
    uptimeSeconds: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

app.get(['/ready', '/api/ready', '/api/v1/ready'], (req, res) => {
  try {
    const db = getDatabase();
    const result = db.prepare('SELECT 1 as alive').get() as { alive: number };
    if (result && result.alive === 1) {
      return res.json({
        status: 'ready',
        databaseConnected: true,
        walMode: true,
        timestamp: new Date().toISOString()
      });
    }
    return res.status(503).json({ status: 'not_ready', databaseConnected: false });
  } catch (err: any) {
    return res.status(503).json({ status: 'not_ready', error: err.message });
  }
});

// Mount Legacy & Versioned API v1 Routes
const routePairs: Array<[string, express.Router]> = [
  ['/auth', authRoutes],
  ['/profile', profileRoutes],
  ['/career', careerRoutes],
  ['/roadmap', roadmapRoutes],
  ['/resume', resumeRoutes],
  ['/projects', projectsRoutes],
  ['/interview', interviewRoutes],
  ['/learning', learningRoutes],
  ['/integrations', integrationsRoutes],
  ['/analytics', analyticsRoutes],
  ['/user', userRoutes],
  ['/edge-ai', edgeAiRoutes]
];

for (const [pathPrefix, router] of routePairs) {
  app.use(`/api${pathPrefix}`, router);
  app.use(`/api/v1${pathPrefix}`, router);
}

// Centralized error handler
app.use(errorHandler);

// Start server
const server = app.listen(PORT, () => {
  console.log(`========================================================`);
  console.log(`🚀 CareerHub AI Production Server Running on port ${PORT}`);
  console.log(`🌐 Health check: http://localhost:${PORT}/api/v1/health`);
  console.log(`🔒 Multi-tenant security, Helmet & Rate Limiter active`);
  console.log(`========================================================`);
});

// Graceful Shutdown
function gracefulShutdown(signal: string) {
  console.log(`[Server] Received ${signal}, closing AI queue, HTTP server & database connections...`);
  try {
    aiQueueManager.shutdown();
  } catch { /* empty */ }

  server.close(() => {
    try {
      const db = getDatabase();
      db.close();
      console.log(`[Server] Database connections closed cleanly.`);
    } catch { /* empty */ }
    process.exit(0);
  });
}

process.on('SIGTERM', () => gracefulShutdown('SIGTERM'));
process.on('SIGINT', () => gracefulShutdown('SIGINT'));

export default app;
