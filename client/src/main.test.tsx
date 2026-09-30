import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { useState } from 'react';
import { AppContent } from './main';
import * as AuthContextModule from './context/AuthContext';

// Mock child page components so we isolate routing and layout integration logic
vi.mock('./components/Auth/AuthPage', () => ({
  default: () => <div data-testid="auth-page">Authentication Screen</div>,
}));

vi.mock('./components/Layout/Sidebar', () => ({
  default: ({ activePage, onNavigate }: { activePage: string; onNavigate: (page: string) => void }) => (
    <aside data-testid="sidebar" data-active={activePage}>
      <button onClick={() => onNavigate('career')}>Nav Career</button>
      <button onClick={() => onNavigate('roadmap')}>Nav Roadmap</button>
      <button onClick={() => onNavigate('resume')}>Nav Resume</button>
      <button onClick={() => onNavigate('projects')}>Nav Projects</button>
      <button onClick={() => onNavigate('interview')}>Nav Interview</button>
      <button onClick={() => onNavigate('learning')}>Nav Learning</button>
      <button onClick={() => onNavigate('integrations')}>Nav Integrations</button>
      <button onClick={() => onNavigate('analytics')}>Nav Analytics</button>
      <button onClick={() => onNavigate('edge-ai')}>Nav Edge AI</button>
      <button onClick={() => onNavigate('profile')}>Nav Profile</button>
      <button onClick={() => onNavigate('nonexistent')}>Nav Unknown</button>
    </aside>
  ),
}));

vi.mock('./components/Layout/AppHeader', () => ({
  default: ({ activePage, onNavigate }: { activePage: string; onNavigate: (page: string) => void }) => (
    <header data-testid="app-header" data-active={activePage}>
      <button onClick={() => onNavigate('profile')}>Header Profile</button>
    </header>
  ),
}));

vi.mock('./components/Dashboard/DashboardPage', () => ({
  default: () => <div data-testid="dashboard-page">Dashboard Content</div>,
}));

vi.mock('./components/Career/CareerPage', () => ({
  default: () => <div data-testid="career-page">Career Content</div>,
}));

vi.mock('./components/Roadmap/RoadmapPage', () => ({
  default: () => <div data-testid="roadmap-page">Roadmap Content</div>,
}));

vi.mock('./components/Resume/ResumePage', () => ({
  default: () => <div data-testid="resume-page">Resume Content</div>,
}));

vi.mock('./components/Projects/ProjectsPage', () => ({
  default: () => <div data-testid="projects-page">Projects Content</div>,
}));

vi.mock('./components/Interview/InterviewPage', () => ({
  default: () => <div data-testid="interview-page">Interview Content</div>,
}));

vi.mock('./components/Learning/LearningPage', () => ({
  default: () => <div data-testid="learning-page">Learning Content</div>,
}));

vi.mock('./components/Integrations/IntegrationsPage', () => ({
  default: () => <div data-testid="integrations-page">Integrations Content</div>,
}));

vi.mock('./components/Analytics/AnalyticsPage', () => ({
  default: () => <div data-testid="analytics-page">Analytics Content</div>,
}));

vi.mock('./components/EdgeAI/EdgeAiPage', () => ({
  default: () => <div data-testid="edge-ai-page">Edge AI Content</div>,
}));

vi.mock('./components/Profile/ProfilePage', () => ({
  default: () => <div data-testid="profile-page">Profile Content</div>,
}));

const mockAuthenticatedUser = {
  id: 'usr_valid_456',
  email: 'jordan@dev.io',
  fullName: 'Jordan Engineer',
  role: 'user',
};

