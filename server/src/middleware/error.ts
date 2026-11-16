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
  const isProduction = process.env.NODE_ENV === 'production';
  const clientMessage = isProduction && statusCode >= 500
    ? 'An unexpected server error occurred. Please try again.'
    : message;

  // Keep production logs free of exception messages, details, and query-string PII.
  console.error(`[Error] ${req.method} ${req.path} - Status: ${statusCode} - Code: ${errorCode}`, {
    name: err.name,
    stack: isProduction ? undefined : err.stack
  });

  res.status(statusCode).json({
    error: {
      code: errorCode,
      message: clientMessage,
      details: isProduction ? undefined : err.details || undefined,
      timestamp: new Date().toISOString()
    }
  });
}
