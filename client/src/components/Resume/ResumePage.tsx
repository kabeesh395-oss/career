import { useEffect, useState, useRef } from 'react';
import { api } from '../../api/client';

export default function ResumePage() {
  const [resumes, setResumes] = useState<any[]>([]);
  const [latestAnalysis, setLatestAnalysis] = useState<any>(null);
  const [uploading, setUploading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const fileRef = useRef<HTMLInputElement>(null);

  const loadResumes = async () => {
    try {
      const data = await api('/resume');
      setResumes(data.resumes || []);
      setLatestAnalysis(data.latestAnalysis || null);
    } catch { /* empty */ }
    setLoading(false);
  };

  useEffect(() => { loadResumes(); }, []);

  const handleUpload = async () => {
    const file = fileRef.current?.files?.[0];
    if (!file) return;
    setUploading(true);
    setError('');
    try {
      const formData = new FormData();
      formData.append('resume', file);
      const data = await api('/resume/upload', { method: 'POST', body: formData });
      setResumes(prev => [data.resume, ...prev]);
      setLatestAnalysis(data.analysis);
      if (fileRef.current) fileRef.current.value = '';
    } catch (err: any) {
      setError(err.message || 'Upload failed.');
    }
    setUploading(false);
  };

  const deleteResume = async (id: string) => {
    try {
      await api(`/resume/${id}`, { method: 'DELETE' });
      setResumes(prev => prev.filter(r => r.id !== id));
      if (latestAnalysis?.resume_id === id) setLatestAnalysis(null);
    } catch { /* empty */ }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading resumes…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Resume Pipeline</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Upload, extract, and score your resume against target role requirements.</p>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>{error}</div>
      )}

      {/* Upload Zone */}
      <div className="glass-card" style={{ padding: 28, textAlign: 'center' }}>
        <div style={{ fontSize: 36, marginBottom: 12 }}>📄</div>
        <p style={{ color: '#94a3b8', fontSize: 14, marginBottom: 16 }}>Drag and drop or click to upload your resume (PDF, TXT, DOCX · max 10MB)</p>
        <input ref={fileRef} type="file" accept=".pdf,.txt,.doc,.docx" style={{ display: 'none' }} id="resume-file-input" onChange={handleUpload} />
        <button className="btn-primary" onClick={() => fileRef.current?.click()} disabled={uploading} id="upload-resume">
          {uploading ? 'Uploading & Analyzing…' : 'Upload Resume'}
        </button>
      </div>

      {/* Latest Analysis */}
      {latestAnalysis && (
        <div className="glass-card" style={{ padding: 24 }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>ATS Analysis Result</h3>
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(140px, 1fr))', gap: 14, marginBottom: 20 }}>
            <ScoreBox label="Overall" score={latestAnalysis.overall_score} color="#3b82f6" />
            <ScoreBox label="Impact" score={latestAnalysis.impact_score} color="#10b981" />
            <ScoreBox label="Brevity" score={latestAnalysis.brevity_score} color="#8b5cf6" />
            <ScoreBox label="Style" score={latestAnalysis.style_score} color="#f59e0b" />
          </div>

          {latestAnalysis.skills_detected?.length > 0 && (
            <div style={{ marginBottom: 16 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>Detected Skills</div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                {latestAnalysis.skills_detected.map((s: string) => (
                  <span key={s} className="badge badge-primary">{s}</span>
                ))}
              </div>
            </div>
          )}

          {latestAnalysis.strengths?.length > 0 && (
            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#34d399', marginBottom: 6 }}>Strengths</div>
              {latestAnalysis.strengths.map((s: string, i: number) => (
                <p key={i} style={{ color: '#94a3b8', fontSize: 13, marginBottom: 4 }}>✓ {s}</p>
              ))}
            </div>
          )}

          {latestAnalysis.weaknesses?.length > 0 && (
            <div style={{ marginBottom: 12 }}>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#f87171', marginBottom: 6 }}>Areas to Improve</div>
              {latestAnalysis.weaknesses.map((w: string, i: number) => (
                <p key={i} style={{ color: '#94a3b8', fontSize: 13, marginBottom: 4 }}>✗ {w}</p>
              ))}
            </div>
          )}

          {latestAnalysis.recommendations?.length > 0 && (
            <div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#60a5fa', marginBottom: 6 }}>Recommendations</div>
              {latestAnalysis.recommendations.map((r: string, i: number) => (
                <p key={i} style={{ color: '#94a3b8', fontSize: 13, marginBottom: 4 }}>→ {r}</p>
              ))}
            </div>
          )}

          {latestAnalysis.model_used && (
            <span className="badge badge-neutral" style={{ marginTop: 12 }}>Engine: {latestAnalysis.model_used}</span>
          )}
        </div>
      )}

      {/* Resume History */}
      {resumes.length > 0 && (
        <div>
          <h3 style={{ fontSize: 15, fontWeight: 600, color: '#e2e8f0', marginBottom: 12 }}>Upload History</h3>
          {resumes.map(r => (
            <div key={r.id} className="glass-card" style={{
              padding: '14px 18px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8
            }}>
              <div>
                <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0' }}>{r.original_filename}</div>
                <div style={{ fontSize: 12, color: '#64748b' }}>
                  {(r.file_size / 1024).toFixed(1)} KB · {r.status} · {new Date(r.created_at).toLocaleDateString()}
                </div>
              </div>
              <button
                onClick={() => deleteResume(r.id)}
                style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 8, padding: '6px 12px', color: '#f87171', cursor: 'pointer', fontSize: 12 }}
              >
                Delete
              </button>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

function ScoreBox({ label, score, color }: { label: string; score: number; color: string }) {
  return (
    <div style={{
      background: 'rgba(15,23,42,0.6)', border: '1px solid rgba(51,65,85,0.4)',
      borderRadius: 10, padding: '14px 16px', textAlign: 'center'
    }}>
      <div style={{ fontSize: 28, fontWeight: 700, color, marginBottom: 4 }}>{score}</div>
      <div style={{ fontSize: 12, color: '#94a3b8', fontWeight: 500 }}>{label}</div>
    </div>
  );
}
