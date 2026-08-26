import { Request, Response, NextFunction } from 'express';

export interface AppError extends Error {
  statusCode?: number;
  code?: string;
  details?: any;
}

export function errorHandler(
  err: AppError,
  req: Request,
  res: Response,
  next: NextFunction
) {
  const statusCode = err.statusCode || 500;
  const errorCode = err.code || (statusCode === 500 ? 'INTERNAL_SERVER_ERROR' : 'BAD_REQUEST');
  const message = err.message || 'An unexpected error occurred. Please try again.';

  // Structured server-side logging without exposing secrets
  console.error(`[Error] ${req.method} ${req.originalUrl} - Status: ${statusCode} - Code: ${errorCode}`, {
    message: err.message,
    stack: process.env.NODE_ENV === 'production' ? undefined : err.stack,
    details: err.details
  });

  res.status(statusCode).json({
    error: {
      code: errorCode,
      message,
      details: err.details || undefined,
      timestamp: new Date().toISOString()
    }
  });
}
