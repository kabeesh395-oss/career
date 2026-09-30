import { Capacitor } from '@capacitor/core';

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

  const res = await fetch(`${getApiBase()}${endpoint}`, {
    ...options,
    headers,
  });

  if (res.status === 401) {
    clearToken();
    if (typeof window !== 'undefined') {
      window.dispatchEvent(new CustomEvent('auth:unauthorized'));
      window.location.hash = '';
    }
    throw new Error('Session expired. Please log in again.');
  }

  if (res.status === 429) {
    let msg = 'Too many requests. Please wait a moment before trying again.';
    try {
      const errData = await res.json();
      if (errData?.error) msg = typeof errData.error === 'string' ? errData.error : errData.error.message || msg;
      else if (errData?.message) msg = errData.message;
    } catch { /* empty */ }
    throw new Error(msg);
  }

  if (!res.ok) {
    let errorMessage = `API request failed with status ${res.status}`;
    try {
      const errData = await res.json();
      if (errData?.error?.message) {
        errorMessage = errData.error.message;
      }
    } catch {
      // ignore
    }
    throw new Error(errorMessage);
  }

  const data = await res.json();
  return data as T;
}

