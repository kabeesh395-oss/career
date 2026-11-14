export type CorsOriginCallback = (
  origin: string | undefined,
  callback: (error: Error | null, allow?: boolean) => void
) => void;

const defaultAllowedOrigins = [
  'http://localhost:3000',
  'http://localhost:5173',
  'http://127.0.0.1:3000',
  'http://127.0.0.1:5173'
];

export function createCorsOriginCallback(
  configuredOrigin: string | undefined,
  nodeEnv: string | undefined
): CorsOriginCallback {
  const isTest = nodeEnv === 'test';
  const configuredOrigins = configuredOrigin
    ?.split(',')
    .map(origin => origin.trim())
    .filter(Boolean);

  if (!isTest && configuredOrigins?.includes('*')) {
    throw new Error('CORS_ORIGIN must contain explicit origins; "*" is not allowed when credentials are enabled.');
  }

  if (nodeEnv === 'production' && (!configuredOrigins || configuredOrigins.length === 0)) {
    throw new Error('CORS_ORIGIN must be configured with at least one explicit origin in production.');
  }

  const allowedOrigins = configuredOrigins?.length ? configuredOrigins : defaultAllowedOrigins;

  return (origin, callback) => {
    // Native mobile clients and server-to-server requests do not send an Origin header.
    if (!origin || isTest || allowedOrigins.includes(origin)) {
      return callback(null, true);
    }

    return callback(new Error(`CORS policy rejection: Origin ${origin} not allowed`));
  };
}
