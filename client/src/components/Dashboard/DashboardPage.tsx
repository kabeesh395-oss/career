import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import PatternWaves from '../Common/PatternWaves';
import { Sparkles, ArrowRight, ShieldCheck, Target, Zap, FileText, CheckCircle2, ChevronRight, Activity } from 'lucide-react';

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
      <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: 380, gap: 12 }}>
        <div style={{
          width: 36,
          height: 36,
          borderRadius: '50%',
          border: '3px solid rgba(59, 130, 246, 0.2)',
          borderTopColor: '#3b82f6',
          animation: 'spin 0.8s linear infinite'
        }} />
        <div style={{ color: '#94a3b8', fontSize: 14, fontWeight: 500 }}>Calibrating dashboard metrics…</div>
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

  const targetRoleName = profile?.target_role || analytics?.targetRole || 'Full Stack Engineer';

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      
      {/* Premium Hero Welcome Banner with PatternWaves */}
      <div
        className="glass-card"
        style={{
          position: 'relative',
          borderRadius: 18,
          padding: '28px 28px 24px',
          background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.95) 0%, rgba(30, 41, 59, 0.85) 100%)',
          border: '1px solid rgba(59, 130, 246, 0.25)',
          overflow: 'hidden',
          boxShadow: '0 12px 36px -8px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.04)'
        }}
      >
        {/* Subtle WebGL PatternWaves Ambient Layer */}
        <div style={{
          position: 'absolute',
          inset: 0,
          zIndex: 0,
          opacity: 0.22,
          pointerEvents: 'auto'
        }}>
          <PatternWaves
            preset="silk"
            color="#38bdf8"
            backgroundColor="transparent"
            fade="edges"
            fadeSize={0.6}
            interactive={true}
            speed={0.2}
            markSize={0.85}
            cursorSize={45}
            cursorStrength={0.5}
          />
        </div>

        {/* Foreground Content */}
        <div style={{ position: 'relative', zIndex: 1, display: 'flex', flexDirection: 'column', gap: 18 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: 16 }}>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 6 }}>
                <span className="badge badge-primary" style={{ fontSize: 11, fontWeight: 600, display: 'inline-flex', alignItems: 'center', gap: 4 }}>
                  <Zap size={11} /> CAREER RADAR ACTIVE
                </span>
                <span style={{ fontSize: 12, color: '#94a3b8', fontWeight: 500 }}>
                  Role: <strong style={{ color: '#e2e8f0' }}>{targetRoleName}</strong>
                </span>
              </div>
              <h1 style={{ fontSize: 26, fontWeight: 800, color: '#f8fafc', letterSpacing: '-0.02em', margin: 0 }}>
                Welcome back, <span className="gradient-text">{user?.fullName?.split(' ')[0] || 'Developer'}</span>
              </h1>
              <p style={{ color: '#94a3b8', fontSize: 13.5, margin: '6px 0 0', maxWidth: 640, lineHeight: 1.5 }}>
                Your engineering milestones and skill gaps are actively synchronized. Complete high-priority tasks to improve role readiness.
              </p>
            </div>

            {/* Readiness Index Metric Pill */}
            <div style={{
              display: 'flex',
              alignItems: 'center',
              gap: 14,
              padding: '10px 18px',
              borderRadius: 14,
              background: 'rgba(15, 23, 42, 0.75)',
              border: '1px solid rgba(59, 130, 246, 0.35)',
              backdropFilter: 'blur(12px)'
            }}>
              <div>
                <div style={{ fontSize: 11, textTransform: 'uppercase', color: '#94a3b8', letterSpacing: '0.04em', fontWeight: 600 }}>
                  Readiness Index
                </div>
                <div style={{ fontSize: 22, fontWeight: 800, color: readiness >= 70 ? '#34d399' : '#38bdf8', lineHeight: 1.1 }}>
                  {readiness > 0 ? `${readiness}%` : 'Calibrating'}
                </div>
              </div>
              <div style={{
                width: 38,
                height: 38,
                borderRadius: '50%',
                background: 'rgba(59, 130, 246, 0.15)',
                border: '1px solid rgba(59, 130, 246, 0.4)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#60a5fa'
              }}>
                <Target size={18} />
              </div>
            </div>
          </div>

          {/* Quick Action Buttons */}
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', paddingTop: 4 }}>
            <button
              className="btn-primary"
              onClick={() => onNavigate('resume')}
              style={{ fontSize: 13, padding: '7px 14px', display: 'inline-flex', alignItems: 'center', gap: 6 }}
            >
              <FileText size={14} /> Scan Resume
            </button>
            <button
              className="btn-secondary"
              onClick={() => onNavigate('roadmap')}
              style={{ fontSize: 13, padding: '7px 14px', display: 'inline-flex', alignItems: 'center', gap: 6 }}
            >
              <Target size={14} /> View Roadmap
            </button>
            <button
              className="btn-secondary"
              onClick={() => onNavigate('career')}
              style={{ fontSize: 13, padding: '7px 14px', display: 'inline-flex', alignItems: 'center', gap: 6 }}
            >
              <Sparkles size={14} /> Calibrate Skills
            </button>
            <button
              className="btn-secondary"
              onClick={() => onNavigate('interview')}
              style={{ fontSize: 13, padding: '7px 14px', display: 'inline-flex', alignItems: 'center', gap: 6 }}
            >
              <ShieldCheck size={14} /> Mock Interview
            </button>
          </div>
        </div>
      </div>

      {/* Next Best Action Card */}
      {nextAction && (
        <div
          className="glass-card glow-primary"
          style={{
            padding: '24px 26px',
            borderRadius: 16,
            background: 'linear-gradient(135deg, rgba(15, 23, 42, 0.9) 0%, rgba(26, 39, 68, 0.85) 100%)',
            border: '1px solid rgba(59, 130, 246, 0.35)',
            boxShadow: '0 8px 30px rgba(0, 0, 0, 0.25)'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 10 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <span className="badge badge-primary" style={{ fontSize: 10, letterSpacing: '0.04em' }}>
                RECOMMENDED FOCUS
              </span>
              <span className="badge badge-warning" style={{ fontSize: 10 }}>
                {nextAction.priority.toUpperCase()}
              </span>
            </div>
            <span style={{ color: '#94a3b8', fontSize: 12 }}>
              Estimated: ~{nextAction.estimatedMinutes} min
            </span>
          </div>
          <h2 style={{ fontSize: 19, fontWeight: 700, color: '#f8fafc', marginBottom: 6 }}>
            {nextAction.title}
          </h2>
          <p style={{ color: '#cbd5e1', fontSize: 13.5, lineHeight: 1.6, marginBottom: 6 }}>
            {nextAction.whyItMatters}
          </p>
          <p style={{ color: '#64748b', fontSize: 12, marginBottom: 14 }}>
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
              style={{ fontSize: 13.5, padding: '8px 16px', display: 'inline-flex', alignItems: 'center', gap: 6 }}
            >
              {nextAction.ctaText} <ArrowRight size={14} />
            </button>
          </div>
        </div>
      )}

      {/* Visual Roadmap Progress Summary Tracker */}
      <div
        className="glass-card"
        style={{
          padding: '24px 26px',
          borderRadius: 16,
          background: 'linear-gradient(145deg, rgba(15, 23, 42, 0.85) 0%, rgba(19, 29, 53, 0.9) 100%)',
          border: '1px solid rgba(59, 130, 246, 0.25)',
          boxShadow: '0 8px 32px rgba(0, 0, 0, 0.25)'
        }}
        id="roadmap-progress-tracker"
      >
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 14, marginBottom: 18 }}>
          <div>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
              <span className="badge badge-primary" style={{ fontSize: 10, letterSpacing: '0.05em' }}>
                CAREER TRAJECTORY
              </span>
              <span style={{ color: '#94a3b8', fontSize: 13, fontWeight: 500 }}>
                {roadmapData?.roadmap?.title || (profile?.target_role ? `${profile.target_role} Roadmap` : 'Active Roadmap')}
              </span>
            </div>
            <h2 style={{ fontSize: 20, fontWeight: 700, color: '#f8fafc', letterSpacing: '-0.01em', margin: 0 }}>
              Roadmap Progress Summary
            </h2>
          </div>

          <button
            className="btn-secondary"
            style={{ padding: '7px 14px', fontSize: 13, display: 'flex', alignItems: 'center', gap: 6 }}
            onClick={() => onNavigate('roadmap')}
          >
            Manage Full Roadmap <ChevronRight size={14} />
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
            {/* Master Progress Bar */}
            <div>
              <div style={{
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                marginBottom: 8
              }}>
                <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
                  <span style={{
                    fontSize: 28,
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
                  padding: '5px 12px',
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

              {/* Master Progress Track */}
              <div className="progress-track-container" style={{ height: 8, borderRadius: 6, background: 'rgba(30, 41, 59, 0.8)', overflow: 'hidden' }}>
                <div
                  className="progress-bar-fill"
                  style={{
                    height: '100%',
                    borderRadius: 6,
                    width: `${Math.max(2, progressPercent)}%`,
                    background: progressPercent === 100
                      ? 'linear-gradient(90deg, #10b981 0%, #34d399 100%)'
                      : 'linear-gradient(90deg, #2563eb 0%, #06b6d4 50%, #10b981 100%)',
                    transition: 'width 0.4s ease'
                  }}
                />
              </div>
            </div>

            {/* Upcoming Milestone Card */}
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
                    width: 28,
                    height: 28,
                    borderRadius: 7,
                    border: '1px solid rgba(59, 130, 246, 0.4)',
                    background: 'rgba(37, 99, 235, 0.15)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#60a5fa',
                    fontSize: 13,
                    fontWeight: 700
                  }}>
                    <ChevronRight size={16} />
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
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 14 }}>
        <MetricCard
          label="Career Readiness"
          value={`${readiness}%`}
          detail={targetRoleName}
          color="#3b82f6"
          icon={<Target size={16} color="#3b82f6" />}
        />
        <MetricCard
          label="Roadmap Tasks"
          value={totalTasks > 0 ? `${completedTasks}/${totalTasks}` : '—'}
          detail={totalTasks > 0 ? `${progressPercent}% complete` : 'No roadmap yet'}
          color="#10b981"
          icon={<CheckCircle2 size={16} color="#10b981" />}
        />
        <MetricCard
          label="Skills Acquired"
          value={`${analytics?.skills.acquired || 0}`}
          detail={`${analytics?.skills.gapsIdentified || 0} gaps identified`}
          color="#8b5cf6"
          icon={<Sparkles size={16} color="#8b5cf6" />}
        />
        <MetricCard
          label="Mock Interviews"
          value={`${analytics?.interviews.completed || 0}`}
          detail={analytics?.interviews.completed ? `Avg score: ${analytics.interviews.averageScore}%` : 'No interviews yet'}
          color="#06b6d4"
          icon={<ShieldCheck size={16} color="#06b6d4" />}
        />
        <MetricCard
          label="Portfolio Projects"
          value={`${analytics?.projects.total || 0}`}
          detail={`${analytics?.projects.completed || 0} completed`}
          color="#f59e0b"
          icon={<Zap size={16} color="#f59e0b" />}
        />
        <MetricCard
          label="Resume ATS Score"
          value={analytics?.resume ? `${analytics.resume.overallScore}%` : '—'}
          detail={analytics?.resume ? 'Latest ATS analysis' : 'No resume analyzed'}
          color="#ef4444"
          icon={<FileText size={16} color="#ef4444" />}
        />
      </div>

      {/* Recent Activity Audit Trail */}
      <div className="glass-card" style={{ padding: '20px 24px', borderRadius: 16 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 14 }}>
          <Activity size={16} color="#60a5fa" />
          <h3 style={{ fontSize: 15, fontWeight: 700, color: '#e2e8f0', margin: 0 }}>Verified Activity Stream</h3>
        </div>
        {(!analytics?.recentActivity || analytics.recentActivity.length === 0) ? (
          <p style={{ color: '#64748b', fontSize: 13, margin: 0 }}>No telemetry recorded yet. Complete roadmap milestones or scan resumes to generate verified events.</p>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            {analytics.recentActivity.slice(0, 6).map((evt, i) => (
              <div key={i} style={{
                display: 'flex', justifyContent: 'space-between', alignItems: 'center',
                padding: '8px 10px', borderRadius: 8, background: 'rgba(30, 41, 59, 0.3)', border: '1px solid rgba(51, 65, 85, 0.25)'
              }}>
                <span style={{ fontSize: 13, color: '#cbd5e1', fontWeight: 500 }}>
                  {evt.eventName.replace(/_/g, ' ').replace(/\b\w/g, c => c.toUpperCase())}
                </span>
                <span style={{ fontSize: 11.5, color: '#64748b' }}>
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

function MetricCard({ label, value, detail, color, icon }: { label: string; value: string; detail: string; color: string; icon?: React.ReactNode }) {
  return (
    <div className="glass-card" style={{ padding: '18px 20px', borderRadius: 14, position: 'relative', overflow: 'hidden' }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 10 }}>
        <div style={{
          width: 8, height: 8, borderRadius: '50%',
          background: color,
          boxShadow: `0 0 10px ${color}60`
        }} />
        {icon}
      </div>
      <div style={{ fontSize: 24, fontWeight: 800, color: '#f1f5f9', marginBottom: 3, letterSpacing: '-0.02em' }}>{value}</div>
      <div style={{ fontSize: 12.5, fontWeight: 600, color: '#94a3b8', marginBottom: 2 }}>{label}</div>
      <div style={{ fontSize: 11, color: '#64748b' }}>{detail}</div>
    </div>
  );
}
