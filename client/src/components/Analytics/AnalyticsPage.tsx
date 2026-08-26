import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function AnalyticsPage() {
  const [analytics, setAnalytics] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [customEvent, setCustomEvent] = useState({ name: '', data: '' });
  const [submitting, setSubmitting] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  const loadAnalytics = async () => {
    try {
      const data = await api('/analytics/dashboard');
      setAnalytics(data.analytics || null);
    } catch { /* empty */ }
    setLoading(false);
  };

  useEffect(() => {
    loadAnalytics();
  }, []);

  const handleTrackEvent = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!customEvent.name) return;
    setSubmitting(true);
    setError('');
    setMessage('');
    try {
      let parsedData = {};
      if (customEvent.data) {
        try {
          parsedData = JSON.parse(customEvent.data);
        } catch {
          throw new Error('Invalid JSON payload in event data.');
        }
      }
      await api('/analytics/event', {
        method: 'POST',
        body: JSON.stringify({
          eventName: customEvent.name,
          eventData: parsedData
        })
      });
      setMessage(`Successfully tracked event: "${customEvent.name}"`);
      setCustomEvent({ name: '', data: '' });
      await loadAnalytics();
    } catch (err: any) {
      setError(err.message || 'Failed to track event.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading analytics…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Telemetry & Platform Analytics</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Real-time user engagement metrics, activity tracking, and system performance telemetry.</p>
      </div>

      {/* Analytics Overview */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: 18 }}>
        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ color: '#94a3b8', fontSize: 13, marginBottom: 8 }}>Ready Score Contribution</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <span style={{ fontSize: 32, fontWeight: 700, color: '#3b82f6' }}>{analytics?.readinessScore || 0}%</span>
            <span style={{ color: '#10b981', fontSize: 12, fontWeight: 500 }}>Calibrated</span>
          </div>
          <p style={{ color: '#64748b', fontSize: 11, marginTop: 8 }}>Derived from profile updates, task completions, resume reviews, and mock interviews.</p>
        </div>

        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ color: '#94a3b8', fontSize: 13, marginBottom: 8 }}>Roadmap Mastery</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <span style={{ fontSize: 32, fontWeight: 700, color: '#10b981' }}>{analytics?.tasks.percent || 0}%</span>
            <span style={{ color: '#94a3b8', fontSize: 12 }}>({analytics?.tasks.completed}/{analytics?.tasks.total})</span>
          </div>
          <div style={{ width: '100%', height: 5, background: 'rgba(51,65,85,0.4)', borderRadius: 3, marginTop: 10, overflow: 'hidden' }}>
            <div style={{ width: `${analytics?.tasks.percent || 0}%`, height: '100%', background: '#10b981', borderRadius: 3 }} />
          </div>
        </div>

        <div className="glass-card" style={{ padding: 20 }}>
          <div style={{ color: '#94a3b8', fontSize: 13, marginBottom: 8 }}>Skills vs Gaps</div>
          <div style={{ display: 'flex', alignItems: 'baseline', gap: 8 }}>
            <span style={{ fontSize: 32, fontWeight: 700, color: '#8b5cf6' }}>{analytics?.skills.acquired || 0}</span>
            <span style={{ color: '#f59e0b', fontSize: 12 }}>{analytics?.skills.gapsIdentified || 0} Gaps</span>
          </div>
          <p style={{ color: '#64748b', fontSize: 11, marginTop: 8 }}>Continuous tracking of canonical target skills compared with user profile data.</p>
        </div>
      </div>

      <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 24 }}>
        {/* Track Custom Event Form */}
        <div className="glass-card" style={{ padding: 24 }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16, color: '#e2e8f0' }}>Simulate Telemetry Event</h3>
          {message && (
            <div style={{ background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)', borderRadius: 8, padding: '8px 12px', color: '#34d399', fontSize: 13, marginBottom: 16 }}>
              {message}
            </div>
          )}
          {error && (
            <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 8, padding: '8px 12px', color: '#f87171', fontSize: 13, marginBottom: 16 }}>
              {error}
            </div>
          )}
          <form onSubmit={handleTrackEvent} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <div>
              <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>Event Name</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. click_resume_download"
                value={customEvent.name}
                onChange={e => setCustomEvent(prev => ({ ...prev, name: e.target.value }))}
                required
                id="telemetry-event-name"
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>Payload Data (JSON String, Optional)</label>
              <textarea
                className="input-field"
                rows={3}
                placeholder='e.g. { "fileFormat": "PDF", "source": "sidebar" }'
                value={customEvent.data}
                onChange={e => setCustomEvent(prev => ({ ...prev, data: e.target.value }))}
                style={{ resize: 'vertical', fontFamily: 'var(--font-mono)' }}
                id="telemetry-event-data"
              />
            </div>
            <button className="btn-primary" type="submit" disabled={submitting} style={{ alignSelf: 'flex-start' }} id="submit-telemetry-event">
              {submitting ? 'Tracking Event…' : 'Track Event'}
            </button>
          </form>
        </div>

        {/* Recent Telemetry Stream */}
        <div className="glass-card" style={{ padding: 24 }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16, color: '#e2e8f0' }}>Recent Telemetry Activity</h3>
          {(!analytics?.recentActivity || analytics.recentActivity.length === 0) ? (
            <p style={{ color: '#64748b', fontSize: 13 }}>No recent events recorded in this session.</p>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 12, maxHeight: 300, overflowY: 'auto', paddingRight: 6 }}>
              {analytics.recentActivity.map((act: any, i: number) => (
                <div key={i} style={{ borderBottom: '1px solid rgba(51,65,85,0.3)', paddingBottom: 10 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 4 }}>
                    <span style={{ fontSize: 13, fontWeight: 600, color: '#e2e8f0', fontFamily: 'var(--font-mono)' }}>{act.eventName}</span>
                    <span style={{ fontSize: 11, color: '#64748b' }}>{new Date(act.timestamp).toLocaleTimeString()}</span>
                  </div>
                  {act.data && Object.keys(act.data).length > 0 && (
                    <pre style={{ fontSize: 11, background: 'rgba(15,23,42,0.4)', padding: 6, borderRadius: 6, color: '#94a3b8', overflowX: 'auto' }}>
                      {JSON.stringify(act.data, null, 2)}
                    </pre>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
