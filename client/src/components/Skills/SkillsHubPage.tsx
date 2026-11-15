import { Suspense, lazy, useEffect, useState } from 'react';
import MySkillsTab from './MySkillsTab';

const CareerPage = lazy(() => import('../Career/CareerPage'));
const RoadmapPage = lazy(() => import('../Roadmap/RoadmapPage'));
const ProjectsPage = lazy(() => import('../Projects/ProjectsPage'));

export type SkillsSubTab = 'my-skills' | 'gaps' | 'roadmap' | 'projects';

interface SkillsHubPageProps {
  initialTab?: SkillsSubTab;
  onNavigate?: (page: string) => void;
}

export default function SkillsHubPage({ initialTab = 'my-skills', onNavigate }: SkillsHubPageProps) {
  const [activeTab, setActiveTab] = useState<SkillsSubTab>(initialTab);

  useEffect(() => {
    setActiveTab(initialTab);
  }, [initialTab]);

  const switchTab = (tab: SkillsSubTab) => {
    setActiveTab(tab);
    onNavigate?.(`skills/${tab}`);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: '0 0 2px' }}>Skills & Progression</h1>
          <p style={{ color: '#94a3b8', fontSize: 13.5, margin: 0 }}>
            Record verified skills, measure role gaps, follow milestones, and showcase portfolio builds.
          </p>
        </div>

        <div className="segmented-tabs" role="tablist" aria-label="Skills Views">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'my-skills'}
            className={`segmented-tab-btn ${activeTab === 'my-skills' ? 'active' : ''}`}
            onClick={() => switchTab('my-skills')}
          >
            My Skills
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'gaps'}
            className={`segmented-tab-btn ${activeTab === 'gaps' ? 'active' : ''}`}
            onClick={() => switchTab('gaps')}
          >
            Skill Gaps
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'roadmap'}
            className={`segmented-tab-btn ${activeTab === 'roadmap' ? 'active' : ''}`}
            onClick={() => switchTab('roadmap')}
          >
            Roadmap
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'projects'}
            className={`segmented-tab-btn ${activeTab === 'projects' ? 'active' : ''}`}
            onClick={() => switchTab('projects')}
          >
            Projects
          </button>
        </div>
      </div>

      {activeTab === 'my-skills' && <MySkillsTab onNavigate={onNavigate} />}

      {activeTab !== 'my-skills' && (
        <Suspense fallback={<div style={{ color: '#94a3b8', padding: 32 }}>Loading view…</div>}>
          {activeTab === 'gaps' && <CareerPage />}
          {activeTab === 'roadmap' && <RoadmapPage />}
          {activeTab === 'projects' && <ProjectsPage />}
        </Suspense>
      )}
    </div>
  );
}
