import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadAnalytics = async () => {
    try {
      const data = await api('/analytics/dashboard');
      setAnalytics(data.analytics || null);
    } catch (err: any) {
      setError(err.message || 'Failed to load platform analytics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  if (loading) {
    return <div style={{ color: '#94a3b8', padding: 40 }}>Loading verified user activity analytics…</div>;
  }

  const hasAnyData = analytics?.hasSufficientData;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Page Header */}
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4, color: '#f8fafc' }}>
          Activity & Readiness Analytics
        </h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>
          Empirical telemetry calculated strictly from your genuine resume uploads, skill validations, roadmap milestones, and interview simulations.
        </p>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '12px 16px', color: '#f87171', fontSize: 13 }}>
          {error}
        </div>
      )}

      {/* Global Readiness Metric Banner */}
      <div className="glass-card" style={{ padding: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
          <div>
            <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', letterSpacing: '0.05em', marginBottom: 4 }}>
              Empirical Career Readiness Score
            </div>
            <div style={{ display: 'flex', alignItems: 'baseline', gap: 10 }}>
              <span style={{ fontSize: 38, fontWeight: 800, color: hasAnyData ? '#3b82f6' : '#64748b' }}>
                {hasAnyData ? `${analytics.readinessScore}%` : '0%'}
              </span>
              <span className={`badge ${hasAnyData ? 'badge-primary' : 'badge-neutral'}`}>
                {hasAnyData ? 'Live Calculated' : 'No Data Yet'}
              </span>
            </div>
            <p style={{ color: '#64748b', fontSize: 12, marginTop: 6, maxWidth: 600 }}>
              {hasAnyData
                ? `Calibrated for ${analytics.targetRole || 'Target Role'} based on multi-dimensional telemetry: verified skill matrix, completed roadmap items, ATS impact, and mock interview rubric evaluations.`
                : 'Complete real actions (upload a resume, assess skills, complete roadmap items, or try a mock interview) to calculate your readiness rating.'}
            </p>
          </div>

          <div style={{ width: 160 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: 11, color: '#94a3b8', marginBottom: 4 }}>
              <span>Verification</span>
              <span>{hasAnyData ? `${analytics.readinessScore}/100` : '0/100'}</span>
            </div>
            <div style={{ width: '100%', height: 8, background: 'rgba(51, 65, 85, 0.4)', borderRadius: 4, overflow: 'hidden' }}>
              <div style={{
                width: hasAnyData ? `${analytics.readinessScore}%` : '0%',
                height: '100%',
                background: 'linear-gradient(90deg, #3b82f6, #10b981)',
                borderRadius: 4
              }} />
            </div>
          </div>
        </div>
      </div>

      {/* 5 Core Pillars: Resume, Skills, Roadmap, Interviews, Learning */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: 16 }}>
        {/* 1. Resume Pillar */}
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Resume Analyses
          </div>
          {analytics?.resume?.hasData ? (
            <div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#3b82f6', marginBottom: 2 }}>
                {analytics.resume.overallScore}%
              </div>
              <div style={{ fontSize: 12, color: '#cbd5e1' }}>
                Latest ATS Score · {analytics.resume.totalUploaded} uploaded
              </div>
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 8 }}>
                Impact: {analytics.resume.impactScore}% · Brevity: {analytics.resume.brevityScore}%
              </div>
            </div>
          ) : (
            <div style={{ padding: '8px 0', color: '#64748b', fontSize: 13 }}>
              No data yet
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                Upload a resume to see ATS metrics.
              </div>
            </div>
          )}
        </div>

        {/* 2. Skill Progress */}
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Skill Matrix
          </div>
          {analytics?.skills?.hasData ? (
            <div>
              <div style={{ display: 'flex', alignItems: 'baseline', gap: 8, marginBottom: 2 }}>
                <span style={{ fontSize: 28, fontWeight: 700, color: '#8b5cf6' }}>
                  {analytics.skills.acquired}
                </span>
                <span style={{ fontSize: 13, color: '#f59e0b' }}>
                  / {analytics.skills.gapsIdentified} gaps
                </span>
              </div>
              <div style={{ fontSize: 12, color: '#cbd5e1' }}>
                Acquired competencies
              </div>
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 8 }}>
                Compared against target industry role.
              </div>
            </div>
          ) : (
            <div style={{ padding: '8px 0', color: '#64748b', fontSize: 13 }}>
              No data yet
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                Set your target role in Profile to evaluate skills.
              </div>
            </div>
          )}
        </div>

        {/* 3. Roadmap Progress */}
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Roadmap Milestones
          </div>
          {analytics?.tasks?.hasData ? (
            <div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#10b981', marginBottom: 2 }}>
                {analytics.tasks.percent}%
              </div>
              <div style={{ fontSize: 12, color: '#cbd5e1' }}>
                {analytics.tasks.completed} of {analytics.tasks.total} tasks completed
              </div>
              <div style={{ width: '100%', height: 4, background: 'rgba(51, 65, 85, 0.4)', borderRadius: 2, marginTop: 8, overflow: 'hidden' }}>
                <div style={{ width: `${analytics.tasks.percent}%`, height: '100%', background: '#10b981' }} />
              </div>
            </div>
          ) : (
            <div style={{ padding: '8px 0', color: '#64748b', fontSize: 13 }}>
              No data yet
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                Generate a roadmap to track milestones.
              </div>
            </div>
          )}
        </div>

        {/* 4. Interview Simulations */}
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Interview Performance
          </div>
          {analytics?.interviews?.hasData ? (
            <div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#f59e0b', marginBottom: 2 }}>
                {analytics.interviews.averageScore}%
              </div>
              <div style={{ fontSize: 12, color: '#cbd5e1' }}>
                Avg Score ({analytics.interviews.completed} completed)
              </div>
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 8 }}>
                Derived from STAR rubric evaluations.
              </div>
            </div>
          ) : (
            <div style={{ padding: '8px 0', color: '#64748b', fontSize: 13 }}>
              No data yet
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                Complete a mock interview round to see scores.
              </div>
            </div>
          )}
        </div>

        {/* 5. Learning Activity */}
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 600, textTransform: 'uppercase', marginBottom: 6 }}>
            Curated Learning
          </div>
          {analytics?.learning?.hasData ? (
            <div>
              <div style={{ fontSize: 28, fontWeight: 700, color: '#06b6d4', marginBottom: 2 }}>
                {analytics.learning.completedCount}
              </div>
              <div style={{ fontSize: 12, color: '#cbd5e1' }}>
                Modules completed · {analytics.learning.inProgressCount} in progress
              </div>
              <div style={{ fontSize: 11, color: '#64748b', marginTop: 8 }}>
                Verified technical curriculum progress.
              </div>
            </div>
          ) : (
            <div style={{ padding: '8px 0', color: '#64748b', fontSize: 13 }}>
              No data yet
              <div style={{ fontSize: 11, color: '#94a3b8', marginTop: 4 }}>
                Engage with learning resources to log progress.
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Real Telemetry Activity Feed */}
      <div className="glass-card" style={{ padding: 24 }}>
        <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc', marginBottom: 4 }}>
          Real-Time Audit & Event Stream
        </h3>
        <p style={{ color: '#94a3b8', fontSize: 13, marginBottom: 16 }}>
          Historical audit events captured directly from your session actions.
        </p>

        {(!analytics?.recentActivity || analytics.recentActivity.length === 0) ? (
          <div style={{ padding: '24px 0', textAlign: 'center', color: '#64748b', fontSize: 13 }}>
            No activity events recorded yet. Perform actions across the platform to populate the audit stream.
          </div>
        ) : (
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {analytics.recentActivity.map((act: any, i: number) => (
              <div
                key={i}
                style={{
                  display: 'flex',
                  justifyContent: 'space-between',
                  alignItems: 'center',
                  padding: '10px 14px',
                  background: 'rgba(15, 23, 42, 0.5)',
                  border: '1px solid rgba(51, 65, 85, 0.4)',
                  borderRadius: 8,
                  flexWrap: 'wrap',
                  gap: 8
                }}
              >
                <div>
                  <span style={{ fontSize: 13, fontWeight: 600, color: '#e2e8f0', fontFamily: 'var(--font-mono)' }}>
                    {act.eventName}
                  </span>
                  {act.data && Object.keys(act.data).length > 0 && (
                    <span style={{ fontSize: 12, color: '#94a3b8', marginLeft: 8 }}>
                      ({Object.entries(act.data).slice(0, 2).map(([k, v]) => `${k}: ${v}`).join(', ')})
                    </span>
                  )}
                </div>
                <span style={{ fontSize: 11, color: '#64748b' }}>
                  {new Date(act.timestamp).toLocaleTimeString()} · {new Date(act.timestamp).toLocaleDateString()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
