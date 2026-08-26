import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

export default function CareerPage() {
  const { profile, refreshProfile } = useAuth();
  const [gaps, setGaps] = useState<any[]>([]);
  const [analysisResult, setAnalysisResult] = useState<any>(null);
  const [loading, setLoading] = useState(false);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      setLoading(true);
      try {
        const data = await api('/career/skill-gaps');
        setGaps(data.gaps || []);
      } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  const runAnalysis = async () => {
    setAnalyzing(true);
    setError('');
    try {
      const result = await api('/career/analyze', {
        method: 'POST',
        body: JSON.stringify({ targetRole: profile?.target_role || 'Full Stack Engineer' }),
      });
      setAnalysisResult(result);
      setGaps(result.skillGaps || []);
      await refreshProfile();
    } catch (err: any) {
      setError(err.message || 'Analysis failed.');
    }
    setAnalyzing(false);
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Career Analysis</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>
            Calibrate readiness and identify precise skill gaps for {profile?.target_role || 'your target role'}.
          </p>
        </div>
        <button className="btn-primary" onClick={runAnalysis} disabled={analyzing} id="run-analysis">
          {analyzing ? 'Analyzing…' : 'Run Career Analysis'}
        </button>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>
          {error}
        </div>
      )}

      {/* Readiness Score */}
      {(analysisResult || profile?.current_readiness_score) && (
        <div className="glass-card" style={{ padding: '28px', display: 'flex', alignItems: 'center', gap: 28 }}>
          <div style={{
            width: 100, height: 100, borderRadius: '50%',
            background: `conic-gradient(#3b82f6 ${(analysisResult?.readinessScore || profile?.current_readiness_score || 0) * 3.6}deg, rgba(51,65,85,0.4) 0deg)`,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            boxShadow: '0 0 30px rgba(59,130,246,0.2)'
          }}>
            <div style={{
              width: 80, height: 80, borderRadius: '50%',
              background: 'hsl(223, 47%, 14%)',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 26, fontWeight: 700
            }}>
              {analysisResult?.readinessScore || profile?.current_readiness_score || 0}%
            </div>
          </div>
          <div>
            <div style={{ fontSize: 18, fontWeight: 700, marginBottom: 4 }}>Career Readiness Score</div>
            <p style={{ color: '#94a3b8', fontSize: 13, lineHeight: 1.6 }}>
              {analysisResult?.roleRequirementsSummary || `Calibrated for ${profile?.target_role || 'Full Stack Engineer'}.`}
            </p>
            {analysisResult?.modelUsed && (
              <span className="badge badge-neutral" style={{ marginTop: 8 }}>
                Engine: {analysisResult.modelUsed}
              </span>
            )}
          </div>
        </div>
      )}

      {/* Skill Gaps */}
      {loading ? (
        <p style={{ color: '#64748b' }}>Loading skill gaps…</p>
      ) : gaps.length === 0 ? (
        <div className="glass-card" style={{ padding: 28, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>No skill gap data yet. Click "Run Career Analysis" to begin.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
          <h2 style={{ fontSize: 17, fontWeight: 600, color: '#e2e8f0' }}>Identified Skill Gaps ({gaps.length})</h2>
          {gaps.map((gap: any, i: number) => (
            <div key={i} className="glass-card" style={{ padding: '16px 20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ flex: 1 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 4 }}>
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{gap.skill_name}</span>
                  <span className={`badge ${gap.priority === 'high' ? 'badge-danger' : gap.priority === 'medium' ? 'badge-warning' : 'badge-neutral'}`}>
                    {gap.priority}
                  </span>
                </div>
                <div style={{ color: '#64748b', fontSize: 12 }}>{gap.category}</div>
                <div style={{ color: '#94a3b8', fontSize: 12, marginTop: 4 }}>{gap.recommendation}</div>
              </div>
              <div style={{ textAlign: 'center', minWidth: 80 }}>
                <div style={{ fontSize: 13, color: '#94a3b8' }}>
                  L{gap.current_level} → L{gap.required_level}
                </div>
                <div style={{
                  width: 60, height: 6, background: 'rgba(51,65,85,0.5)', borderRadius: 3, marginTop: 6, overflow: 'hidden'
                }}>
                  <div style={{
                    width: `${gap.required_level > 0 ? (gap.current_level / gap.required_level) * 100 : 0}%`,
                    height: '100%',
                    background: gap.priority === 'high' ? '#ef4444' : gap.priority === 'medium' ? '#f59e0b' : '#10b981',
                    borderRadius: 3, transition: 'width 0.3s ease'
                  }} />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
