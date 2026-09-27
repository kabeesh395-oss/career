import { useEffect, useState, useRef } from 'react';
import { api } from '../../api/client';

type ProcessingStep = 'idle' | 'uploading' | 'parsing' | 'analyzing' | 'success' | 'error';

export default function ResumePage() {
  const [activeTab, setActiveTab] = useState<'resume' | 'job_description'>('resume');
  const [resumes, setResumes] = useState<any[]>([]);
  const [latestAnalysis, setLatestAnalysis] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  
  // Resume Processing State Machine
  const [processingStep, setProcessingStep] = useState<ProcessingStep>('idle');
  const [lastUploadedFile, setLastUploadedFile] = useState<File | null>(null);
  const fileRef = useRef<HTMLInputElement>(null);

  // Job Description Analyzer State
  const [jdText, setJdText] = useState('');
  const [selectedResumeId, setSelectedResumeId] = useState<string>('');
  const [jdAnalyzing, setJdAnalyzing] = useState(false);
  const [jdAnalysisResult, setJdAnalysisResult] = useState<any>(null);
  const [jdError, setJdError] = useState('');

  const loadResumes = async () => {
    try {
      const data = await api('/resume');
      setResumes(data.resumes || []);
      setLatestAnalysis(data.latestAnalysis || null);
      if (data.resumes?.[0]?.id) {
        setSelectedResumeId(data.resumes[0].id);
      }
    } catch { /* empty */ }
    setLoading(false);
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

      // Transition visual feedback: Uploading -> Parsing
      const parseTimer = setTimeout(() => {
        setProcessingStep('parsing');
      }, 700);

      // Transition visual feedback: Parsing -> Analyzing
      const analyzeTimer = setTimeout(() => {
        setProcessingStep('analyzing');
      }, 1500);

      const data = await api('/resume/upload', {
        method: 'POST',
        body: formData,
      });

      clearTimeout(parseTimer);
      clearTimeout(analyzeTimer);

      setResumes(prev => [data.resume, ...prev.filter(r => r.id !== data.resume.id)]);
      setLatestAnalysis(data.analysis);
      setSelectedResumeId(data.resume.id);
      setProcessingStep('success');

      if (fileRef.current) fileRef.current.value = '';
    } catch (err: any) {
      setError(err.message || 'Resume upload or analysis failed. Please verify the document format.');
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
    try {
      await api(`/resume/${id}`, { method: 'DELETE' });
      setResumes(prev => prev.filter(r => r.id !== id));
      if (latestAnalysis?.resume_id === id) setLatestAnalysis(null);
      if (selectedResumeId === id) {
        const remaining = resumes.filter(r => r.id !== id);
        setSelectedResumeId(remaining[0]?.id || '');
      }
    } catch { /* empty */ }
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
          resumeId: selectedResumeId || undefined
        })
      });

      setJdAnalysisResult(data.analysis);
    } catch (err: any) {
      setJdError(err.message || 'Failed to analyze job description.');
    } finally {
      setJdAnalyzing(false);
    }
  };

  const insertSampleJd = () => {
    setJdText(`Role: Senior Full Stack Engineer (Cloud & AI)
Experience: Minimum 4+ years of professional software engineering experience.
Requirements:
- Deep expertise in TypeScript, React, Next.js, and Node.js backend architecture.
- Strong proficiency in PostgreSQL database indexing, query optimization, and schema design.
- Hands-on experience with Docker & Containerization, Kubernetes, and AWS Cloud Architecture.
- Familiarity with LLM Prompting & Function Calling and RAG (Retrieval Augmented Generation).
- Strong cross-functional communication, agile collaboration, and mentorship abilities.`);
  };

  if (loading) {
    return <div style={{ color: '#94a3b8', padding: 40 }}>Loading Resume intelligence pipeline…</div>;
  }

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      {/* Title & Navigation Tabs */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 16 }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Resume & Job Intelligence</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>
            Multi-stage document extraction, ATS scoring, and targeted Job Description gap matching.
          </p>
        </div>

        {/* Tab Switcher */}
        <div style={{
          display: 'flex',
          background: 'rgba(15, 23, 42, 0.8)',
          border: '1px solid rgba(51, 65, 85, 0.6)',
          borderRadius: 10,
          padding: 3
        }}>
          <button
            onClick={() => setActiveTab('resume')}
            style={{
              padding: '8px 18px',
              borderRadius: 8,
              fontSize: 13,
              fontWeight: 600,
              cursor: 'pointer',
              border: 'none',
              background: activeTab === 'resume' ? '#2563eb' : 'transparent',
              color: activeTab === 'resume' ? '#ffffff' : '#94a3b8',
              transition: 'all 0.15s ease'
            }}
          >
            ATS Resume Analyzer
          </button>
          <button
            onClick={() => setActiveTab('job_description')}
            style={{
              padding: '8px 18px',
              borderRadius: 8,
              fontSize: 13,
              fontWeight: 600,
              cursor: 'pointer',
              border: 'none',
              background: activeTab === 'job_description' ? '#2563eb' : 'transparent',
              color: activeTab === 'job_description' ? '#ffffff' : '#94a3b8',
              transition: 'all 0.15s ease'
            }}
          >
            Job Description Matcher
          </button>
        </div>
      </div>

      {/* TAB 1: RESUME ATS PIPELINE */}
      {activeTab === 'resume' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {error && (
            <div style={{
              background: 'rgba(239,68,68,0.1)',
              border: '1px solid rgba(239,68,68,0.3)',
              borderRadius: 10,
              padding: '12px 16px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              color: '#f87171',
              fontSize: 13
            }}>
              <span>{error}</span>
              <button
                onClick={handleRetry}
                className="btn-secondary"
                style={{ fontSize: 12, padding: '4px 12px' }}
              >
                Retry Analysis
              </button>
            </div>
          )}

          {/* Upload Zone & Dynamic Multi-step Progress State */}
          <div className="glass-card" style={{ padding: 28, textAlign: 'center' }}>
            <input
              ref={fileRef}
              type="file"
              accept=".pdf,.txt,.doc,.docx"
              style={{ display: 'none' }}
              id="resume-file-input"
              onChange={handleFileSelected}
            />

            {processingStep === 'idle' || processingStep === 'success' ? (
              <div>
                <div style={{ fontSize: 38, marginBottom: 10 }}>📄</div>
                <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc', marginBottom: 6 }}>
                  Upload Candidate Resume
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13, marginBottom: 18, maxWidth: 500, margin: '0 auto 18px' }}>
                  Supports PDF, TXT, DOC, DOCX up to 10MB. Text is parsed into structured skills and evaluated against target role benchmarks.
                </p>
                <div style={{ display: 'flex', justifyContent: 'center', gap: 12 }}>
                  <button
                    className="btn-primary"
                    onClick={() => fileRef.current?.click()}
                    id="upload-resume-btn"
                  >
                    Select Resume File
                  </button>
                  {latestAnalysis && (
                    <button
                      className="btn-secondary"
                      onClick={() => setActiveTab('job_description')}
                    >
                      Compare with Job Listing →
                    </button>
                  )}
                </div>
              </div>
            ) : processingStep === 'error' ? (
              <div>
                <div style={{ fontSize: 38, marginBottom: 10 }}>⚠️</div>
                <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f87171', marginBottom: 6 }}>
                  Processing Encountered an Issue
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13, marginBottom: 18 }}>
                  Could not complete resume extraction. Please try uploading the document again or check file permissions.
                </p>
                <button className="btn-primary" onClick={handleRetry}>
                  Retry Document Analysis
                </button>
              </div>
            ) : (
              /* Multi-state visual progress indicator */
              <div style={{ maxWidth: 440, margin: '0 auto', padding: '10px 0' }}>
                <div style={{ fontSize: 32, marginBottom: 12 }}>
                  {processingStep === 'uploading' && '⬆️'}
                  {processingStep === 'parsing' && '🔍'}
                  {processingStep === 'analyzing' && '🧠'}
                </div>

                <div style={{ fontSize: 15, fontWeight: 600, color: '#f8fafc', marginBottom: 6 }}>
                  {processingStep === 'uploading' && 'Step 1/3: Uploading document to memory…'}
                  {processingStep === 'parsing' && 'Step 2/3: Parsing document text & extracting skills…'}
                  {processingStep === 'analyzing' && 'Step 3/3: Evaluating ATS scoring & role readiness…'}
                </div>

                <div style={{ width: '100%', height: 6, background: 'rgba(51, 65, 85, 0.4)', borderRadius: 3, overflow: 'hidden', margin: '14px 0 8px' }}>
                  <div style={{
                    height: '100%',
                    background: 'linear-gradient(90deg, #3b82f6, #38bdf8)',
                    borderRadius: 3,
                    width: processingStep === 'uploading' ? '33%' : processingStep === 'parsing' ? '66%' : '95%',
                    transition: 'width 0.4s ease'
                  }} />
                </div>

                <div style={{ fontSize: 12, color: '#64748b' }}>
                  Processing securely on-device with zero external data leakage.
                </div>
              </div>
            )}
          </div>

          {/* Latest Analysis Results */}
          {latestAnalysis ? (
            <div className="glass-card" style={{ padding: 24 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16, flexWrap: 'wrap', gap: 8 }}>
                <div>
                  <h3 style={{ fontSize: 17, fontWeight: 700, color: '#f8fafc' }}>
                    ATS Evaluation Results
                  </h3>
                  <div style={{ fontSize: 12, color: '#94a3b8', marginTop: 2 }}>
                    Calibrated against: <strong>{latestAnalysis.target_role || 'Target Role'}</strong>
                  </div>
                </div>
                {latestAnalysis.model_used && (
                  <span className="badge badge-neutral">Engine: {latestAnalysis.model_used}</span>
                )}
              </div>

              {/* Score Box Cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(130px, 1fr))', gap: 12, marginBottom: 20 }}>
                <ScoreCard label="Overall Match" score={latestAnalysis.overall_score} color="#3b82f6" />
                <ScoreCard label="Quantifiable Impact" score={latestAnalysis.impact_score} color="#10b981" />
                <ScoreCard label="Brevity & Density" score={latestAnalysis.brevity_score} color="#8b5cf6" />
                <ScoreCard label="Style & Hierarchy" score={latestAnalysis.style_score} color="#f59e0b" />
              </div>

              {/* Extracted Skills */}
              {latestAnalysis.skills_detected?.length > 0 && (
                <div style={{ marginBottom: 18 }}>
                  <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>
                    Extracted Technical Skills ({latestAnalysis.skills_detected.length})
                  </div>
                  <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                    {latestAnalysis.skills_detected.map((skill: string) => (
                      <span key={skill} className="badge badge-primary">{skill}</span>
                    ))}
                  </div>
                </div>
              )}

              {/* Strengths & Weaknesses Grid */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: 16, marginBottom: 18 }}>
                {latestAnalysis.strengths?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(16, 185, 129, 0.25)', borderRadius: 10, padding: 16 }}>
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
                  <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(239, 68, 68, 0.25)', borderRadius: 10, padding: 16 }}>
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

              {/* Recommendations */}
              {latestAnalysis.recommendations?.length > 0 && (
                <div style={{ background: 'rgba(15, 23, 42, 0.5)', border: '1px solid rgba(59, 130, 246, 0.25)', borderRadius: 10, padding: 16 }}>
                  <div style={{ fontSize: 13, fontWeight: 700, color: '#60a5fa', marginBottom: 8 }}>
                    Actionable ATS Recommendations
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
            <div className="glass-card" style={{ padding: 24, textAlign: 'center', color: '#64748b', fontSize: 13 }}>
              No resume analysis recorded yet. Upload a resume above to generate real ATS feedback.
            </div>
          )}

          {/* Upload History */}
          {resumes.length > 0 && (
            <div>
              <h3 style={{ fontSize: 15, fontWeight: 600, color: '#e2e8f0', marginBottom: 12 }}>
                Document Vault ({resumes.length})
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
                {resumes.map(r => (
                  <div key={r.id} className="glass-card" style={{
                    padding: '14px 18px',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    flexWrap: 'wrap',
                    gap: 10
                  }}>
                    <div>
                      <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0' }}>{r.original_filename}</div>
                      <div style={{ fontSize: 12, color: '#64748b', marginTop: 2 }}>
                        {(r.file_size / 1024).toFixed(1)} KB · Status: <strong>{r.status}</strong> · {new Date(r.created_at).toLocaleDateString()}
                      </div>
                    </div>
                    <div style={{ display: 'flex', gap: 8 }}>
                      <button
                        onClick={() => {
                          setSelectedResumeId(r.id);
                          setActiveTab('job_description');
                        }}
                        className="btn-secondary"
                        style={{ fontSize: 11, padding: '5px 10px' }}
                      >
                        Match vs JD →
                      </button>
                      <button
                        onClick={() => deleteResume(r.id)}
                        style={{
                          background: 'rgba(239,68,68,0.1)',
                          border: '1px solid rgba(239,68,68,0.3)',
                          borderRadius: 8,
                          padding: '5px 10px',
                          color: '#f87171',
                          cursor: 'pointer',
                          fontSize: 11
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

      {/* TAB 2: JOB DESCRIPTION ANALYZER (PHASE 6) */}
      {activeTab === 'job_description' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
          {/* JD Input Card */}
          <div className="glass-card" style={{ padding: 24 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12, flexWrap: 'wrap', gap: 8 }}>
              <div>
                <h3 style={{ fontSize: 16, fontWeight: 600, color: '#f8fafc' }}>
                  Target Job Description Analyzer
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13 }}>
                  Paste any job posting text. The analyzer extracts required technical skills, soft skills, and experience, comparing them directly with your resume.
                </p>
              </div>
              <button
                type="button"
                className="btn-secondary"
                onClick={insertSampleJd}
                style={{ fontSize: 11, padding: '6px 12px' }}
              >
                Paste Sample JD
              </button>
            </div>

            {jdError && (
              <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 8, padding: '10px 14px', color: '#f87171', fontSize: 13, marginBottom: 16 }}>
                {jdError}
              </div>
            )}

            <form onSubmit={handleAnalyzeJobDescription} style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
              {resumes.length > 0 && (
                <div>
                  <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>
                    Select Resume for Comparison
                  </label>
                  <select
                    className="input-field"
                    value={selectedResumeId}
                    onChange={e => setSelectedResumeId(e.target.value)}
                    style={{ background: 'rgba(15, 23, 42, 0.9)' }}
                  >
                    {resumes.map(r => (
                      <option key={r.id} value={r.id}>
                        {r.original_filename} (Uploaded {new Date(r.created_at).toLocaleDateString()})
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label style={{ display: 'block', fontSize: 12, color: '#94a3b8', marginBottom: 6 }}>
                  Job Description Text
                </label>
                <textarea
                  className="input-field"
                  rows={8}
                  placeholder="Paste the full job posting requirements, responsibilities, and qualifications here..."
                  value={jdText}
                  onChange={e => setJdText(e.target.value)}
                  required
                  style={{ resize: 'vertical' }}
                  id="job-description-input"
                />
              </div>

              <div style={{ display: 'flex', justifyContent: 'flex-start' }}>
                <button
                  className="btn-primary"
                  type="submit"
                  disabled={jdAnalyzing || !jdText.trim()}
                  id="analyze-jd-btn"
                >
                  {jdAnalyzing ? 'Analyzing Job Requirements…' : 'Extract Requirements & Compare'}
                </button>
              </div>
            </form>
          </div>

          {/* JD Analysis Output */}
          {jdAnalysisResult && (
            <div className="glass-card" style={{ padding: 24 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 18, flexWrap: 'wrap', gap: 12 }}>
                <div>
                  <span className="badge badge-primary" style={{ marginBottom: 6 }}>
                    {jdAnalysisResult.jobRoleDetected}
                  </span>
                  <h3 style={{ fontSize: 18, fontWeight: 700, color: '#f8fafc' }}>
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

              <div style={{ width: '100%', height: 8, background: 'rgba(51, 65, 85, 0.4)', borderRadius: 4, overflow: 'hidden', marginBottom: 20 }}>
                <div style={{
                  width: `${jdAnalysisResult.overallMatchPercent}%`,
                  height: '100%',
                  background: jdAnalysisResult.overallMatchPercent >= 70 ? '#10b981' : jdAnalysisResult.overallMatchPercent >= 45 ? '#f59e0b' : '#ef4444',
                  borderRadius: 4
                }} />
              </div>

              {/* 3 Categories: Matched, Partial, Missing */}
              <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
                {/* Matched Skills */}
                <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(16, 185, 129, 0.3)', borderRadius: 10, padding: 18 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                    <span style={{ fontSize: 14, fontWeight: 700, color: '#34d399' }}>
                      ✓ Matched Skills ({jdAnalysisResult.matchedSkills?.length || 0})
                    </span>
                    <span style={{ fontSize: 11, color: '#64748b' }}>Verified against resume content</span>
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

                {/* Partial Skills */}
                {jdAnalysisResult.partialSkills?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(245, 158, 11, 0.3)', borderRadius: 10, padding: 18 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                      <span style={{ fontSize: 14, fontWeight: 700, color: '#fbbf24' }}>
                        ⚡ Transferable / Partial Skills ({jdAnalysisResult.partialSkills.length})
                      </span>
                      <span style={{ fontSize: 11, color: '#64748b' }}>Foundational equivalents detected</span>
                    </div>

                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {jdAnalysisResult.partialSkills.map((p: any, i: number) => (
                        <div key={i} style={{ borderBottom: i < jdAnalysisResult.partialSkills.length - 1 ? '1px solid rgba(51, 65, 85, 0.3)' : 'none', paddingBottom: 8 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                            <span className="badge badge-warning">{p.skill}</span>
                            <span style={{ fontSize: 11, color: '#64748b' }}>Related: {p.relatedSkillFound}</span>
                          </div>
                          <div style={{ fontSize: 12, color: '#cbd5e1', marginTop: 4 }}>
                            {p.reason}
                          </div>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Missing Skills */}
                <div style={{ background: 'rgba(15, 23, 42, 0.6)', border: '1px solid rgba(239, 68, 68, 0.3)', borderRadius: 10, padding: 18 }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 12 }}>
                    <span style={{ fontSize: 14, fontWeight: 700, color: '#f87171' }}>
                      ✗ Missing Skills & Gaps ({jdAnalysisResult.missingSkills?.length || 0})
                    </span>
                    <span style={{ fontSize: 11, color: '#64748b' }}>High priority items to address</span>
                  </div>

                  {jdAnalysisResult.missingSkills?.length === 0 ? (
                    <div style={{ color: '#34d399', fontSize: 13 }}>Incredible match! All required technical skills are present in your resume.</div>
                  ) : (
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
                      {jdAnalysisResult.missingSkills.map((mis: any, i: number) => (
                        <div key={i} style={{ borderBottom: i < jdAnalysisResult.missingSkills.length - 1 ? '1px solid rgba(51, 65, 85, 0.3)' : 'none', paddingBottom: 8 }}>
                          <span className="badge badge-danger" style={{ marginBottom: 4 }}>{mis.skill}</span>
                          <div style={{ fontSize: 12, color: '#cbd5e1', marginTop: 3 }}>
                            {mis.recommendation}
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Soft Skills */}
                {jdAnalysisResult.softSkillsRequired?.length > 0 && (
                  <div style={{ background: 'rgba(15, 23, 42, 0.4)', border: '1px solid rgba(51, 65, 85, 0.4)', borderRadius: 10, padding: 16 }}>
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
    <div style={{
      background: 'rgba(15,23,42,0.6)',
      border: '1px solid rgba(51,65,85,0.4)',
      borderRadius: 10,
      padding: '12px 14px',
      textAlign: 'center'
    }}>
      <div style={{ fontSize: 24, fontWeight: 700, color, marginBottom: 2 }}>{score || 0}%</div>
      <div style={{ fontSize: 11, color: '#94a3b8' }}>{label}</div>
    </div>
  );
}
