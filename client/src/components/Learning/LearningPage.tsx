import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function LearningPage() {
  const [resources, setResources] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState<'all' | 'in_progress' | 'completed'>('all');

  useEffect(() => {
    async function load() {
      try {
        const data = await api('/learning/resources');
        setResources(data.resources || []);
      } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  const updateProgress = async (resourceId: string, status: string) => {
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
    } catch { /* empty */ }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading verified learning resources…</div>;

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
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Header */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Curated Technical Learning</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>
            Official documentation, guides, and engineering references verified against industry standards.
          </p>
        </div>

        {/* Filter Buttons */}
        <div style={{
          display: 'flex',
          background: 'rgba(15, 23, 42, 0.8)',
          border: '1px solid rgba(51, 65, 85, 0.6)',
          borderRadius: 8,
          padding: 3
        }}>
          <button
            onClick={() => setFilter('all')}
            style={{
              padding: '6px 12px',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 600,
              cursor: 'pointer',
              border: 'none',
              background: filter === 'all' ? '#2563eb' : 'transparent',
              color: filter === 'all' ? '#ffffff' : '#94a3b8'
            }}
          >
            All ({resources.length})
          </button>
          <button
            onClick={() => setFilter('in_progress')}
            style={{
              padding: '6px 12px',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 600,
              cursor: 'pointer',
              border: 'none',
              background: filter === 'in_progress' ? '#2563eb' : 'transparent',
              color: filter === 'in_progress' ? '#ffffff' : '#94a3b8'
            }}
          >
            In Progress ({inProgressCount})
          </button>
          <button
            onClick={() => setFilter('completed')}
            style={{
              padding: '6px 12px',
              borderRadius: 6,
              fontSize: 12,
              fontWeight: 600,
              cursor: 'pointer',
              border: 'none',
              background: filter === 'completed' ? '#2563eb' : 'transparent',
              color: filter === 'completed' ? '#ffffff' : '#94a3b8'
            }}
          >
            Completed ({completedCount})
          </button>
        </div>
      </div>

      {Object.keys(groups).length === 0 ? (
        <div className="glass-card" style={{ padding: 40, textAlign: 'center', color: '#94a3b8', fontSize: 14 }}>
          No resources found for the current filter.
        </div>
      ) : (
        Object.entries(groups).map(([category, items]) => (
          <div key={category}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
              <span style={{ fontSize: 15, fontWeight: 700, color: '#e2e8f0' }}>{category}</span>
              <span className="badge badge-neutral" style={{ fontSize: 10 }}>{items.length} resources</span>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {items.map(r => (
                <div
                  key={r.id}
                  className="glass-card"
                  style={{
                    padding: '16px 20px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: 12,
                    border: r.user_status === 'completed' ? '1px solid rgba(16, 185, 129, 0.35)' : '1px solid rgba(51, 65, 85, 0.5)'
                  }}
                >
                  <div style={{ flex: 1, minWidth: 260 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                      <a
                        href={r.url}
                        target="_blank"
                        rel="noopener noreferrer"
                        style={{ fontSize: 14, fontWeight: 600, color: '#f8fafc', textDecoration: 'none' }}
                      >
                        {r.title} ↗
                      </a>
                      {r.is_verified ? (
                        <span className="badge badge-success" style={{ fontSize: 10, padding: '2px 6px' }}>
                          Verified Source ✓
                        </span>
                      ) : (
                        <span className="badge badge-warning" style={{ fontSize: 10, padding: '2px 6px' }}>
                          Community / Unverified
                        </span>
                      )}
                    </div>

                    <div style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>
                      {r.provider} · ~{r.estimated_minutes} min · Difficulty: {r.difficulty}
                    </div>

                    <div style={{ display: 'flex', gap: 6, marginTop: 6, flexWrap: 'wrap' }}>
                      {r.skill_tags?.split(',').slice(0, 3).map((t: string) => (
                        <span key={t.trim()} className="badge badge-neutral" style={{ fontSize: 10 }}>
                          {t.trim()}
                        </span>
                      ))}
                    </div>
                  </div>

                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    {r.user_status !== 'started' && r.user_status !== 'completed' && (
                      <button
                        className="btn-secondary"
                        style={{ fontSize: 12, padding: '6px 12px' }}
                        onClick={() => updateProgress(r.id, 'started')}
                      >
                        Start Learning
                      </button>
                    )}
                    {r.user_status === 'started' && (
                      <button
                        className="btn-primary"
                        style={{ fontSize: 12, padding: '6px 12px' }}
                        onClick={() => updateProgress(r.id, 'completed')}
                      >
                        Mark Complete ✓
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
