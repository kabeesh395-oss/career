import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, act } from '@testing-library/react';
import { useState } from 'react';
import { AppContent } from './main';
import * as AuthContextModule from './context/AuthContext';

vi.mock('./components/Auth/AuthPage', () => ({
  default: () => <div data-testid="auth-page">Authentication Screen</div>,
}));

vi.mock('./components/Layout/Sidebar', () => ({
  default: ({ activePage, onNavigate }: { activePage: string; onNavigate: (page: string) => void }) => (
    <aside data-testid="sidebar" data-active={activePage}>
      <button onClick={() => onNavigate('home')}>Nav Home</button>
      <button onClick={() => onNavigate('resume')}>Nav Resume</button>
      <button onClick={() => onNavigate('practice')}>Nav Practice</button>
      <button onClick={() => onNavigate('skills')}>Nav Skills</button>
      <button onClick={() => onNavigate('profile')}>Nav Profile</button>
      <button onClick={() => onNavigate('roadmap')}>Nav Legacy Roadmap</button>
    </aside>
  ),
}));

vi.mock('./components/Layout/BottomNav', () => ({
  default: ({ activePage, onNavigate }: { activePage: string; onNavigate: (page: string) => void }) => (
    <nav data-testid="bottom-nav" data-active={activePage}>
      <button onClick={() => onNavigate('home')}>Bottom Home</button>
      <button onClick={() => onNavigate('resume')}>Bottom Resume</button>
      <button onClick={() => onNavigate('practice')}>Bottom Practice</button>
      <button onClick={() => onNavigate('skills')}>Bottom Skills</button>
      <button onClick={() => onNavigate('profile')}>Bottom Profile</button>
    </nav>
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
  default: ({ initialTab }: { initialTab?: string }) => (
    <div data-testid="dashboard-page" data-tab={initialTab || 'overview'}>
      Home Content ({initialTab || 'overview'})
    </div>
  ),
}));

vi.mock('./components/Resume/ResumePage', () => ({
  default: ({ initialTab }: { initialTab?: string }) => (
    <div data-testid="resume-page" data-tab={initialTab || 'resume'}>
      Resume Content
    </div>
  ),
}));

vi.mock('./components/Practice/PracticePage', () => ({
  default: ({ initialTab }: { initialTab?: string }) => (
    <div data-testid="practice-page" data-tab={initialTab || 'interview'}>
      Practice Content ({initialTab || 'interview'})
    </div>
  ),
}));

vi.mock('./components/Skills/SkillsHubPage', () => ({
  default: ({ initialTab }: { initialTab?: string }) => (
    <div data-testid="skills-page" data-tab={initialTab || 'my-skills'}>
      Skills Content ({initialTab || 'my-skills'})
    </div>
  ),
}));

vi.mock('./components/Profile/ProfilePage', () => ({
  default: ({ initialTab }: { initialTab?: string }) => (
    <div data-testid="profile-page" data-tab={initialTab || 'general'}>
      Profile Content ({initialTab || 'general'})
    </div>
  ),
}));

const mockAuthenticatedUser = {
  id: 'usr_valid_456',
  email: 'jordan@dev.io',
  fullName: 'Jordan Engineer',
  role: 'user',
};

describe('main.tsx 5-Section Navigation & Authentication Transitions', () => {
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
    expect(screen.queryByTestId('bottom-nav')).toBeNull();
  });

  it('renders default Home view with Sidebar, AppHeader, and BottomNav when authenticated and hash is empty', () => {
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

    expect(screen.getByTestId('sidebar').getAttribute('data-active')).toBe('home');
    expect(screen.getByTestId('bottom-nav').getAttribute('data-active')).toBe('home');
    expect(screen.getByTestId('app-header').getAttribute('data-active')).toBe('home');
    expect(screen.getByTestId('dashboard-page')).toBeDefined();
  });

  it('routes legacy deep links cleanly into the 5 primary sections', () => {
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

    expect(screen.getByTestId('skills-page').getAttribute('data-tab')).toBe('roadmap');
    expect(screen.getByTestId('bottom-nav').getAttribute('data-active')).toBe('skills');
  });

  it('transitions cleanly across all 5 primary sections and sub-tabs on hashchange', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '#/home';
    render(<AppContent />);
    expect(screen.getByTestId('dashboard-page')).toBeDefined();

    act(() => {
      window.location.hash = '#/resume';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('resume-page')).toBeDefined();

    act(() => {
      window.location.hash = '#/practice';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('practice-page').getAttribute('data-tab')).toBe('interview');

    act(() => {
      window.location.hash = '#/learning';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('practice-page').getAttribute('data-tab')).toBe('learning');

    act(() => {
      window.location.hash = '#/skills/projects';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('skills-page').getAttribute('data-tab')).toBe('projects');

    act(() => {
      window.location.hash = '#/analytics';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('dashboard-page').getAttribute('data-tab')).toBe('analytics');

    act(() => {
      window.location.hash = '#/integrations';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('profile-page').getAttribute('data-tab')).toBe('integrations');

    act(() => {
      window.location.hash = '#/edge-ai';
      window.dispatchEvent(new HashChangeEvent('hashchange'));
    });
    expect(screen.getByTestId('profile-page').getAttribute('data-tab')).toBe('system');
  });

  it('falls back to Home overview when receiving an unknown hash route', () => {
    vi.spyOn(AuthContextModule, 'useAuth').mockReturnValue({
      user: mockAuthenticatedUser,
      profile: null,
      loading: false,
      login: vi.fn(),
      signup: vi.fn(),
      logout: vi.fn(),
      refreshProfile: vi.fn(),
    });

    window.location.hash = '#/nonexistent-route';
    render(<AppContent />);

    expect(screen.getByTestId('dashboard-page').getAttribute('data-tab')).toBe('overview');
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

    act(() => {
      screen.getByText('Nav Legacy Roadmap').click();
    });

    expect(window.location.hash).toBe('#/skills/roadmap');
    expect(screen.getByTestId('skills-page').getAttribute('data-tab')).toBe('roadmap');
  });

  it('transitions UI cleanly between unauthenticated and authenticated states', () => {
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
    expect(screen.getByTestId('auth-page')).toBeDefined();

    act(() => {
      authState = { ...authState, user: mockAuthenticatedUser };
      triggerUpdate!();
    });
    rerender(<TestHost />);
    expect(screen.getByTestId('dashboard-page')).toBeDefined();

    act(() => {
      authState = { ...authState, user: null };
      triggerUpdate!();
    });
    rerender(<TestHost />);
    expect(screen.getByTestId('auth-page')).toBeDefined();
  });
});