describe('main.tsx Navigation & Authentication State Transitions', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    window.location.hash = '';
  });

  it('renders loading indicator when authentication is resolving', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: null,
      profile: null,
      loading: true,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    render(<AppContent />);

    expect(screen.getByText('Authenticating Session…')).toBeDefined();
    expect(screen.queryByTestId('auth-page')).toBeNull();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();
    expect(screen.queryByTestId('sidebar')).toBeNull();
  });

  it('renders AuthPage when user is unauthenticated', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: null,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    render(<AppContent />);

    expect(screen.getByTestId('auth-page')).toBeDefined();
    expect(screen.queryByTestId('sidebar')).toBeNull();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();
  });

  it('renders default Dashboard view with Sidebar and AppHeader when authenticated and hash is empty', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '';

    render(<AppContent />);

    expect(screen.getByTestId('sidebar')).toBeDefined();
    expect(screen.getByTestId('app-header')).toBeDefined();
    expect(screen.getByTestId('dashboard-page')).toBeDefined();
    expect(screen.queryByTestId('auth-page')).toBeNull();
  });

  it('renders correct page when mounted with an initial hash route', async () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '#/roadmap';

    render(<AppContent />);

    expect(await screen.findByTestId('roadmap-page')).toBeDefined();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();
  });

  it('transitions between active pages on hashchange event', async () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '#/dashboard';
    render(<AppContent />);
    expect(screen.getByTestId('dashboard-page')).toBeDefined();

    // Transition to Career
    act(() => {
      window.location.hash = '#/career';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('career-page')).toBeDefined();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();

    // Transition to Resume
    act(() => {
      window.location.hash = '#/resume';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('resume-page')).toBeDefined();
    expect(screen.queryByTestId('career-page')).toBeNull();

    // Transition to Projects
    act(() => {
      window.location.hash = '#/projects';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('projects-page')).toBeDefined();

    // Transition to Interview
    act(() => {
      window.location.hash = '#/interview';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('interview-page')).toBeDefined();

    // Transition to Learning
    act(() => {
      window.location.hash = '#/learning';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('learning-page')).toBeDefined();

    // Transition to Integrations
    act(() => {
      window.location.hash = '#/integrations';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('integrations-page')).toBeDefined();

    // Transition to Analytics
    act(() => {
      window.location.hash = '#/analytics';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('analytics-page')).toBeDefined();

    // Transition to Edge AI
    act(() => {
      window.location.hash = '#/edge-ai';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('edge-ai-page')).toBeDefined();

    // Transition to Profile
    act(() => {
      window.location.hash = '#/profile';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(await screen.findByTestId('profile-page')).toBeDefined();
  });

  it('falls back to DashboardPage when navigation receives an unknown hash', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '#/unknown-route-path';

    render(<AppContent />);

    expect(screen.getByTestId('dashboard-page')).toBeDefined();
  });

  it('updates window.location.hash when navigation callback is invoked from UI', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    render(<AppContent />);

    // Click navigation button in sidebar
    act(() => {
      screen.getByText('Nav Roadmap').click();
    });

    expect(window.location.hash).toBe('#/roadmap');
  });

  it('smoothly transitions UI between authenticated and unauthenticated states', () => {
    // Dynamic test wrapper to simulate live auth status changes
    let authState = {
      user: null as any,
      profile: null as any,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    };

    let triggerUpdate: () => void;

    function TestHost() {
      const [, setTick] = useState(0);
      triggerUpdate = () => setTick((t) => t + 1);
      return <AppContent />;
    }

    vi.spyOn(AuthContextModule, 'useAuth').mockImplementation(() => authState);

    const { rerender } = render(<TestHost />);

    // Initially unauthenticated
    expect(screen.getByTestId('auth-page')).toBeDefined();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();

    // Log in transition
    act(() => {
      authState = {
        ...authState,
        user: mockAuthenticatedUser,
      };
      triggerUpdate!();
    });
    rerender(<TestHost />);

    // Authenticated view appears
    expect(screen.queryByTestId('auth-page')).toBeNull();
    expect(screen.getByTestId('dashboard-page')).toBeDefined();
    expect(screen.getByTestId('sidebar')).toBeDefined();

    // Log out transition
    act(() => {
      authState = {
        ...authState,
        user: null,
      };
      triggerUpdate!();
    });
    rerender(<TestHost />);

    // Returns to auth page
    expect(screen.getByTestId('auth-page')).toBeDefined();
    expect(screen.queryByTestId('dashboard-page')).toBeNull();
  });
});
