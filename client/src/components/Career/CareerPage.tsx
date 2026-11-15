import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

export interface NormalizedGap {
  skillName: string;
  category: string;
  currentLevel: number;
  requiredLevel: number;
  priority: string;
  recommendation: string;
}

export function normalizeSkillGap(raw: any): NormalizedGap {
  return {
    skillName: raw?.skillName ?? raw?.skill_name ?? 'Unnamed Skill',
    category: raw?.category ?? 'General',
    currentLevel: Number(raw?.currentLevel ?? raw?.current_level ?? 0),
    requiredLevel: Number(raw?.requiredLevel ?? raw?.required_level ?? 0),
    priority: raw?.priority ?? 'medium',
    recommendation: raw?.recommendation ?? '',
  };
}

export default function CareerPage() {
  const { profile, refreshProfile } = useAuth();
  const [gaps, setGaps] = useState<NormalizedGap[]>([]);
  const [analysisResult, setAnalysisResult] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [analyzing, setAnalyzing] = useState(false);
  const [error, setError] = useState('');

  const loadGaps = async () => {
    setLoading(true);
    setError('');
    try {
      const data = await api('/career/skill-gaps');
      setGaps((data.gaps || []).map(normalizeSkillGap));
    } catch (err: any) {
      setError(err?.message || 'Failed to load skill gaps.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadGaps();
  }, []);

  const runAnalysis = async () => {
    setAnalyzing(true);
    setError('');
    try {
      const targetRole = profile?.target_role?.trim() || 'Full Stack Engineer';
      const result = await api('/career/analyze', {
        method: 'POST',
        body: JSON.stringify({ targetRole }),
      });
      setAnalysisResult(result);
      setGaps((result.skillGaps || []).map(normalizeSkillGap));
      await refreshProfile();
    } catch (err: any) {
      setError(err?.message || 'Career analysis failed.');
    } finally {
      setAnalyzing(false);
    }
  };

  const displayedScore = analysisResult?.readinessScore ?? profile?.current_readiness_score ?? 0;
  const hasScore = Number(displayedScore) > 0;
  const activeRole = profile?.target_role?.trim();

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 2px' }}>Career Gap Analysis</h2>
          <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
            {activeRole
              ? `Benchmarking your recorded skills against ${activeRole}.`
              : 'Target role not configured in Profile yet (defaults to Full Stack Engineer when run).'}
          </p>
        </div>
        <button type="button" className="btn-primary" onClick={runAnalysis} disabled={analyzing} id="run-analysis">
          {analyzing ? 'Analyzing…' : 'Run Gap Analysis'}
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

      {/* Readiness Score */}
      {hasScore ? (
        <div className="glass-card" style={{ padding: 22, display: 'flex', alignItems: 'center', flexWrap: 'wrap', gap: 20 }}>
          <div
            style={{
              width: 92,
              height: 92,
              borderRadius: '50%',
              background: `conic-gradient(#3b82f6 ${displayedScore * 3.6}deg, rgba(51,65,85,0.4) 0deg)`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <div
              style={{
                width: 74,
                height: 74,
                borderRadius: '50%',
                background: 'hsl(223, 47%, 14%)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 22,
                fontWeight: 700,
              }}
            >
              {displayedScore}%
            </div>
          </div>
          <div style={{ flex: '1 1 200px', minWidth: 0 }}>
            <div style={{ fontSize: 17, fontWeight: 700, marginBottom: 4 }}>Career Readiness Score</div>
            <p style={{ color: '#94a3b8', fontSize: 13, lineHeight: 1.5, margin: '0 0 8px' }}>
              {analysisResult?.roleRequirementsSummary || `Calibrated for ${activeRole || 'Full Stack Engineer'}.`}
            </p>
            {analysisResult?.modelUsed && (
              <span className="badge badge-neutral">Engine: {analysisResult.modelUsed}</span>
            )}
          </div>
        </div>
      ) : (
        <div className="glass-card" style={{ padding: 18, color: '#94a3b8', fontSize: 13 }}>
          Readiness score has not been calibrated yet. Run Gap Analysis to evaluate your current skills.
        </div>
      )}

      {/* Skill Gaps */}
      {loading ? (
        <p style={{ color: '#64748b' }}>Loading skill gaps…</p>
      ) : gaps.length === 0 ? (
        <div className="glass-card" style={{ padding: 24, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14, margin: 0 }}>
            No skill gap records yet. Click "Run Gap Analysis" to compare your recorded skills against role requirements.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <h3 style={{ fontSize: 15.5, fontWeight: 600, color: '#e2e8f0', margin: 0 }}>
            Identified Skill Gaps ({gaps.length})
          </h3>
          {gaps.map((gap, i) => (
            <div
              key={`${gap.skillName}-${i}`}
              className="glass-card"
              style={{
                padding: '14px 16px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 12,
              }}
            >
              <div style={{ flex: '1 1 200px', minWidth: 0 }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap', marginBottom: 4 }}>
                  <span style={{ fontWeight: 600, fontSize: 14 }}>{gap.skillName}</span>
                  <span className={`badge ${gap.priority === 'high' ? 'badge-danger' : gap.priority === 'medium' ? 'badge-warning' : 'badge-neutral'}`}>
                    {gap.priority}
                  </span>
                </div>
                <div style={{ color: '#64748b', fontSize: 12 }}>{gap.category}</div>
                {gap.recommendation && (
                  <div style={{ color: '#94a3b8', fontSize: 12.5, marginTop: 4, lineHeight: 1.45 }}>{gap.recommendation}</div>
                )}
              </div>
              <div style={{ textAlign: 'center', minWidth: 82 }}>
                <div style={{ fontSize: 13, color: '#94a3b8', fontWeight: 600 }}>
                  L{gap.currentLevel} → L{gap.requiredLevel}
                </div>
                <div
                  style={{
                    width: 68,
                    height: 6,
                    background: 'rgba(51,65,85,0.5)',
                    borderRadius: 3,
                    margin: '6px auto 0',
                    overflow: 'hidden',
                  }}
                >
                  <div
                    style={{
                      width: `${gap.requiredLevel > 0 ? Math.min(100, (gap.currentLevel / gap.requiredLevel) * 100) : 0}%`,
                      height: '100%',
                      background: gap.priority === 'high' ? '#ef4444' : gap.priority === 'medium' ? '#f59e0b' : '#10b981',
                      borderRadius: 3,
                    }}
                  />
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
