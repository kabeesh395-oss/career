import { describe, it, expect, vi, beforeEach } from 'vitest';
import { renderHook, act, waitFor } from '@testing-library/react';
import type { ReactNode } from 'react';
import { AuthProvider, useAuth } from './AuthContext';
import * as apiClient from '../api/client';

// Mock the apiClient module
vi.mock('../api/client', () => ({
  api: vi.fn(),
  setToken: vi.fn((token: string) => localStorage.setItem('cp_token', token)),
  clearToken: vi.fn(() => localStorage.removeItem('cp_token')),
  getApiBase: vi.fn(() => '/api'),
}));

const mockUser = {
  id: 'usr_test123',
  email: 'candidate@test.com',
  fullName: 'Morgan Test',
  role: 'user',
};

const mockProfile = {
  id: 'prof_test123',
  user_id: 'usr_test123',
  headline: 'Senior Engineer',
  bio: 'Passionate builder',
  location: 'San Francisco, CA',
  education: 'BS CS',
  experience_years: 5,
  target_role: 'Lead Architect',
  target_industry: 'Cloud Infrastructure',
  target_salary: '$200,000',
  current_readiness_score: 85,
  onboarding_completed: 1,
};

const Wrapper = ({ children }: { children: ReactNode }) => (
  <AuthProvider>{children}</AuthProvider>
);

describe('AuthContext & AuthProvider', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    localStorage.clear();
  });

  it('throws an error if useAuth is used outside of AuthProvider', () => {
    const originalConsoleError = console.error;
    console.error = () => {};
    try {
      expect(() => renderHook(() => useAuth())).toThrow('useAuth must be used within AuthProvider');
    } finally {
      console.error = originalConsoleError;
    }
  });

  it('initializes with loading: false and user: null when no token is present', async () => {
    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(result.current.user).toBeNull();
    expect(result.current.profile).toBeNull();
    expect(apiClient.api).not.toHaveBeenCalled();
  });

  it('restores user session automatically if a token exists in localStorage', async () => {
    localStorage.setItem('cp_token', 'valid_test_jwt_token');

    (apiClient.api as any).mockResolvedValueOnce({
      user: mockUser,
      profile: mockProfile,
    });

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    expect(result.current.loading).toBe(true);

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(apiClient.api).toHaveBeenCalledWith('/auth/me');
    expect(result.current.user).toEqual(mockUser);
    expect(result.current.profile).toEqual(mockProfile);
  });

  it('clears token and resets user state if session restoration fails (e.g. 401 or network error)', async () => {
    localStorage.setItem('cp_token', 'expired_test_jwt_token');

    (apiClient.api as any).mockRejectedValueOnce(new Error('Session expired'));

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    expect(apiClient.clearToken).toHaveBeenCalled();
    expect(localStorage.getItem('cp_token')).toBeNull();
    expect(result.current.user).toBeNull();
    expect(result.current.profile).toBeNull();
  });

  it('handles login successfully, sets token, and restores profile', async () => {
    (apiClient.api as any)
      .mockResolvedValueOnce({
        token: 'auth_jwt_token_123',
        user: mockUser,
      })
      .mockResolvedValueOnce({
        user: mockUser,
        profile: mockProfile,
      });

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    await act(async () => {
      await result.current.login('candidate@test.com', 'password123');
    });

    expect(apiClient.api).toHaveBeenCalledWith('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email: 'candidate@test.com', password: 'password123' }),
    });

    expect(apiClient.setToken).toHaveBeenCalledWith('auth_jwt_token_123');
    expect(result.current.user).toEqual(mockUser);
    expect(result.current.profile).toEqual(mockProfile);
  });

  it('propagates error when login fails', async () => {
    (apiClient.api as any).mockRejectedValueOnce(new Error('Invalid email or password'));

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    await expect(
      act(async () => {
        await result.current.login('candidate@test.com', 'wrongpassword');
      })
    ).rejects.toThrow('Invalid email or password');

    expect(result.current.user).toBeNull();
  });

  it('handles signup successfully, sets token, and restores profile', async () => {
    (apiClient.api as any)
      .mockResolvedValueOnce({
        token: 'new_jwt_signup_token',
        user: mockUser,
      })
      .mockResolvedValueOnce({
        user: mockUser,
        profile: mockProfile,
      });

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.loading).toBe(false);
    });

    await act(async () => {
      await result.current.signup('candidate@test.com', 'password123', 'Morgan Test');
    });

    expect(apiClient.api).toHaveBeenCalledWith('/auth/signup', {
      method: 'POST',
      body: JSON.stringify({
        email: 'candidate@test.com',
        password: 'password123',
        fullName: 'Morgan Test',
      }),
    });

    expect(apiClient.setToken).toHaveBeenCalledWith('new_jwt_signup_token');
    expect(result.current.user).toEqual(mockUser);
    expect(result.current.profile).toEqual(mockProfile);
  });

  it('handles logout by clearing tokens and resetting user and profile to null', async () => {
    localStorage.setItem('cp_token', 'active_token');

    (apiClient.api as any).mockResolvedValueOnce({
      user: mockUser,
      profile: mockProfile,
    });

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.user).toEqual(mockUser);
    });

    act(() => {
      result.current.logout();
    });

    expect(apiClient.clearToken).toHaveBeenCalled();
    expect(result.current.user).toBeNull();
    expect(result.current.profile).toBeNull();
  });

  it('refreshProfile re-fetches latest user profile data without crashing on error', async () => {
    const updatedProfile = {
      ...mockProfile,
      current_readiness_score: 95,
      target_role: 'VP of Engineering',
    };

    (apiClient.api as any)
      .mockResolvedValueOnce({
        user: mockUser,
        profile: mockProfile,
      })
      .mockResolvedValueOnce({
        user: mockUser,
        profile: updatedProfile,
      })
      .mockRejectedValueOnce(new Error('Network error on refresh'));

    localStorage.setItem('cp_token', 'valid_token');

    const { result } = renderHook(() => useAuth(), { wrapper: Wrapper });

    await waitFor(() => {
      expect(result.current.profile).toEqual(mockProfile);
    });

    // Refresh successfully
    await act(async () => {
      await result.current.refreshProfile();
    });
    expect(result.current.profile?.target_role).toBe('VP of Engineering');
    expect(result.current.profile?.current_readiness_score).toBe(95);

    // Refresh failure is caught gracefully (no unhandled rejection)
    await act(async () => {
      await result.current.refreshProfile();
    });
    // Values remain preserved
    expect(result.current.profile?.target_role).toBe('VP of Engineering');
  });
});
