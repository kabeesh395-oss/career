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
  const [togglingTaskId, setTogglingTaskId] = useState<string | null>(null);

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

  const handleToggleTask = async (itemId: string) => {
    setTogglingTaskId(itemId);
    try {
      const data = await api(`/roadmap/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({}) });
      if (data?.item && data?.roadmap) {
        setRoadmapData(prev => {
          if (!prev) return null;
          const updatedItems = prev.items.map(i => i.id === itemId ? data.item : i);
          return {
            roadmap: data.roadmap,
            items: updatedItems
          };
        });
        // Update analytics snapshot
        setAnalytics(prev => {
          if (!prev) return null;
          const total = data.roadmap.total_tasks || prev.tasks.total;
          const completed = data.roadmap.completed_tasks ?? prev.tasks.completed;
          const percent = total > 0 ? Math.round((completed / total) * 100) : 0;
          return {
            ...prev,
            tasks: { total, completed, percent }
          };
        });
      }
    } catch { /* empty */ }
    setTogglingTaskId(null);
  };

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

  // Group items by phase for the visual breakdown
  const phaseMap: Record<number, { title: string; items: RoadmapItem[] }> = {};
  if (roadmapData?.items) {
    roadmapData.items.forEach(item => {
      const phaseNum = item.phase_number || 1;
      if (!phaseMap[phaseNum]) {
        phaseMap[phaseNum] = {
          title: item.phase_title || `Phase ${phaseNum}`,
          items: []
        };
      }
      phaseMap[phaseNum].items.push(item);
    });
  }
  const phases = Object.entries(phaseMap).map(([phaseNum, data]) => {
    const pTotal = data.items.length;
    const pCompleted = data.items.filter(i => i.status === 'completed').length;
    const pPercent = pTotal > 0 ? Math.round((pCompleted / pTotal) * 100) : 0;
    return {
      phaseNumber: Number(phaseNum),
      title: data.title,
      total: pTotal,
      completed: pCompleted,
      percent: pPercent,
      isCompleted: pPercent === 100 && pTotal > 0,
      isCurrent: pPercent < 100 && (Number(phaseNum) === 1 || (phaseMap[Number(phaseNum) - 1]?.items.every(i => i.status === 'completed') ?? true))
    };
  });

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

      {/* Visual Roadmap Progress Tracker */}
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
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16, marginBottom: 20 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
              <span className="badge badge-primary" style={{ fontSize: 10, letterSpacing: '0.05em' }}>
                CAREER TRAJECTORY
              </span>
              <span style={{ color: '#94a3b8', fontSize: 13, fontWeight: 500 }}>
                {roadmapData?.roadmap?.title || (profile?.target_role ? `${profile.target_role} Roadmap` : 'Active Roadmap')}
              </span>
            </div>
            <h2 style={{ fontSize: 22, fontWeight: 700, color: '#f8fafc', letterSpacing: '-0.01em' }}>
              Roadmap Velocity & Completion
            </h2>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
            <button
              className="btn-secondary"
              style={{ padding: '8px 14px', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6 }}
              onClick={() => onNavigate('roadmap')}
            >
              View Full Roadmap →
            </button>
          </div>
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
              No active career roadmap tasks found yet.
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
          <div>
            {/* Top Score & Master Progress Bar */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between',
              marginBottom: 10
            }}>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                <span style={{
                  fontSize: 34,
                  fontWeight: 800,
                  color: progressPercent === 100 ? '#10b981' : '#38bdf8',
                  lineHeight: 1
                }}>
                  {progressPercent}%
                </span>
                <span style={{ fontSize: 14, color: '#94a3b8', fontWeight: 500 }}>
                  Completed
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
                  {completedTasks} of {totalTasks} Tasks Done
                </span>
              </div>
            </div>

            {/* Master Progress Bar Container */}
            <div className="progress-track-container" style={{ marginBottom: 20 }}>
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

            {/* Phase Breakdown Grid */}
            {phases.length > 0 && (
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))',
                gap: 12,
                marginBottom: 18
              }}>
                {phases.map(phase => (
                  <div
                    key={phase.phaseNumber}
                    style={{
                      background: phase.isCompleted
                        ? 'rgba(16, 185, 129, 0.08)'
                        : phase.isCurrent
                          ? 'rgba(59, 130, 246, 0.08)'
                          : 'rgba(15, 23, 42, 0.5)',
                      border: `1px solid ${
                        phase.isCompleted
                          ? 'rgba(16, 185, 129, 0.3)'
                          : phase.isCurrent
                            ? 'rgba(59, 130, 246, 0.4)'
                            : 'rgba(51, 65, 85, 0.4)'
                      }`,
                      borderRadius: 10,
                      padding: '12px 14px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 8
                    }}
                  >
                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                      <span style={{ fontSize: 11, fontWeight: 700, color: phase.isCompleted ? '#34d399' : phase.isCurrent ? '#60a5fa' : '#94a3b8' }}>
                        PHASE {phase.phaseNumber}
                      </span>
                      <span style={{
                        fontSize: 11,
                        fontWeight: 600,
                        padding: '2px 6px',
                        borderRadius: 6,
                        background: phase.isCompleted ? 'rgba(16, 185, 129, 0.2)' : phase.isCurrent ? 'rgba(59, 130, 246, 0.2)' : 'rgba(51, 65, 85, 0.4)',
                        color: phase.isCompleted ? '#10b981' : phase.isCurrent ? '#38bdf8' : '#64748b'
                      }}>
                        {phase.isCompleted ? '✓ 100%' : `${phase.percent}%`}
                      </span>
                    </div>

                    <div style={{
                      fontSize: 13,
                      fontWeight: 600,
                      color: '#f1f5f9',
                      overflow: 'hidden',
                      textOverflow: 'ellipsis',
                      whiteSpace: 'nowrap'
                    }}>
                      {phase.title.replace(/^Phase \d+:\s*/i, '')}
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', fontSize: 11, color: '#94a3b8' }}>
                      <span>{phase.completed}/{phase.total} tasks</span>
                      <span>{phase.isCompleted ? 'Completed' : phase.isCurrent ? 'In Progress' : 'Pending'}</span>
                    </div>

                    {/* Micro Progress Bar */}
                    <div style={{
                      width: '100%',
                      height: 5,
                      backgroundColor: 'rgba(30, 41, 59, 0.8)',
                      borderRadius: 3,
                      overflow: 'hidden'
                    }}>
                      <div
                        className="micro-progress-fill"
                        style={{
                          width: `${phase.percent}%`,
                          backgroundColor: phase.isCompleted ? '#10b981' : phase.isCurrent ? '#3b82f6' : '#64748b'
                        }}
                      />
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* Immediate Next Task Milestone Action */}
            {nextPendingTask && (
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: 12,
                background: 'rgba(30, 41, 59, 0.4)',
                border: '1px solid rgba(59, 130, 246, 0.2)',
                borderRadius: 10,
                padding: '10px 14px'
              }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 10, minWidth: 200, flex: 1 }}>
                  <div style={{
                    width: 24,
                    height: 24,
                    borderRadius: 6,
                    border: '1px solid rgba(59, 130, 246, 0.4)',
                    background: 'rgba(37, 99, 235, 0.1)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#60a5fa',
                    fontSize: 12,
                    fontWeight: 700
                  }}>
                    →
                  </div>
                  <div>
                    <div style={{ fontSize: 11, color: '#94a3b8', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                      Up Next in Phase {nextPendingTask.phase_number}
                    </div>
                    <div style={{ fontSize: 13, fontWeight: 600, color: '#f1f5f9' }}>
                      {nextPendingTask.title}
                    </div>
                  </div>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                  <button
                    onClick={() => handleToggleTask(nextPendingTask.id)}
                    disabled={togglingTaskId === nextPendingTask.id}
                    style={{
                      background: 'rgba(16, 185, 129, 0.15)',
                      border: '1px solid rgba(16, 185, 129, 0.4)',
                      color: '#34d399',
                      borderRadius: 6,
                      padding: '6px 12px',
                      fontSize: 12,
                      fontWeight: 600,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: 4
                    }}
                  >
                    {togglingTaskId === nextPendingTask.id ? 'Saving…' : '✓ Mark Done'}
                  </button>
                </div>
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

