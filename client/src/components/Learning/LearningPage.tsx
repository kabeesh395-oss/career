import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function LearningPage() {
  const [resources, setResources] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [updatingId, setUpdatingId] = useState<string | null>(null);
  const [filter, setFilter] = useState<'all' | 'in_progress' | 'completed'>('all');

  const loadResources = async () => {
    setError('');
    try {
      const data = await api('/learning/resources');
      setResources(data.resources || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load learning resources.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResources();
  }, []);

  const updateProgress = async (resourceId: string, status: string) => {
    setUpdatingId(resourceId);
    setError('');
    try {
      const data = await api('/learning/progress', {
        method: 'POST',
        body: JSON.stringify({ resourceId, status }),
      });
      setResources(prev =>
        prev.map(r =>
          r.id === resourceId
            ? { ...r, user_status: data.resource.user_status, user_progress: data.resource.user_progress }
            : r
        )
      );
    } catch (err: any) {
      setError(err?.message || 'Failed to update learning progress.');
    } finally {
      setUpdatingId(null);
    }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 32 }}>Loading curated learning resources…</div>;

  const filteredResources = resources.filter(r => {
    if (filter === 'in_progress') return r.user_status === 'started';
    if (filter === 'completed') return r.user_status === 'completed';
    return true;
  });

  const groups: Record<string, any[]> = {};
  filteredResources.forEach(r => {
    if (!groups[r.category]) groups[r.category] = [];
    groups[r.category].push(r);
  });

  const completedCount = resources.filter(r => r.user_status === 'completed').length;
  const inProgressCount = resources.filter(r => r.user_status === 'started').length;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 2px' }}>Curated Technical Learning</h2>
          <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
            Official documentation and engineering references with real completion tracking.
          </p>
        </div>

        <div className="segmented-tabs">
          <button
            type="button"
            onClick={() => setFilter('all')}
            className={`segmented-tab-btn ${filter === 'all' ? 'active' : ''}`}
          >
            All ({resources.length})
          </button>
          <button
            type="button"
            onClick={() => setFilter('in_progress')}
            className={`segmented-tab-btn ${filter === 'in_progress' ? 'active' : ''}`}
          >
            In Progress ({inProgressCount})
          </button>
          <button
            type="button"
            onClick={() => setFilter('completed')}
            className={`segmented-tab-btn ${filter === 'completed' ? 'active' : ''}`}
          >
            Completed ({completedCount})
          </button>
        </div>
      </div>

      {error && (
        <div
          style={{
            background: 'rgba(239,68,68,0.1)',
            border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 10,
            padding: '10px 14px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 8,
            color: '#f87171',
            fontSize: 13,
          }}
        >
          <span>{error}</span>
          <button type="button" className="btn-secondary" style={{ fontSize: 12, padding: '4px 10px' }} onClick={loadResources}>
            Retry
          </button>
        </div>
      )}

      {Object.keys(groups).length === 0 ? (
        <div className="glass-card" style={{ padding: 32, textAlign: 'center', color: '#94a3b8', fontSize: 14 }}>
          No resources match the selected filter.
        </div>
      ) : (
        Object.entries(groups).map(([category, items]) => (
          <div key={category}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
              <span style={{ fontSize: 15, fontWeight: 700, color: '#e2e8f0' }}>{category}</span>
              <span className="badge badge-neutral" style={{ fontSize: 10 }}>{items.length} resources</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {items.map(r => (
                <div
                  key={r.id}
                  className="glass-card"
                  style={{
                    padding: '14px 16px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: 12,
                    border: r.user_status === 'completed' ? '1px solid rgba(16, 185, 129, 0.35)' : '1px solid rgba(51, 65, 85, 0.5)',
                  }}
                >
                  <div style={{ flex: '1 1 200px', minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                      <a
                        href={r.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ fontSize: 14, fontWeight: 600, color: '#f8fafc', textDecoration: 'none', wordBreak: 'break-word' }}
                      >
                        {r.title} ↗
                      </a>
                      {r.is_verified ? (
                        <span className="badge badge-success" style={{ fontSize: 10, padding: '2px 6px' }}>
                          Verified Source ✓
                        </span>
                      ) : (
                        <span className="badge badge-warning" style={{ fontSize: 10, padding: '2px 6px' }}>
                          Community
                        </span>
                      )}
                    </div>

                    <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>
                      {r.provider} · ~{r.estimated_minutes} min · {r.difficulty}
                    </div>

                    {r.skill_tags && (
                      <div style={{ display: 'flex', gap: 6, marginTop: 6, flexWrap: 'wrap' }}>
                        {r.skill_tags.split(',').slice(0, 3).map((t: string) => (
                          <span key={t.trim()} className="badge badge-neutral" style={{ fontSize: 10 }}>
                            {t.trim()}
                          </span>
                        ))}
                      </div>
                    )}
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    {r.user_status !== 'started' && r.user_status !== 'completed' && (
                      <button
                        type="button"
                        className="btn-secondary"
                        style={{ fontSize: 12, padding: '7px 12px', minHeight: 38 }}
                        disabled={updatingId === r.id}
                        onClick={() => updateProgress(r.id, 'started')}
                      >
                        {updatingId === r.id ? 'Saving…' : 'Start Learning'}
                      </button>
                    )}
                    {r.user_status === 'started' && (
                      <button
                        type="button"
                        className="btn-primary"
                        style={{ fontSize: 12, padding: '7px 12px', minHeight: 38 }}
                        disabled={updatingId === r.id}
                        onClick={() => updateProgress(r.id, 'completed')}
                      >
                        {updatingId === r.id ? 'Saving…' : 'Mark Complete ✓'}
                      </button>
                    )}
                    {r.user_status === 'completed' && (
                      <span className="badge badge-success" style={{ fontSize: 12, padding: '6px 12px' }}>
                        Completed ✓
                      </span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        ))
      )}
    </div>
  );
}
