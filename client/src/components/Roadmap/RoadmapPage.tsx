import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function RoadmapPage() {
  const [roadmap, setRoadmap] = useState<any>(null);
  const [items, setItems] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [generating, setGenerating] = useState(false);
  const [togglingId, setTogglingId] = useState<string | null>(null);
  const [error, setError] = useState('');
  const [expandedItem, setExpandedItem] = useState<string | null>(null);

  const loadRoadmap = async () => {
    setError('');
    try {
      const data = await api('/roadmap');
      setRoadmap(data.roadmap);
      setItems(data.items || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load roadmap.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRoadmap();
  }, []);

  const generateRoadmap = async () => {
    setGenerating(true);
    setError('');
    try {
      const data = await api('/roadmap/generate', { method: 'POST', body: JSON.stringify({}) });
      setRoadmap(data.roadmap);
      setItems(data.items || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to generate roadmap.');
    } finally {
      setGenerating(false);
    }
  };

  const toggleTask = async (itemId: string) => {
    setTogglingId(itemId);
    setError('');
    try {
      const data = await api(`/roadmap/items/${itemId}`, { method: 'PATCH', body: JSON.stringify({}) });
      setItems(prev => prev.map(i => (i.id === itemId ? data.item : i)));
      setRoadmap(data.roadmap);
    } catch (err: any) {
      setError(err?.message || 'Failed to update roadmap milestone.');
    } finally {
      setTogglingId(null);
    }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 32 }}>Loading personalized roadmap…</div>;

  const phases: Record<string, any[]> = {};
  items.forEach(item => {
    const key = `Phase ${item.phase_number}: ${item.phase_title}`;
    if (!phases[key]) phases[key] = [];
    phases[key].push(item);
  });

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 2px' }}>Career Trajectory Roadmap</h2>
          <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
            {roadmap
              ? `${roadmap.title} — ${roadmap.completed_tasks || 0} of ${roadmap.total_tasks || 0} milestones completed`
              : 'Milestone curriculum derived from your profile and skill gaps.'}
          </p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={generateRoadmap}
          disabled={generating}
          id="generate-roadmap"
        >
          {generating ? 'Generating…' : roadmap ? 'Regenerate from Gaps' : 'Generate Roadmap'}
        </button>
      </div>

      {error && (
        <div
          style={{
            background: 'rgba(239,68,68,0.1)',
            border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 10,
            padding: '10px 14px',
            color: '#f87171',
            fontSize: 13,
          }}
        >
          {error}
        </div>
      )}

      {roadmap && (
        <div className="glass-card" style={{ padding: '16px 20px' }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 8, marginBottom: 10 }}>
            <div>
              <span style={{ fontSize: 14, fontWeight: 700, color: '#f8fafc' }}>Roadmap Completion</span>
              <span style={{ fontSize: 12, color: '#94a3b8', marginLeft: 8 }}>
                Role: <strong>{roadmap.target_role}</strong>
              </span>
            </div>
            <span style={{ fontSize: 15, fontWeight: 700, color: '#38bdf8' }}>
              {roadmap.progress_percent?.toFixed(0) || 0}%
            </span>
          </div>

          <div style={{ width: '100%', height: 8, background: 'rgba(51,65,85,0.5)', borderRadius: 4, overflow: 'hidden' }}>
            <div
              style={{
                width: `${roadmap.progress_percent || 0}%`,
                height: '100%',
                background: 'linear-gradient(90deg, #3b82f6, #38bdf8)',
                borderRadius: 4,
                transition: 'width 0.4s ease',
              }}
            />
          </div>
        </div>
      )}

      {!roadmap ? (
        <div className="glass-card" style={{ padding: 32, textAlign: 'center' }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc', marginBottom: 6 }}>
            No Career Roadmap Generated Yet
          </h3>
          <p style={{ color: '#94a3b8', fontSize: 13.5, maxWidth: 480, margin: '0 auto 18px', lineHeight: 1.5 }}>
            Generate a 3-phase curriculum tailored to your target role and skill gaps.
          </p>
          <button type="button" className="btn-primary" onClick={generateRoadmap} disabled={generating}>
            {generating ? 'Generating…' : 'Generate Roadmap'}
          </button>
        </div>
      ) : (
        Object.entries(phases).map(([phaseTitle, phaseItems]) => (
          <div key={phaseTitle} style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
              <div style={{ width: 8, height: 8, borderRadius: '50%', background: '#38bdf8', flexShrink: 0 }} />
              <h3 style={{ fontSize: 15, fontWeight: 700, color: '#e2e8f0', margin: 0 }}>{phaseTitle}</h3>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
              {phaseItems.map(item => {
                const isCompleted = item.status === 'completed';
                const isExpanded = expandedItem === item.id;

                return (
                  <div
                    key={item.id}
                    className="glass-card"
                    style={{
                      padding: '14px 16px',
                      display: 'flex',
                      flexDirection: 'column',
                      gap: 10,
                      border: isCompleted ? '1px solid rgba(16, 185, 129, 0.35)' : '1px solid rgba(51, 65, 85, 0.6)',
                      background: isCompleted ? 'rgba(15, 23, 42, 0.45)' : 'rgba(19, 29, 53, 0.7)',
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'flex-start', gap: 12 }}>
                      <button
                        type="button"
                        onClick={() => toggleTask(item.id)}
                        disabled={togglingId === item.id}
                        aria-label={isCompleted ? `Mark ${item.title} pending` : `Mark ${item.title} completed`}
                        style={{
                          width: 28,
                          height: 28,
                          borderRadius: 6,
                          flexShrink: 0,
                          marginTop: 2,
                          border: isCompleted ? '2px solid #10b981' : '2px solid #64748b',
                          background: isCompleted ? '#10b981' : 'transparent',
                          color: '#ffffff',
                          cursor: 'pointer',
                          fontSize: 14,
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                        }}
                      >
                        {isCompleted ? '✓' : ''}
                      </button>

                      <div style={{ flex: 1, minWidth: 0 }}>
                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', flexWrap: 'wrap', gap: 8 }}>
                          <div
                            style={{
                              fontSize: 14.5,
                              fontWeight: 600,
                              color: isCompleted ? '#94a3b8' : '#f8fafc',
                              textDecoration: isCompleted ? 'line-through' : 'none',
                            }}
                          >
                            {item.title}
                          </div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 6, flexWrap: 'wrap' }}>
                            {item.skill && (
                              <span className="badge badge-primary" style={{ fontSize: 11 }}>
                                {item.skill}
                              </span>
                            )}
                            <span className="badge badge-neutral" style={{ fontSize: 11 }}>
                              ~{item.estimated_hours}h
                            </span>
                            <button
                              type="button"
                              onClick={() => setExpandedItem(isExpanded ? null : item.id)}
                              style={{
                                background: 'transparent',
                                border: 'none',
                                fontSize: 12,
                                color: '#38bdf8',
                                cursor: 'pointer',
                                padding: '4px 6px',
                                textDecoration: 'underline',
                              }}
                            >
                              {isExpanded ? 'Hide Details ▲' : 'Details ▼'}
                            </button>
                          </div>
                        </div>

                        <p style={{ fontSize: 13, color: '#94a3b8', margin: '4px 0 0', lineHeight: 1.45 }}>
                          {item.description}
                        </p>
                      </div>
                    </div>

                    {isExpanded && (
                      <div
                        style={{
                          marginTop: 4,
                          paddingTop: 10,
                          borderTop: '1px solid rgba(51, 65, 85, 0.4)',
                          display: 'grid',
                          gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 220px), 1fr))',
                          gap: 10,
                        }}
                      >
                        {item.why_it_matters && (
                          <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '10px 12px', borderRadius: 8, border: '1px solid rgba(51, 65, 85, 0.3)' }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: '#60a5fa', textTransform: 'uppercase', marginBottom: 4 }}>
                              Why It Matters
                            </div>
                            <div style={{ fontSize: 12, color: '#cbd5e1', lineHeight: 1.4 }}>{item.why_it_matters}</div>
                          </div>
                        )}
                        {item.learning_objective && (
                          <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '10px 12px', borderRadius: 8, border: '1px solid rgba(51, 65, 85, 0.3)' }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: '#34d399', textTransform: 'uppercase', marginBottom: 4 }}>
                              Learning Objective
                            </div>
                            <div style={{ fontSize: 12, color: '#cbd5e1', lineHeight: 1.4 }}>{item.learning_objective}</div>
                          </div>
                        )}
                        {item.practice_suggestion && (
                          <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '10px 12px', borderRadius: 8, border: '1px solid rgba(51, 65, 85, 0.3)' }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: '#fbbf24', textTransform: 'uppercase', marginBottom: 4 }}>
                              Practice Suggestion
                            </div>
                            <div style={{ fontSize: 12, color: '#cbd5e1', lineHeight: 1.4 }}>{item.practice_suggestion}</div>
                          </div>
                        )}
                        {item.interview_relevance && (
                          <div style={{ background: 'rgba(15, 23, 42, 0.5)', padding: '10px 12px', borderRadius: 8, border: '1px solid rgba(51, 65, 85, 0.3)' }}>
                            <div style={{ fontSize: 11, fontWeight: 700, color: '#a855f7', textTransform: 'uppercase', marginBottom: 4 }}>
                              Interview Relevance
                            </div>
                            <div style={{ fontSize: 12, color: '#cbd5e1', lineHeight: 1.4 }}>{item.interview_relevance}</div>
                          </div>
                        )}
                      </div>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        ))
      )}
    </div>
  );
}
