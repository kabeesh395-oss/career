import { Suspense, lazy, useEffect, useState } from 'react';

const InterviewPage = lazy(() => import('../Interview/InterviewPage'));
const LearningPage = lazy(() => import('../Learning/LearningPage'));

interface PracticePageProps {
  initialTab?: 'interview' | 'learning';
  onNavigate?: (page: string) => void;
}

export default function PracticePage({ initialTab = 'interview', onNavigate }: PracticePageProps) {
  const [activeTab, setActiveTab] = useState<'interview' | 'learning'>(initialTab);

  useEffect(() => {
    setActiveTab(initialTab);
  }, [initialTab]);

  const switchTab = (tab: 'interview' | 'learning') => {
    setActiveTab(tab);
    onNavigate?.(`practice/${tab}`);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: '0 0 2px' }}>Practice</h1>
          <p style={{ color: '#94a3b8', fontSize: 13.5, margin: 0 }}>
            Rehearse technical interviews and track verified study resources.
          </p>
        </div>

        <div className="segmented-tabs" role="tablist" aria-label="Practice Views">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'interview'}
            className={`segmented-tab-btn ${activeTab === 'interview' ? 'active' : ''}`}
            onClick={() => switchTab('interview')}
          >
            Mock Interview
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'learning'}
            className={`segmented-tab-btn ${activeTab === 'learning' ? 'active' : ''}`}
            onClick={() => switchTab('learning')}
          >
            Learning Resources
          </button>
        </div>
      </div>

      <Suspense fallback={<div style={{ color: '#94a3b8', padding: 32 }}>Loading practice workspace…</div>}>
        {activeTab === 'interview' ? <InterviewPage /> : <LearningPage />}
      </Suspense>
    </div>
  );
}
