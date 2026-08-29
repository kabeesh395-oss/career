import { Capacitor } from '@capacitor/core';
import { handleMockApi } from './mockService';

export function getApiBase(): string {
  const custom = localStorage.getItem('cp_api_url');
  if (custom) return custom;
  const envUrl = (import.meta as any).env?.VITE_API_URL;
  if (envUrl) return envUrl;
  if (Capacitor.isNativePlatform()) return 'http://10.0.2.2:5000/api';
  return '/api';
}

function getToken(): string | null {
  return localStorage.getItem('cp_token');
}

export function setToken(token: string) {
  localStorage.setItem('cp_token', token);
}

export function clearToken() {
  localStorage.removeItem('cp_token');
}

export async function api<T = any>(
  endpoint: string,
  options: RequestInit = {}
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = {
    ...(options.headers as Record<string, string> || {}),
  };

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  // Don't set Content-Type for FormData (browser sets boundary automatically)
  if (!(options.body instanceof FormData)) {
    headers['Content-Type'] = 'application/json';
  }

  try {
    const res = await fetch(`${getApiBase()}${endpoint}`, {
      ...options,
      headers,
    });

    if (res.status === 401) {
      clearToken();
      window.location.hash = '#/login';
      throw new Error('Session expired. Please log in again.');
    }

    if (res.ok) {
      const data = await res.json();
      return data as T;
    }

    // If server responded with 404 or 500+, fall back to client mock engine
    return (await handleMockApi(endpoint, options)) as T;
  } catch (err: any) {
    if (err.message === 'Session expired. Please log in again.') {
      throw err;
    }
    // Automatically fall back to client-side mock service when server is unreachable or offline
    console.info(`[CareerHub] Falling back to client-side engine for ${endpoint}`);
    return (await handleMockApi(endpoint, options)) as T;
  }
}

