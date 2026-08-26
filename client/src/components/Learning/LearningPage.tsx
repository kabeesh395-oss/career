import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function LearningPage() {
  const [resources, setResources] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    async function load() {
      try { const data = await api('/learning/resources'); setResources(data.resources || []); } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  const updateProgress = async (resourceId: string, status: string) => {
    try {
      const data = await api('/learning/progress', {
        method: 'POST', body: JSON.stringify({ resourceId, status }),
      });
      setResources(prev => prev.map(r => r.id === resourceId ? { ...r, user_status: data.resource.user_status, user_progress: data.resource.user_progress } : r));
    } catch { /* empty */ }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading resources…</div>;

  const groups: Record<string, any[]> = {};
  resources.forEach(r => {
    if (!groups[r.category]) groups[r.category] = [];
    groups[r.category].push(r);
  });

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Learning Resources</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Verified curated resources to close your skill gaps.</p>
      </div>

      {Object.entries(groups).map(([category, items]) => (
        <div key={category}>
          <h3 style={{ fontSize: 15, fontWeight: 600, color: '#e2e8f0', marginBottom: 12 }}>{category}</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            {items.map(r => (
              <div key={r.id} className="glass-card" style={{ padding: '16px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div style={{ flex: 1 }}>
                  <a href={r.url} target="_blank" rel="noopener noreferrer" style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0', textDecoration: 'none' }}>
                    {r.title} ↗
                  </a>
                  <div style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>
                    {r.provider} · ~{r.estimated_minutes} min · {r.difficulty}
                  </div>
                  <div style={{ display: 'flex', gap: 4, marginTop: 6 }}>
                    {r.skill_tags?.split(',').slice(0, 3).map((t: string) => (
                      <span key={t.trim()} className="badge badge-neutral" style={{ fontSize: 10 }}>{t.trim()}</span>
                    ))}
                  </div>
                </div>
                <div style={{ display: 'flex', gap: 6 }}>
                  {r.user_status !== 'started' && r.user_status !== 'completed' && (
                    <button className="btn-secondary" style={{ fontSize: 11, padding: '5px 10px' }} onClick={() => updateProgress(r.id, 'started')}>Start</button>
                  )}
                  {r.user_status === 'started' && (
                    <button className="btn-primary" style={{ fontSize: 11, padding: '5px 10px' }} onClick={() => updateProgress(r.id, 'completed')}>Complete</button>
                  )}
                  {r.user_status === 'completed' && (
                    <span className="badge badge-success">Completed ✓</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      ))}
    </div>
  );
}
