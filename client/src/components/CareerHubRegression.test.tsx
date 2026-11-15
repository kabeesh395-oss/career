import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor, fireEvent } from '@testing-library/react';
import { navItems } from './Layout/Sidebar';
import { PRIMARY_NAV_ITEMS } from './Layout/BottomNav';
import { normalizeSkillGap } from './Career/CareerPage';
import DashboardPage from './Dashboard/DashboardPage';
import AppHeader from './Layout/AppHeader';
import * as apiModule from '../api/client';
import * as AuthContextModule from '../context/AuthContext';

vi.mock('./Common/PatternWaves', () => ({
  default: () => <div data-testid="pattern-waves-mock" />,
}));

describe('Career Hub 5-Section & Data Integrity Regressions', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('keeps strictly the 5 primary navigation sections in Sidebar and BottomNav', () => {
    expect(navItems.map(i => i.id)).toEqual(['home', 'resume', 'practice', 'skills', 'profile']);
    expect(PRIMARY_NAV_ITEMS.map(i => i.id)).toEqual(['home', 'resume', 'practice', 'skills', 'profile']);
  });

  it('normalizes both camelCase and snake_case skill gaps cleanly', () => {
    const camel = normalizeSkillGap({
      skillName: 'PostgreSQL',
      category: 'Databases',
      currentLevel: 2,
      requiredLevel: 4,
      priority: 'high',
      recommendation: 'Practice indexing',
    });
    const snake = normalizeSkillGap({
      skill_name: 'Docker',
      category: 'DevOps',
      current_level: 1,
      required_level: 3,
      priority: 'medium',
      recommendation: 'Build container images',
    });

    expect(camel).toEqual({
      skillName: 'PostgreSQL',
      category: 'Databases',
      currentLevel: 2,
      requiredLevel: 4,
      priority: 'high',
      recommendation: 'Practice indexing',
    });

    expect(snake).toEqual({
      skillName: 'Docker',
      category: 'DevOps',
      currentLevel: 1,
      requiredLevel: 3,
      priority: 'medium',
      recommendation: 'Build container images',
    });
  });

  it('shows honest empty/uncalibrated states on Home instead of "undefined%" or fabricated defaults', async () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: { id: 'u1', email: 'alex@example.com', fullName: 'Alex', role: 'user' },
      profile: {
        id: 'p1',
        user_id: 'u1',
        headline: '',
        bio: '',
        location: '',
        education: '',
        experience_years: 0,
        target_role: '',
        target_industry: '',
        target_salary: '',
        current_readiness_score: 0,
        onboarding_completed: 0,
      },
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn().mockResolvedValue(true),
    });

    vi.spyOn(apiModule, 'api').mockImplementation(async (endpoint: string) => {
      if (endpoint === '/analytics/dashboard') {
        return {
          analytics: {
            hasSufficientData: false,
            readinessScore: 0,
            targetRole: null,
            onboardingCompleted: false,
            tasks: { total: 0, completed: 0, percent: 0, hasData: false },
            interviews: { completed: 0, averageScore: 0, hasData: false },
            skills: { acquired: 0, gapsIdentified: 0, hasData: false },
            projects: { total: 0, completed: 0, hasData: false },
            resume: { totalUploaded: 0, hasData: false },
            recentActivity: [],
          },
        };
      }
      if (endpoint === '/career/next-best-action') {
        return { action: null };
      }
      if (endpoint === '/roadmap') {
        return { roadmap: null, items: [] };
      }
      return {};
    });

    render(<DashboardPage onNavigate={vi.fn()} />);

    await waitFor(() => {
      expect(screen.getByText('Not calibrated')).toBeDefined();
    });

    expect(screen.getAllByText('Not set')).toHaveLength(2);
    expect(screen.getByText('No resume analyzed yet')).toBeDefined();
    expect(screen.queryByText(/undefined%/i)).toBeNull();
  });

  it('shows error indicator rather than fake success when AppHeader refresh fails', async () => {
    const refreshMock = vi.fn().mockResolvedValue(false);
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: { id: 'u1', email: 'alex@example.com', fullName: 'Alex', role: 'user' },
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: refreshMock,
    });

    render(<AppHeader onNavigate={vi.fn()} activePage="home" />);

    const syncBtn = screen.getByTestId('sync-button');
    fireEvent.click(syncBtn);

    await waitFor(() => {
      expect(refreshMock).toHaveBeenCalledTimes(1);
      expect(syncBtn.getAttribute('title')).toBe('Failed to refresh data from server');
    });
  });
});
