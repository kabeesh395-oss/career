import React, { useEffect, useState, Suspense, lazy } from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ThemeProvider } from './context/ThemeContext';
import AuthPage from './components/Auth/AuthPage';
import Sidebar from './components/Layout/Sidebar';
import AppHeader from './components/Layout/AppHeader';
import DashboardPage from './components/Dashboard/DashboardPage';
import './index.css';

// Code splitting: Secondary pages loaded dynamically via React.lazy
const CareerPage = lazy(() => import('./components/Career/CareerPage'));
const RoadmapPage = lazy(() => import('./components/Roadmap/RoadmapPage'));
const ResumePage = lazy(() => import('./components/Resume/ResumePage'));
const ProjectsPage = lazy(() => import('./components/Projects/ProjectsPage'));
const InterviewPage = lazy(() => import('./components/Interview/InterviewPage'));
const LearningPage = lazy(() => import('./components/Learning/LearningPage'));
const IntegrationsPage = lazy(() => import('./components/Integrations/IntegrationsPage'));
const AnalyticsPage = lazy(() => import('./components/Analytics/AnalyticsPage'));
const ProfilePage = lazy(() => import('./components/Profile/ProfilePage'));
const EdgeAiPage = lazy(() => import('./components/EdgeAI/EdgeAiPage'));

function PageLoadingFallback() {
  return (
    <div
      style={{
        padding: '60px 0',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        gap: 14,
        color: 'hsl(var(--text-secondary))',
      }}
    >
      <div
        style={{
          width: 32,
          height: 32,
          border: '3px solid rgba(59, 130, 246, 0.2)',
          borderTopColor: '#38bdf8',
          borderRadius: '50%',
          animation: 'spin 0.8s linear infinite',
        }}
      />
      <span style={{ fontSize: 13, color: 'hsl(var(--text-muted))' }}>Loading view…</span>
    </div>
  );
}

export function AppContent() {
  const { user, loading } = useAuth();
  const [activePage, setActivePage] = useState('dashboard');
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  // Simple Hash Router sync
  useEffect(() => {
    const handleHashChange = () => {
      const hash = window.location.hash.slice(2); // Remove '#/'
      if (hash) {
        setActivePage(hash);
      } else {
        setActivePage('dashboard');
      }
    };

    window.addEventListener('hashchange', handleHashChange);
    handleHashChange(); // Run once on mount

    return () => window.removeEventListener('hashchange', handleHashChange);
  }, []);

  const navigateTo = (page: string) => {
    window.location.hash = `#/${page}`;
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

  // Render correct page view inside Suspense boundary
  const renderPage = () => {
    switch (activePage) {
      case 'dashboard':
        return <DashboardPage onNavigate={navigateTo} />;
      case 'career':
        return <CareerPage />;
      case 'roadmap':
        return <RoadmapPage />;
      case 'resume':
        return <ResumePage />;
      case 'projects':
        return <ProjectsPage />;
      case 'interview':
        return <InterviewPage />;
      case 'learning':
        return <LearningPage />;
      case 'integrations':
        return <IntegrationsPage />;
      case 'analytics':
        return <AnalyticsPage />;
      case 'edge-ai':
        return <EdgeAiPage />;
      case 'profile':
        return <ProfilePage />;
      default:
        return <DashboardPage onNavigate={navigateTo} />;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'hsl(var(--bg-base))' }}>
      <Sidebar
        activePage={activePage}
        onNavigate={navigateTo}
        isOpenMobile={isMobileMenuOpen}
        onCloseMobile={() => setIsMobileMenuOpen(false)}
      />
      <main className="app-main-content">
        <AppHeader
          activePage={activePage}
          onNavigate={navigateTo}
          onToggleMobileMenu={() => setIsMobileMenuOpen(prev => !prev)}
        />
        <Suspense fallback={<PageLoadingFallback />}>
          {renderPage()}
        </Suspense>
      </main>
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
