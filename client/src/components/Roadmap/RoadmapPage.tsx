import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function RoadmapPage() {
  const [roadmap, setRoadmap] = useState<any>(null);
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [error, setError] = useState('');

  const loadRoadmap = async () => {
    try {
      const data = await api('/roadmap');
      setRoadmap(data.roadmap);
      setItems(data.items || []);
    } catch { /* empty */ }
    setLoading(false);
  };

  useEffect(() => { loadRoadmap(); }, []);

  const generateRoadmap = async () => {
    setGenerating(true);
    setError('');
    try {
      const data = await api('/roadmap/generate', { method: 'POST', body: JSON.stringify({}) });
      setRoadmap(data.roadmap);
      setItems(data.items || []);
    } catch (err: any) {
      setError(err.message || 'Failed to generate roadmap.');
    }
    setGenerating(false);
  };

  const toggleTask = async (itemId: string) => {
    try {
      const data = await api(`/roadmap/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({}) });
      setItems(prev => prev.map(i => i.id === itemId ? data.item : i));
      setRoadmap(data.roadmap);
    } catch { /* empty */ }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading roadmap…</div>;

  // Group items by phase
  const phases: Record<string, any[]> = {};
  items.forEach(item => {
    const key = `Phase ${item.phase_number}: ${item.phase_title}`;
    if (!phases[key]) phases[key] = [];
    phases[key].push(item);
  });

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Career Roadmap</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>
            {roadmap ? `${roadmap.title} — ${roadmap.progress_percent?.toFixed(0)}% complete` : 'Generate your personalized career trajectory.'}
          </p>
        </div>
        <button className="btn-primary" onClick={generateRoadmap} disabled={generating} id="generate-roadmap">
          {generating ? 'Generating…' : roadmap ? 'Regenerate Roadmap' : 'Generate Roadmap'}
        </button>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>{error}</div>
      )}

      {/* Progress Bar */}
      {roadmap && (
        <div className="glass-card" style={{ padding: '16px 22px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 8 }}>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#e2e8f0' }}>Overall Progress</span>
            <span style={{ fontSize: 13, fontWeight: 600, color: '#60a5fa' }}>{roadmap.completed_tasks}/{roadmap.total_tasks} tasks</span>
          </div>
          <div style={{ width: '100%', height: 8, background: 'rgba(51,65,85,0.5)', borderRadius: 4, overflow: 'hidden' }}>
            <div style={{
              width: `${roadmap.progress_percent || 0}%`, height: '100%',
              background: 'linear-gradient(90deg, #3b82f6, #8b5cf6)',
              borderRadius: 4, transition: 'width 0.4s ease'
            }} />
          </div>
        </div>
      )}

      {/* Phases & Tasks */}
      {!roadmap ? (
        <div className="glass-card" style={{ padding: 40, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>No roadmap generated yet. Click "Generate Roadmap" to create a personalized plan based on your skill gaps.</p>
        </div>
      ) : (
        Object.entries(phases).map(([phaseTitle, phaseItems]) => (
          <div key={phaseTitle} style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
            <h3 style={{ fontSize: 15, fontWeight: 600, color: '#e2e8f0', marginBottom: 4 }}>{phaseTitle}</h3>
            {phaseItems.map(item => (
              <div key={item.id} className="glass-card" style={{
                padding: '14px 18px', display: 'flex', alignItems: 'flex-start', gap: 14,
                opacity: item.status === 'completed' ? 0.7 : 1
              }}>
                <button
                  onClick={() => toggleTask(item.id)}
                  style={{
                    width: 22, height: 22, borderRadius: 6, flexShrink: 0, marginTop: 2,
                    border: item.status === 'completed' ? '2px solid #10b981' : '2px solid #475569',
                    background: item.status === 'completed' ? 'rgba(16,185,129,0.15)' : 'transparent',
                    color: item.status === 'completed' ? '#10b981' : '#475569',
                    cursor: 'pointer', fontSize: 13, display: 'flex', alignItems: 'center', justifyContent: 'center',
                  }}
                >
                  {item.status === 'completed' ? '✓' : ''}
                </button>
                <div style={{ flex: 1 }}>
                  <div style={{
                    fontSize: 14, fontWeight: 600, color: '#e2e8f0',
                    textDecoration: item.status === 'completed' ? 'line-through' : 'none'
                  }}>
                    {item.title}
                  </div>
                  <div style={{ fontSize: 12, color: '#64748b', marginTop: 4 }}>{item.description}</div>
                  <div style={{ display: 'flex', gap: 8, marginTop: 8 }}>
                    <span className="badge badge-neutral">{item.category}</span>
                    <span className="badge badge-neutral">~{item.estimated_hours}h</span>
                  </div>
                </div>
              </div>
            ))}
          </div>
        ))
      )}
    </div>
  );
}
