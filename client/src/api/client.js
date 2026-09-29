import { Capacitor } from '@capacitor/core';
export function getApiBase() {
    const custom = localStorage.getItem('cp_api_url');
    if (custom)
        return custom;
    const envUrl = import.meta.env?.VITE_API_URL;
    if (envUrl)
        return envUrl;
    if (Capacitor.isNativePlatform())
        return 'http://10.0.2.2:5000/api';
    return '/api';
}
function getToken() {
    return localStorage.getItem('cp_token');
}
export function setToken(token) {
    localStorage.setItem('cp_token', token);
}
export function clearToken() {
    localStorage.removeItem('cp_token');
}
export async function api(endpoint, options = {}) {
    const token = getToken();
    const headers = {
        ...(options.headers || {}),
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
        window.location.hash = '#/login';
        throw new Error('Session expired. Please log in again.');
    }
    if (!res.ok) {
        let errorMessage = `API request failed with status ${res.status}`;
        try {
            const errData = await res.json();
            if (errData?.error?.message) {
                errorMessage = errData.error.message;
            }
        }
        catch {
            // ignore
        }
        throw new Error(errorMessage);
    }
    const data = await res.json();
    return data;
}
