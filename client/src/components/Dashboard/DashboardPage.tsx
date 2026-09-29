import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

interface NextAction {
  actionId: string;
  title: string;
  category: string;
  whyItMatters: string;
  evidence: string;
  estimatedMinutes: number;
  priority: string;
  targetRoute: string;
  ctaText: string;
}

interface RoadmapItem {
  id: string;
  phase_number: number;
  phase_title: string;
  title: string;
  description: string;
  estimated_hours: number;
  status: string;
  priority: string;
}

interface RoadmapData {
  roadmap: {
    id: string;
    title: string;
    target_role: string;
    total_tasks: number;
    completed_tasks: number;
    progress_percent: number;
    estimated_weeks: number;
  } | null;
  items: RoadmapItem[];
}

interface Analytics {
  readinessScore: number;
  targetRole: string | null;
  onboardingCompleted: boolean;
  tasks: { total: number; completed: number; percent: number };
  interviews: { completed: number; averageScore: number };
  skills: { acquired: number; gapsIdentified: number };
  projects: { total: number; completed: number };
  resume: { overallScore: number; impactScore: number; brevityScore: number; styleScore: number } | null;
  recentActivity: Array<{ eventName: string; data: any; timestamp: string }>;
}

interface Props {
  onNavigate: (page: string) => void;
}

