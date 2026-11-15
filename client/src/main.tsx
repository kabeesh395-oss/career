import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import AuthPage from './components/Auth/AuthPage';
import Sidebar from './components/Layout/Sidebar';
import AppHeader from './components/Layout/AppHeader';
import BottomNav from './components/Layout/BottomNav';
import DashboardPage from './components/Dashboard/DashboardPage';
import ResumePage from './components/Resume/ResumePage';
import PracticePage from './components/Practice/PracticePage';
import SkillsHubPage, { type SkillsSubTab } from './components/Skills/SkillsHubPage';
import ProfilePage, { type ProfileSubTab } from './components/Profile/ProfilePage';
import { resolveRoute, type ResolvedRoute } from './navigation';
import './index.css';

export function AppContent() {
  const { user, loading } = useAuth();
  const [route, setRoute] = useState<ResolvedRoute>(() =>
    resolveRoute(typeof window !== 'undefined' ? window.location.hash : '')
  );

  useEffect(() => {
    const handleHashChange = () => {
      setRoute(resolveRoute(window.location.hash));
    };

    window.addEventListener('hashchange', handleHashChange);
    handleHashChange();

    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const navigateTo = (target: string) => {
    const resolved = resolveRoute(target);
    const nextHash = resolved.subTab && !['overview', 'resume', 'interview', 'my-skills', 'general'].includes(resolved.subTab)
      ? `#/${resolved.section}/${resolved.subTab}`
      : `#/${resolved.section}`;

    setRoute(resolved);
    if (window.location.hash !== nextHash) {
      window.location.hash = nextHash;
    }
  };

  if (loading) {
    return (
      <div
        style={{
          minHeight: '100vh',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          background: 'hsl(var(--bg-base))',
          color: 'hsl(var(--text-secondary))',
          fontSize: 16,
        }}
      >
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          <div
            style={{
              width: 40,
              height: 40,
              border: '3px solid rgba(59,130,246,0.2)',
              borderTopColor: '#3b82f6',
              borderRadius: '50%',
              animation: 'spin 1s linear infinite',
            }}
          />
          <span>Authenticating Session…</span>
        </div>
        <style>{`
          @keyframes spin {
            to { transform: rotate(360deg); }
          }
        `}</style>
      </div>
    );
  }

  if (!user) {
    return <AuthPage />;
  }

  const renderPage = () => {
    switch (route.section) {
      case 'home':
        return (
          <DashboardPage
            onNavigate={navigateTo}
            initialTab={route.subTab === 'analytics' ? 'analytics' : 'overview'}
          />
        );
      case 'resume':
        return (
          <ResumePage
            initialTab={route.subTab === 'job_description' ? 'job_description' : 'resume'}
          />
        );
      case 'practice':
        return (
          <PracticePage
            initialTab={route.subTab === 'learning' ? 'learning' : 'interview'}
            onNavigate={navigateTo}
          />
        );
      case 'skills':
        return (
          <SkillsHubPage
            initialTab={(route.subTab as SkillsSubTab) || 'my-skills'}
            onNavigate={navigateTo}
          />
        );
      case 'profile':
        return (
          <ProfilePage
            initialTab={(route.subTab as ProfileSubTab) || 'general'}
            onNavigate={navigateTo}
          />
        );
      default:
        return <DashboardPage onNavigate={navigateTo} />;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'hsl(var(--bg-base))' }}>
      <Sidebar
        activePage={route.section}
        onNavigate={navigateTo}
      />
      <main className="app-main-content">
        <AppHeader
          activePage={route.section}
          onNavigate={navigateTo}
        />
        {renderPage()}
      </main>
      <BottomNav
        activePage={route.section}
        onNavigate={navigateTo}
      />
    </div>
  );
}

export function App() {
  return (
    <ThemeProvider>
      <AuthProvider>
        <AppContent />
      </AuthProvider>
    </ThemeProvider>
  );
}

const rootElement = document.getElementById('root');
if (rootElement) {
  ReactDOM.createRoot(rootElement).render(
    <React.StrictMode>
      <App />
    </React.StrictMode>
  );
}
