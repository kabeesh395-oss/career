import React, { useEffect, useState } from 'react';
import ReactDOM from 'react-dom/client';
import { AuthProvider, useAuth } from './context/AuthContext';
import AuthPage from './components/Auth/AuthPage';
import Sidebar from './components/Layout/Sidebar';
import AppHeader from './components/Layout/AppHeader';
import DashboardPage from './components/Dashboard/DashboardPage';
import CareerPage from './components/Career/CareerPage';
import RoadmapPage from './components/Roadmap/RoadmapPage';
import ResumePage from './components/Resume/ResumePage';
import ProjectsPage from './components/Projects/ProjectsPage';
import InterviewPage from './components/Interview/InterviewPage';
import LearningPage from './components/Learning/LearningPage';
import IntegrationsPage from './components/Integrations/IntegrationsPage';
import AnalyticsPage from './components/Analytics/AnalyticsPage';
import ProfilePage from './components/Profile/ProfilePage';
import MobileUiTemplates from './components/Templates/MobileUiTemplates';
import './index.css';

function AppContent() {
  const { user, loading } = useAuth();
  const [activePage, setActivePage] = useState('dashboard');

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
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        background: 'hsl(222, 47%, 7%)',
        color: '#94a3b8',
        fontSize: 16
      }}>
        <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 40, height: 40, border: '3px solid rgba(59,130,246,0.2)',
            borderTopColor: '#3b82f6', borderRadius: '50%',
            animation: 'spin 1s linear infinite'
          }} />
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

  // Render correct page view
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
      case 'templates':
        return <MobileUiTemplates />;
      case 'profile':
        return <ProfilePage />;
      default:
        return <DashboardPage onNavigate={navigateTo} />;
    }
  };

  return (
    <div style={{ display: 'flex', minHeight: '100vh', background: 'hsl(222, 47%, 7%)' }}>
      <Sidebar activePage={activePage} onNavigate={navigateTo} />
      <main style={{
        flex: 1,
        marginLeft: 240, // Match sidebar width
        padding: '24px 36px 48px',
        maxWidth: 1240,
        width: 'calc(100% - 240px)'
      }}>
        <AppHeader activePage={activePage} onNavigate={navigateTo} />
        {renderPage()}
      </main>
    </div>
  );
}

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <AuthProvider>
      <AppContent />
    </AuthProvider>
  </React.StrictMode>
);