export default function DashboardPage({ onNavigate }: Props) {
  const { user, profile } = useAuth();
  const [analytics, setAnalytics] = useState<Analytics | null>(null);
  const [nextAction, setNextAction] = useState<NextAction | null>(null);
  const [roadmapData, setRoadmapData] = useState<RoadmapData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [analyticsRes, actionRes, roadmapRes] = await Promise.all([
          api('/analytics/dashboard').catch(() => ({ analytics: null })),
          api('/career/next-best-action').catch(() => ({ action: null })),
          api('/roadmap').catch(() => ({ roadmap: null, items: [] }))
        ]);
        if (analyticsRes?.analytics) setAnalytics(analyticsRes.analytics);
        if (actionRes?.action) setNextAction(actionRes.action);
        if (roadmapRes) setRoadmapData(roadmapRes);
      } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  if (loading) {
    return (
      <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: 400 }}>
        <div style={{ color: '#94a3b8', fontSize: 15 }}>Loading dashboard…</div>
      </div>
    );
  }

  const readiness = analytics?.readinessScore || 0;

  // Calculate live roadmap stats
  const totalTasks = roadmapData?.items?.length || analytics?.tasks?.total || 0;
  const completedTasks = roadmapData?.items
    ? roadmapData.items.filter(i => i.status === 'completed').length
    : (analytics?.tasks?.completed || 0);
  const progressPercent = totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : (analytics?.tasks?.percent || 0);
  const nextPendingTask = roadmapData?.items?.find(i => i.status !== 'completed');

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 28 }}>
      {/* Header */}
      <div>
        <h1 style={{ fontSize: 26, fontWeight: 700, marginBottom: 4 }}>
          Welcome back, <span className="gradient-text">{user?.fullName?.split(' ')[0] || 'Engineer'}</span>
        </h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>
          {profile?.target_role ? `Tracking toward: ${profile.target_role}` : 'Set your career target to begin.'}
        </p>
      </div>

      {/* Next Best Action Card */}
      {nextAction && (
        <div className="glass-card glow-primary" style={{ padding: '28px 28px 24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
            <span className="badge badge-primary" style={{ fontSize: 11 }}>NEXT BEST ACTION</span>
            <span className="badge badge-warning" style={{ fontSize: 11 }}>{nextAction.priority.toUpperCase()}</span>
          </div>
          <h2 style={{ fontSize: 20, fontWeight: 700, marginBottom: 8 }}>{nextAction.title}</h2>
          <p style={{ color: '#94a3b8', fontSize: 13, lineHeight: 1.7, marginBottom: 6 }}>
            {nextAction.whyItMatters}
          </p>
          <p style={{ color: '#64748b', fontSize: 12, marginBottom: 16 }}>
            Evidence: {nextAction.evidence}
          </p>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <button
              className="btn-primary"
              onClick={() => {
                const route = nextAction.targetRoute.replace('/', '');
                onNavigate(route);
              }}
              id="nba-cta"
            >
              {nextAction.ctaText} →
            </button>
            <span style={{ color: '#64748b', fontSize: 12 }}>
              ~{nextAction.estimatedMinutes} min
            </span>
          </div>
        </div>
      )}

      {/* Visual Roadmap Progress Summary Tracker */}
      <div
        className="glass-card"
        style={{
          padding: '24px 26px',
          background: 'linear-gradient(145deg, rgba(15, 23, 42, 0.85) 0%, rgba(19, 29, 53, 0.9) 100%)',
          border: '1px solid rgba(59, 130, 246, 0.3)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.25), 0 0 16px rgba(59, 130, 246, 0.08)'
        }}
        id="roadmap-progress-tracker"
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16, marginBottom: 18 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <span className="badge badge-primary" style={{ fontSize: 10, letterSpacing: '0.05em' }}>
                CAREER TRAJECTORY
              </span>
              <span style={{ color: '#94a3b8', fontSize: 13, fontWeight: 500 }}>
                {roadmapData?.roadmap?.title || (profile?.target_role ? `${profile.target_role} Roadmap` : 'Active Roadmap')}
              </span>
            </div>
            <h2 style={{ fontSize: 20, fontWeight: 700, color: '#f8fafc', letterSpacing: '-0.01em' }}>
              Roadmap Progress Summary
            </h2>
          </div>

          <button
            className="btn-secondary"
            style={{ padding: '8px 14px', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6 }}
            onClick={() => onNavigate('roadmap')}
          >
            Manage Full Roadmap →
          </button>
        </div>

        {totalTasks === 0 ? (
          <div style={{
            background: 'rgba(30, 41, 59, 0.5)',
            borderRadius: 12,
            padding: '24px 20px',
            textAlign: 'center',
            border: '1px dashed rgba(100, 116, 139, 0.3)'
          }}>
            <p style={{ color: '#cbd5e1', fontSize: 14, marginBottom: 12 }}>
              No active career roadmap milestones found yet.
            </p>
            <button
              className="btn-primary"
              style={{ fontSize: 13, padding: '8px 16px' }}
              onClick={() => onNavigate('roadmap')}
            >
              Generate AI Career Roadmap
            </button>
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
            {/* Top Score & Master Progress Bar */}
            <div>
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 8
              }}>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                  <span style={{
                    fontSize: 30,
                    fontWeight: 800,
                    color: progressPercent === 100 ? '#10b981' : '#38bdf8',
                    lineHeight: 1
                  }}>
                    {progressPercent}%
                  </span>
                  <span style={{ fontSize: 13, color: '#94a3b8', fontWeight: 500 }}>
                    Milestones Completed
                  </span>
                </div>

                <div style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 8,
                  background: 'rgba(15, 23, 42, 0.6)',
                  padding: '6px 12px',
                  borderRadius: 20,
                  border: '1px solid rgba(51, 65, 85, 0.5)'
                }}>
                  <span style={{
                    width: 8,
                    height: 8,
                    borderRadius: '50%',
                    backgroundColor: progressPercent === 100 ? '#10b981' : progressPercent > 0 ? '#38bdf8' : '#f59e0b',
                    boxShadow: `0 0 8px ${progressPercent === 100 ? '#10b981' : '#38bdf8'}`
                  }} />
                  <span style={{ fontSize: 12, fontWeight: 600, color: '#e2e8f0' }}>
                    {completedTasks} of {totalTasks} Milestones Done
                  </span>
                </div>
              </div>

              {/* Master Progress Bar Container */}
              <div className="progress-track-container">
                <div
                  className="progress-bar-fill"
                  style={{
                    width: `${Math.max(2, progressPercent)}%`,
                    background: progressPercent === 100
                      ? 'linear-gradient(90deg, #10b981 0%, #34d399 100%)'
                      : 'linear-gradient(90deg, #2563eb 0%, #06b6d4 50%, #10b981 100%)'
                  }}
                />
              </div>
            </div>

            {/* Concise Upcoming Milestone Card */}
            {nextPendingTask && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: 12,
                background: 'rgba(30, 41, 59, 0.45)',
                border: '1px solid rgba(59, 130, 246, 0.25)',
                borderRadius: 10,
                padding: '12px 16px'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 12, minWidth: 200, flex: 1 }}>
                  <div style={{
                    width: 26,
                    height: 26,
                    borderRadius: 6,
                    border: '1px solid rgba(59, 130, 246, 0.4)',
                    background: 'rgba(37, 99, 235, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#60a5fa',
                    fontSize: 13,
                    fontWeight: 700
                  }}>
                    →
                  </div>
                  <div>
                    <div style={{ fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                      Up Next · Phase {nextPendingTask.phase_number}
                    </div>
                    <div style={{ fontSize: 13.5, fontWeight: 600, color: '#f1f5f9' }}>
                      {nextPendingTask.title}
                    </div>
                  </div>
                </div>

                <button
                  onClick={() => onNavigate('roadmap')}
                  className="btn-secondary"
                  style={{ fontSize: 12, padding: '6px 14px' }}
                >
                  View in Roadmap →
                </button>
              </div>
            )}
          </div>
        )}
      </div>

      {/* Metrics Grid */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 16 }}>
        {/* Readiness Score */}
        <MetricCard
          label="Career Readiness"
          value={`${readiness}%`}
          detail={analytics?.targetRole || 'Not set'}
          color="#3b82f6"
        />
        {/* Tasks */}
        <MetricCard
          label="Roadmap Tasks"
          value={totalTasks > 0 ? `${completedTasks}/${totalTasks}` : '—'}
          detail={totalTasks > 0 ? `${progressPercent}% complete` : 'No roadmap yet'}
          color="#10b981"
        />
        {/* Skills */}
        <MetricCard
          label="Skills Acquired"
          value={`${analytics?.skills.acquired || 0}`}
          detail={`${analytics?.skills.gapsIdentified || 0} gaps identified`}
          color="#8b5cf6"
        />
        {/* Interviews */}
        <MetricCard
          label="Mock Interviews"
          value={`${analytics?.interviews.completed || 0}`}
          detail={analytics?.interviews.completed ? `Avg score: ${analytics.interviews.averageScore}%` : 'No interviews yet'}
          color="#06b6d4"
        />
        {/* Projects */}
        <MetricCard
          label="Portfolio Projects"
          value={`${analytics?.projects.total || 0}`}
          detail={`${analytics?.projects.completed || 0} completed`}
          color="#f59e0b"
        />
        {/* Resume */}
        <MetricCard
          label="Resume ATS Score"
          value={analytics?.resume ? `${analytics.resume.overallScore}%` : '—'}
          detail={analytics?.resume ? 'Last analysis available' : 'No resume analyzed'}
          color="#ef4444"
        />
      </div>

      {/* Recent Activity */}
      <div className="glass-card" style={{ padding: '20px 24px' }}>
        <h3 style={{ fontSize: 15, fontWeight: 600, marginBottom: 16, color: '#e2e8f0' }}>Recent Activity</h3>
        {(!analytics?.recentActivity || analytics.recentActivity.length === 0) ? (
          <p style={{ color: '#64748b', fontSize: 13 }}>No activity yet. Complete actions above to build your history.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {analytics.recentActivity.slice(0, 6).map((evt, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '8px 0', borderBottom: '1px solid rgba(51,65,85,0.3)'
              }}>
                <span style={{ fontSize: 13, color: '#cbd5e1' }}>
                  {evt.eventName.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())}
                </span>
                <span style={{ fontSize: 11, color: '#64748b' }}>
                  {new Date(evt.timestamp).toLocaleString()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}

function MetricCard({ label, value, detail, color }: { label: string; value: string; detail: string; color: string }) {
  return (
    <div className="glass-card" style={{ padding: '20px 22px' }}>
      <div style={{
        width: 8, height: 8, borderRadius: '50%',
        background: color, marginBottom: 12,
        boxShadow: `0 0 10px ${color}60`
      }} />
      <div style={{ fontSize: 26, fontWeight: 700, color: '#f1f5f9', marginBottom: 4 }}>{value}</div>
      <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 2 }}>{label}</div>
      <div style={{ fontSize: 11, color: '#64748b' }}>{detail}</div>
    </div>
  );
}

