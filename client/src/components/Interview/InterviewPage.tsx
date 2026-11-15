import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

type Difficulty = 'junior' | 'intermediate' | 'advanced' | 'expert';

export default function InterviewPage() {
  const { profile } = useAuth();
  const [interviews, setInterviews] = useState<any[]>([]);
  const [activeInterview, setActiveInterview] = useState<any>(null);
  const [questions, setQuestions] = useState<any[]>([]);
  const [answers, setAnswers] = useState<any[]>([]);
  const [currentQ, setCurrentQ] = useState(0);
  const [answerText, setAnswerText] = useState('');
  const [difficulty, setDifficulty] = useState<Difficulty>('intermediate');
  const [submitting, setSubmitting] = useState(false);
  const [starting, setStarting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastEval, setLastEval] = useState<any>(null);

  const loadHistory = async () => {
    setError('');
    try {
      const data = await api('/interview/history');
      setInterviews(data.interviews || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load interview history.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadHistory();
  }, []);

  const startInterview = async () => {
    setStarting(true);
    setError('');
    try {
      const payload: Record<string, any> = { difficulty };
      if (profile?.target_role) {
        payload.targetRole = profile.target_role;
      }
      const data = await api('/interview/start', { method: 'POST', body: JSON.stringify(payload) });
      setActiveInterview(data.interview);
      setQuestions(data.questions || []);
      setAnswers([]);
      setCurrentQ(0);
      setLastEval(null);
    } catch (err: any) {
      setError(err?.message || 'Failed to start interview.');
    } finally {
      setStarting(false);
    }
  };

  const loadInterview = async (id: string) => {
    setError('');
    try {
      const data = await api(`/interview/${id}`);
      setActiveInterview(data.interview);
      setQuestions(data.questions || []);
      setAnswers(data.answers || []);
      setCurrentQ(data.answers?.length || 0);
      setLastEval(null);
    } catch (err: any) {
      setError(err?.message || 'Failed to open interview session.');
    }
  };

  const exitSessionToHistory = () => {
    setActiveInterview(null);
    setQuestions([]);
    setAnswers([]);
    setCurrentQ(0);
    setLastEval(null);
    setError('');
    loadHistory();
  };

  const submitAnswer = async () => {
    if (!answerText.trim() || !activeInterview || !questions[currentQ]) return;
    setSubmitting(true);
    setError('');
    try {
      const data = await api(`/interview/${activeInterview.id}/answer`, {
        method: 'POST',
        body: JSON.stringify({ questionId: questions[currentQ].id, answerText: answerText.trim() }),
      });
      setAnswers(prev => [...prev, data.answer]);
      setActiveInterview(data.interview);
      setLastEval(data.evaluation);
      setAnswerText('');
      if (currentQ + 1 < questions.length) setCurrentQ(prev => prev + 1);
    } catch (err: any) {
      setError(err?.message || 'Submission failed.');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 32 }}>Loading interviews…</div>;

  // Active interview session
  if (activeInterview && activeInterview.status === 'in_progress' && currentQ < questions.length) {
    const q = questions[currentQ];
    const isAnswered = answers.some(a => a.question_id === q.id);

    return (
      <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
          <div>
            <h2 style={{ fontSize: 19, fontWeight: 700, margin: '0 0 2px' }}>Mock Interview: {activeInterview.role_target}</h2>
            <span style={{ fontSize: 12, color: '#94a3b8' }}>Difficulty: {activeInterview.difficulty}</span>
          </div>
          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
            <span className="badge badge-primary">Q{currentQ + 1} / {questions.length}</span>
            <button type="button" className="btn-secondary" style={{ fontSize: 12, padding: '6px 12px', minHeight: 36 }} onClick={exitSessionToHistory}>
              ← History
            </button>
          </div>
        </div>

        {error && (
          <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>
            {error}
          </div>
        )}

        {lastEval && (
          <div className="glass-card" style={{ padding: 18, borderLeft: '3px solid #10b981' }}>
            <div style={{ fontSize: 14, fontWeight: 600, color: '#34d399', marginBottom: 6 }}>
              Previous Answer Evaluation — Score: {lastEval.score}/100
            </div>
            <p style={{ color: '#cbd5e1', fontSize: 13, margin: '0 0 6px' }}>{lastEval.feedback}</p>
            <p style={{ color: '#94a3b8', fontSize: 12, margin: 0 }}>Suggestion: {lastEval.suggestedImprovement}</p>
          </div>
        )}

        <div className="glass-card" style={{ padding: 22 }}>
          <span className="badge badge-neutral" style={{ marginBottom: 10 }}>{q.category} · {q.difficulty}</span>
          <h3 style={{ fontSize: 16.5, fontWeight: 600, lineHeight: 1.55, marginBottom: 16 }}>{q.question_text}</h3>

          {isAnswered ? (
            <p style={{ color: '#34d399', fontSize: 14 }}>✓ Answer submitted for this question.</p>
          ) : (
            <>
              <textarea
                className="input-field"
                placeholder="Write a structured response covering architecture, trade-offs, and failure handling..."
                value={answerText}
                onChange={e => setAnswerText(e.target.value)}
                style={{ minHeight: 140, resize: 'vertical', marginBottom: 14 }}
                id="interview-answer"
              />
              <button
                type="button"
                className="btn-primary"
                onClick={submitAnswer}
                disabled={submitting || !answerText.trim()}
                id="submit-answer"
              >
                {submitting ? 'Evaluating…' : 'Submit & Score Answer'}
              </button>
            </>
          )}
        </div>
      </div>
    );
  }

  // Completed interview or results view
  if (activeInterview && (activeInterview.status === 'completed' || currentQ >= questions.length)) {
    return (
      <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10 }}>
          <h2 style={{ fontSize: 20, fontWeight: 700, margin: 0 }}>Interview Summary</h2>
          <button type="button" className="btn-secondary" onClick={exitSessionToHistory}>
            ← Back to History
          </button>
        </div>

        <div className="glass-card glow-accent" style={{ padding: 24, textAlign: 'center' }}>
          <div style={{ fontSize: 44, fontWeight: 800, marginBottom: 6 }} className="gradient-text">
            {activeInterview.overall_score}%
          </div>
          <p style={{ color: '#cbd5e1', fontSize: 14, margin: '0 0 4px' }}>Overall Score for {activeInterview.role_target}</p>
          <p style={{ color: '#94a3b8', fontSize: 12, margin: 0 }}>
            {activeInterview.completed_questions} / {activeInterview.total_questions} questions answered · {activeInterview.difficulty}
          </p>
        </div>

        {answers.map((a: any, i: number) => (
          <div key={a.id || i} className="glass-card" style={{ padding: '16px 18px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#e2e8f0' }}>Question {i + 1}</span>
              <span className="badge badge-success">Score: {a.score}/100</span>
            </div>
            <p style={{ color: '#cbd5e1', fontSize: 13, margin: '0 0 6px' }}>{a.feedback}</p>
            {a.suggested_improvement && (
              <p style={{ color: '#94a3b8', fontSize: 12, margin: 0 }}>Improvement: {a.suggested_improvement}</p>
            )}
          </div>
        ))}

        <div>
          <button type="button" className="btn-primary" onClick={exitSessionToHistory}>
            Back to History
          </button>
        </div>
      </div>
    );
  }

  // Interview history & start
  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 14 }}>
          <div>
            <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 4px' }}>Mock Interview Simulator</h2>
            <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
              Target role: <strong style={{ color: '#e2e8f0' }}>{profile?.target_role || 'Full Stack Engineer (default)'}</strong> · Answer evaluation uses deterministic rubric scoring.
            </p>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: 10, flexWrap: 'wrap' }}>
            <select
              aria-label="Interview Difficulty"
              className="input-field"
              value={difficulty}
              onChange={e => setDifficulty(e.target.value as Difficulty)}
              style={{ width: 'auto', minWidth: 140 }}
              id="interview-difficulty"
            >
              <option value="junior">Junior</option>
              <option value="intermediate">Intermediate</option>
              <option value="advanced">Advanced</option>
              <option value="expert">Expert</option>
            </select>
            <button type="button" className="btn-primary" onClick={startInterview} disabled={starting} id="start-interview">
              {starting ? 'Starting…' : 'Start Interview'}
            </button>
          </div>
        </div>
      </div>

      {error && (
        <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>
          {error}
        </div>
      )}

      {interviews.length === 0 ? (
        <div className="glass-card" style={{ padding: 32, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14, margin: 0 }}>
            No mock interviews completed yet. Select a difficulty above to begin your first session.
          </p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <h3 style={{ fontSize: 15, fontWeight: 600, margin: 0 }}>Interview History ({interviews.length})</h3>
          {interviews.map(iv => (
            <div
              key={iv.id}
              className="glass-card"
              style={{
                padding: '14px 16px',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                flexWrap: 'wrap',
                gap: 10,
                cursor: 'pointer',
              }}
              onClick={() => loadInterview(iv.id)}
            >
              <div>
                <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0' }}>{iv.role_target}</div>
                <div style={{ fontSize: 12, color: '#94a3b8' }}>
                  {iv.difficulty} · {iv.completed_questions}/{iv.total_questions} answered · {new Date(iv.created_at).toLocaleDateString()}
                </div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className={`badge ${iv.status === 'completed' ? 'badge-success' : 'badge-warning'}`}>{iv.status}</span>
                {iv.overall_score > 0 && <div style={{ fontSize: 17, fontWeight: 700, color: '#60a5fa', marginTop: 4 }}>{iv.overall_score}%</div>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
