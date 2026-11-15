import { useEffect, useState, useRef } from 'react';
import { api } from '../../api/client';

type ProcessingStep = 'idle' | 'uploading' | 'success' | 'error';

interface ResumePageProps {
  initialTab?: 'resume' | 'job_description';
}

export default function ResumePage({ initialTab = 'resume' }: ResumePageProps) {
  const [activeTab, setActiveTab] = useState<'resume' | 'job_description'>(initialTab);
  const [resumes, setResumes] = useState<any[]>([]);
  const [latestAnalysis, setLatestAnalysis] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState('');
  const [error, setError] = useState('');

  // Upload state
  const [processingStep, setProcessingStep] = useState<ProcessingStep>('idle');
  const [lastUploadedFile, setLastUploadedFile] = useState<File | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  // Job Description Analyzer State
  const [jdText, setJdText] = useState('');
  const [selectedResumeId, setSelectedResumeId] = useState<string>('');
  const [jdAnalyzing, setJdAnalyzing] = useState(false);
  const [jdAnalysisResult, setJdAnalysisResult] = useState<any>(null);
  const [jdError, setJdError] = useState('');

  useEffect(() => {
    setActiveTab(initialTab);
  }, [initialTab]);

  const loadResumes = async () => {
    setLoading(true);
    setLoadError('');
    try {
      const data = await api('/resume');
      const list = data.resumes || [];
      setResumes(list);
      setLatestAnalysis(data.latestAnalysis || null);
      if (list[0]?.id) {
        setSelectedResumeId(prev => (prev && list.some((r: any) => r.id === prev) ? prev : list[0].id));
      } else {
        setSelectedResumeId('');
      }
    } catch (err: any) {
      setLoadError(err?.message || 'Failed to load uploaded resumes.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadResumes();
  }, []);

  const handleFileSelected = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      setLastUploadedFile(file);
      executeUploadAndAnalysis(file);
    }
  };

  const executeUploadAndAnalysis = async (file: File) => {
    setError('');
    setProcessingStep('uploading');

    try {
      const formData = new FormData();
      formData.append('resume', file);

      const data = await api('/resume/upload', {
        method: 'POST',
        body: formData,
      });

      setResumes(prev => [data.resume, ...prev.filter(r => r.id !== data.resume.id)]);
      setLatestAnalysis(data.analysis || null);
      setSelectedResumeId(data.resume.id);
      setProcessingStep('success');

      if (fileRef.current) fileRef.current.value = '';
    } catch (err: any) {
      setError(err?.message || 'Resume upload or text extraction failed. Please verify the file format.');
      setProcessingStep('error');
    }
  };

  const handleRetry = () => {
    if (lastUploadedFile) {
      executeUploadAndAnalysis(lastUploadedFile);
    } else {
      fileRef.current?.click();
    }
  };

  const deleteResume = async (id: string) => {
    setError('');
    try {
      await api(`/resume/${id}`, { method: 'DELETE' });
      let nextSelected = selectedResumeId;
      setResumes(prev => {
        const remaining = prev.filter(r => r.id !== id);
        if (selectedResumeId === id) {
          nextSelected = remaining[0]?.id || '';
        }
        return remaining;
      });
      setSelectedResumeId(nextSelected);
      if (latestAnalysis?.resume_id === id) {
        await loadResumes();
      }
    } catch (err: any) {
      setError(err?.message || 'Failed to delete resume.');
    }
  };

  const handleAnalyzeJobDescription = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!jdText.trim()) return;

    setJdAnalyzing(true);
    setJdError('');
    try {
      const data = await api('/resume/analyze-jd', {
        method: 'POST',
        body: JSON.stringify({
          jobDescription: jdText,
          resumeId: selectedResumeId || undefined,
        }),
      });

      setJdAnalysisResult(data.analysis);
    } catch (err: any) {
      setJdError(err?.message || 'Failed to analyze job description.');
    } finally {
      setJdAnalyzing(false);
    }
  };

  if (loading) {
    return <div style={{ color: '#94a3b8', padding: 32 }}>Loading resumes and ATS records…</div>;
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Title & Segmented Tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 14 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: '0 0 4px' }}>Resume & Job Match</h1>
          <p style={{ color: '#94a3b8', fontSize: 13.5, margin: 0 }}>
            Upload a real resume for server-side text extraction, ATS scoring, and job description comparison.
          </p>
        </div>

        <div className="segmented-tabs" role="tablist" aria-label="Resume Views">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'resume'}
            onClick={() => setActiveTab('resume')}
            className={`segmented-tab-btn ${activeTab === 'resume' ? 'active' : ''}`}
          >
            ATS Analyzer
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'job_description'}
            onClick={() => setActiveTab('job_description')}
            className={`segmented-tab-btn ${activeTab === 'job_description' ? 'active' : ''}`}
          >
            JD Matcher
          </button>
        </div>
      </div>

      {loadError && (
        <div
          style={{
            background: 'rgba(239,68,68,0.1)',
            border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 10,
            padding: '12px 16px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 10,
            color: '#f87171',
            fontSize: 13,
          }}
        >
          <span>{loadError}</span>
          <button type="button" onClick={loadResumes} className="btn-secondary" style={{ fontSize: 12, padding: '5px 12px' }}>
            Retry
          </button>
        </div>
      )}

      {/* TAB 1: RESUME ATS PIPELINE */}
      {activeTab === 'resume' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {error && (
            <div
              style={{
                background: 'rgba(239,68,68,0.1)',
                border: '1px solid rgba(239,68,68,0.3)',
                borderRadius: 10,
                padding: '12px 16px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 10,
                color: '#f87171',
                fontSize: 13,
              }}
            >
              <span>{error}</span>
              {lastUploadedFile && (
                <button type="button" onClick={handleRetry} className="btn-secondary" style={{ fontSize: 12, padding: '5px 12px' }}>
                  Retry Upload
                </button>
              )}
            </div>
          )}

          {/* Upload Zone */}
          <div className="glass-card" style={{ padding: 24, textAlign: 'center' }}>
            <input
              ref={fileRef}
              type="file"
              accept=".pdf,.txt,.doc,.docx"
              style={{ display: 'none' }}
              id="resume-file-input"
              onChange={handleFileSelected}
            />

            {processingStep === 'uploading' ? (
              <div style={{ maxWidth: 440, margin: '0 auto', padding: '12px 0' }}>
                <div
                  style={{
                    width: 34,
                    height: 34,
                    borderRadius: '50%',
                    border: '3px solid rgba(59, 130, 246, 0.25)',
                    borderTopColor: '#38bdf8',
                    animation: 'spin 0.8s linear infinite',
                    margin: '0 auto 14px',
                  }}
                />
                <div style={{ fontSize: 15, fontWeight: 600, color: '#f8fafc', marginBottom: 6 }}>
                  Uploading & Parsing Document…
                </div>
                <div style={{ fontSize: 12.5, color: '#94a3b8', lineHeight: 1.5 }}>
                  Extracting text and running ATS evaluation on the server for{' '}
                  <strong>{lastUploadedFile?.name || 'your file'}</strong>.
                </div>
              </div>
            ) : processingStep === 'error' ? (
              <div>
                <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f87171', marginBottom: 6 }}>
                  Could Not Process Resume
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13, marginBottom: 16 }}>
                  Text extraction or ATS evaluation failed. Make sure the document contains selectable text (PDF, TXT, DOC, or DOCX up to 10MB).
                </p>
                <div style={{ display: 'flex', justifyContent: 'center', gap: 10, flexWrap: 'wrap' }}>
                  {lastUploadedFile && (
                    <button type="button" className="btn-primary" onClick={handleRetry}>
                      Retry Upload
                    </button>
                  )}
                  <button type="button" className="btn-secondary" onClick={() => fileRef.current?.click()}>
                    Choose Another File
                  </button>
                </div>
              </div>
            ) : (
              <div>
                <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc', marginBottom: 6 }}>
                  Upload Candidate Resume
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13, margin: '0 auto 18px', maxWidth: 500, lineHeight: 1.5 }}>
                  Supports PDF, TXT, DOC, and DOCX up to 10MB. Parsed on the server to extract skills and evaluate role fit.
                </p>
                <div style={{ display: 'flex', justifyContent: 'center', gap: 10, flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    className="btn-primary"
                    onClick={() => fileRef.current?.click()}
                    id="upload-resume-btn"
                  >
                    Select Resume File
                  </button>
                  {latestAnalysis && (
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => setActiveTab('job_description')}
                    >
                      Compare vs Job Listing →
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>

          {/* Latest Analysis Results */}
          {latestAnalysis ? (
            <div className="glass-card" style={{ padding: 22 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
                <div>
                  <h3 style={{ fontSize: 17, fontWeight: 700, color: '#f8fafc', margin: 0 }}>
                    ATS Evaluation Results
                  </h3>
                  <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                    Calibrated against: <strong>{latestAnalysis.target_role || 'Profile Target Role'}</strong>
                  </div>
                </div>
                {latestAnalysis.model_used && (
                  <span className="badge badge-neutral">Engine: {latestAnalysis.model_used}</span>
                )}
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 130px), 1fr))', gap: 10, marginBottom: 18 }}>
                <ScoreCard label="Overall Match" score={latestAnalysis.overall_score} color="#3b82f6" />
                <ScoreCard label="Quantifiable Impact" score={latestAnalysis.impact_score} color="#10b981" />
                <ScoreCard label="Brevity & Density" score={latestAnalysis.brevity_score} color="#8b5cf6" />
                <ScoreCard label="Style & Structure" score={latestAnalysis.style_score} color="#f59e0b" />
              </div>

              {latestAnalysis.skills_detected?.length > 0 && (
                <div style={{ marginBottom: 16 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>
                    Detected Technical Skills ({latestAnalysis.skills_detected.length})
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                    {latestAnalysis.skills_detected.map((skill: string) => (
                      <span key={skill} className="badge badge-primary">{skill}</span>
                    ))}
                  </div>
                </div>
              )}

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 260px), 1fr))', gap: 14, marginBottom: 16 }}>
                {latestAnalysis.strengths?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(16, 185, 129, 0.25)', borderRadius: 10, padding: 14 }}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#34d399', marginBottom: 8 }}>
                      Key Strengths
                    </div>
                    {latestAnalysis.strengths.map((str: string, i: number) => (
                      <div key={i} style={{ color: '#cbd5e1', fontSize: 13, marginBottom: 6, display: 'flex', gap: 6 }}>
                        <span style={{ color: '#10b981' }}>✓</span>
                        <span>{str}</span>
                      </div>
                    ))}
                  </div>
                )}

                {latestAnalysis.weaknesses?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(239, 68, 68, 0.25)', borderRadius: 10, padding: 14 }}>
                    <div style={{ fontSize: 13, fontWeight: 700, color: '#f87171', marginBottom: 8 }}>
                      Identified Gaps
                    </div>
                    {latestAnalysis.weaknesses.map((weak: string, i: number) => (
                      <div key={i} style={{ color: '#cbd5e1', fontSize: 13, marginBottom: 6, display: 'flex', gap: 6 }}>
                        <span style={{ color: '#ef4444' }}>✗</span>
                        <span>{weak}</span>
                      </div>
                    ))}
                  </div>
                )}
              </div>

              {latestAnalysis.recommendations?.length > 0 && (
                <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(59, 130, 246, 0.25)', borderRadius: 10, padding: 14 }}>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#60a5fa', marginBottom: 8 }}>
                    Actionable Recommendations
                  </div>
                  {latestAnalysis.recommendations.map((rec: string, i: number) => (
                    <div key={i} style={{ color: '#cbd5e1', fontSize: 13, marginBottom: 6, display: 'flex', gap: 6 }}>
                      <span style={{ color: '#38bdf8' }}>→</span>
                      <span>{rec}</span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          ) : (
            <div className="glass-card" style={{ padding: 24, textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>
              No resume audited yet. Upload a PDF, DOCX, or TXT file above to run real ATS evaluation.
            </div>
          )}

          {/* Upload History */}
          {resumes.length > 0 && (
            <div>
              <h3 style={{ fontSize: 15, fontWeight: 600, color: '#e2e8f0', marginBottom: 10 }}>
                Uploaded Resumes ({resumes.length})
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {resumes.map(r => (
                  <div
                    key={r.id}
                    className="glass-card"
                    style={{
                      padding: '14px 16px',
                      display: 'flex',
                      justifyContent: 'space-between',
                      alignItems: 'center',
                      flexWrap: 'wrap',
                      gap: 10,
                    }}
                  >
                    <div style={{ minWidth: 0 }}>
                      <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0', wordBreak: 'break-word' }}>{r.original_filename}</div>
                      <div style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>
                        {(r.file_size / 1024).toFixed(1)} KB · Status: <strong>{r.status}</strong> · {new Date(r.created_at).toLocaleDateString()}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                      <button
                        type="button"
                        onClick={() => {
                          setSelectedResumeId(r.id);
                          setActiveTab('job_description');
                        }}
                        className="btn-secondary"
                        style={{ fontSize: 12, padding: '6px 10px', minHeight: 36 }}
                      >
                        Match vs JD →
                      </button>
                      <button
                        type="button"
                        onClick={() => deleteResume(r.id)}
                        style={{
                          background: 'rgba(239,68,68,0.1)',
                          border: '1px solid rgba(239,68,68,0.3)',
                          borderRadius: 8,
                          padding: '6px 10px',
                          minHeight: 36,
                          color: '#f87171',
                          cursor: 'pointer',
                          fontSize: 12,
                        }}
                      >
                        Delete
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {/* TAB 2: JOB DESCRIPTION ANALYZER */}
      {activeTab === 'job_description' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div className="glass-card" style={{ padding: 22 }}>
            <div style={{ marginBottom: 12 }}>
              <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc', margin: '0 0 4px' }}>
                Target Job Description Matcher
              </h3>
              <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
                Paste a real job posting to compare its required skills and experience against your uploaded resume.
              </p>
            </div>

            {jdError && (
              <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 8, padding: '10px 14px', color: '#f87171', fontSize: 13, marginBottom: 14 }}>
                {jdError}
              </div>
            )}

            {resumes.length === 0 ? (
              <div style={{ background: 'rgba(30,41,59,0.45)', borderRadius: 10, padding: 18, textAlign: 'center', color: '#94a3b8', fontSize: 13 }}>
                Upload at least one resume first before running job description comparison.
              </div>
            ) : (
              <form onSubmit={handleAnalyzeJobDescription} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
                <div>
                  <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>
                    Resume for Comparison
                  </label>
                  <select
                    className="input-field"
                    value={selectedResumeId}
                    onChange={e => setSelectedResumeId(e.target.value)}
                  >
                    {resumes.map(r => (
                      <option key={r.id} value={r.id}>
                        {r.original_filename} (Uploaded {new Date(r.created_at).toLocaleDateString()})
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>
                    Job Description Text
                  </label>
                  <textarea
                    className="input-field"
                    rows={7}
                    placeholder="Paste the job posting requirements, responsibilities, and qualifications here..."
                    value={jdText}
                    onChange={e => setJdText(e.target.value)}
                    required
                    style={{ resize: 'vertical' }}
                    id="job-description-input"
                  />
                </div>

                <div>
                  <button
                    className="btn-primary"
                    type="submit"
                    disabled={jdAnalyzing || !jdText.trim()}
                    id="analyze-jd-btn"
                  >
                    {jdAnalyzing ? 'Comparing Resume & JD…' : 'Compare Resume vs Job Description'}
                  </button>
                </div>
              </form>
            )}
          </div>

          {jdAnalysisResult && (
            <div className="glass-card" style={{ padding: 22 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 16, flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <span className="badge badge-primary" style={{ marginBottom: 6 }}>
                    {jdAnalysisResult.jobRoleDetected}
                  </span>
                  <h3 style={{ fontSize: 18, fontWeight: 700, color: '#f8fafc', margin: 0 }}>
                    Match Evaluation Breakdown
                  </h3>
                  <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 4 }}>
                    Experience Requirement: <strong>{jdAnalysisResult.experienceRequirement}</strong>
                  </div>
                </div>

                <div style={{ textAlign: 'right' }}>
                  <div style={{ fontSize: 28, fontWeight: 800, color: jdAnalysisResult.overallMatchPercent >= 70 ? '#10b981' : jdAnalysisResult.overallMatchPercent >= 45 ? '#f59e0b' : '#ef4444' }}>
                    {jdAnalysisResult.overallMatchPercent}%
                  </div>
                  <div style={{ fontSize: 11, color: '#94a3b8' }}>Overall Skill Match</div>
                </div>
              </div>

              <div style={{ width: '100%', height: 8, background: 'rgba(51, 65, 85, 0.4)', borderRadius: 4, overflow: 'hidden', marginBottom: 18 }}>
                <div
                  style={{
                    width: `${jdAnalysisResult.overallMatchPercent}%`,
                    height: '100%',
                    background: jdAnalysisResult.overallMatchPercent >= 70 ? '#10b981' : jdAnalysisResult.overallMatchPercent >= 45 ? '#f59e0b' : '#ef4444',
                    borderRadius: 4,
                  }}
                />
              </div>

              <div style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
                <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(16, 185, 129, 0.3)', borderRadius: 10, padding: 16 }}>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#34d399', marginBottom: 10 }}>
                    ✓ Matched Skills ({jdAnalysisResult.matchedSkills?.length || 0})
                  </div>
                  {jdAnalysisResult.matchedSkills?.length === 0 ? (
                    <div style={{ color: '#64748b', fontSize: 13 }}>No direct skill matches identified in resume text.</div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {jdAnalysisResult.matchedSkills.map((m: any, i: number) => (
                        <div key={i} style={{ borderBottom: i < jdAnalysisResult.matchedSkills.length - 1 ? '1px solid rgba(51, 65, 85, 0.3)' : 'none', paddingBottom: 8 }}>
                          <span className="badge badge-success" style={{ marginBottom: 4 }}>{m.skill}</span>
                          <div style={{ fontSize: 12, color: '#94a3b8', fontStyle: 'italic', marginTop: 3 }}>
                            "{m.evidenceInResume}"
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {jdAnalysisResult.partialSkills?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(245, 158, 11, 0.3)', borderRadius: 10, padding: 16 }}>
                    <div style={{ fontSize: 14, fontWeight: 700, color: '#fbbf24', marginBottom: 10 }}>
                      ⚡ Partial / Related Skills ({jdAnalysisResult.partialSkills.length})
                    </div>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {jdAnalysisResult.partialSkills.map((p: any, i: number) => (
                        <div key={i} style={{ borderBottom: i < jdAnalysisResult.partialSkills.length - 1 ? '1px solid rgba(51, 65, 85, 0.3)' : 'none', paddingBottom: 8 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                            <span className="badge badge-warning">{p.skill}</span>
                            <span style={{ fontSize: 11, color: '#64748b' }}>Related: {p.relatedSkillFound}</span>
                          </div>
                          <div style={{ fontSize: 12, color: '#cbd5e1', marginTop: 4 }}>{p.reason}</div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: 10, padding: 16 }}>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#f87171', marginBottom: 10 }}>
                    ✗ Missing Skills ({jdAnalysisResult.missingSkills?.length || 0})
                  </div>
                  {jdAnalysisResult.missingSkills?.length === 0 ? (
                    <div style={{ color: '#34d399', fontSize: 13 }}>All detected required technical skills appear in your resume.</div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {jdAnalysisResult.missingSkills.map((mis: any, i: number) => (
                        <div key={i} style={{ borderBottom: i < jdAnalysisResult.missingSkills.length - 1 ? '1px solid rgba(51, 65, 85, 0.3)' : 'none', paddingBottom: 8 }}>
                          <span className="badge badge-danger" style={{ marginBottom: 4 }}>{mis.skill}</span>
                          <div style={{ fontSize: 12, color: '#cbd5e1', marginTop: 3 }}>{mis.recommendation}</div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {jdAnalysisResult.softSkillsRequired?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.4)', border: '1px solid rgba(51, 65, 85, 0.4)', borderRadius: 10, padding: 14 }}>
                    <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>
                      Required Soft & Collaboration Competencies
                    </div>
                    <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                      {jdAnalysisResult.softSkillsRequired.map((s: string) => (
                        <span key={s} className="badge badge-neutral">{s}</span>
                      ))}
                    </div>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

function ScoreCard({ label, score, color }: { label: string; score: number; color: string }) {
  return (
    <div
      style={{
        background: 'rgba(15,23,42,0.6)',
        border: '1px solid rgba(51,65,85,0.4)',
        borderRadius: 10,
        padding: '12px 14px',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: 22, fontWeight: 700, color, marginBottom: 2 }}>{typeof score === 'number' ? `${score}%` : '—'}</div>
      <div style={{ fontSize: 11, color: '#94a3b8' }}>{label}</div>
    </div>
  );
}
