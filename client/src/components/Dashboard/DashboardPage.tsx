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
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try {
        const [analyticsRes, actionRes] = await Promise.all([
          api('/analytics/dashboard'),
          api('/career/next-best-action'),
        ]);
        setAnalytics(analyticsRes.analytics);
        setNextAction(actionRes.action);
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
          value={analytics?.tasks.total ? `${analytics.tasks.completed}/${analytics.tasks.total}` : '—'}
          detail={analytics?.tasks.total ? `${analytics.tasks.percent}% complete` : 'No roadmap yet'}
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
