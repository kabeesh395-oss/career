import rateLimit from 'express-rate-limit';

// Standard API Rate Limiter (100 requests / min)
export const apiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 100,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'Too many requests from this IP, please try again after 60 seconds.',
    statusCode: 429
  }
});

// Strict Rate Limiter for Expensive AI & Upload Endpoints (10 requests / min)
export const expensiveAiLimiter = rateLimit({
  windowMs: 60 * 1000, // 1 minute
  max: 10,
  standardHeaders: true,
  legacyHeaders: false,
  message: {
    error: 'AI generation limit reached. Please wait 60 seconds before submitting another request.',
    statusCode: 429
  }
});
