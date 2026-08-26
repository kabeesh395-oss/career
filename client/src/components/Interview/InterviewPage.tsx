import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function InterviewPage() {
  const [interviews, setInterviews] = useState<any[]>([]);
  const [activeInterview, setActiveInterview] = useState<any>(null);
  const [questions, setQuestions] = useState<any[]>([]);
  const [answers, setAnswers] = useState<any[]>([]);
  const [currentQ, setCurrentQ] = useState(0);
  const [answerText, setAnswerText] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [starting, setStarting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [lastEval, setLastEval] = useState<any>(null);

  useEffect(() => {
    async function load() {
      try { const data = await api('/interview/history'); setInterviews(data.interviews || []); } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  const startInterview = async () => {
    setStarting(true);
    setError('');
    try {
      const data = await api('/interview/start', { method: 'POST', body: JSON.stringify({ difficulty: 'intermediate' }) });
      setActiveInterview(data.interview);
      setQuestions(data.questions || []);
      setAnswers([]);
      setCurrentQ(0);
      setLastEval(null);
    } catch (err: any) {
      setError(err.message || 'Failed to start interview.');
    }
    setStarting(false);
  };

  const loadInterview = async (id: string) => {
    try {
      const data = await api(`/interview/${id}`);
      setActiveInterview(data.interview);
      setQuestions(data.questions || []);
      setAnswers(data.answers || []);
      setCurrentQ(data.answers?.length || 0);
      setLastEval(null);
    } catch { /* empty */ }
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
      setError(err.message || 'Submission failed.');
    }
    setSubmitting(false);
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading interviews…</div>;

  // Active interview session
  if (activeInterview && activeInterview.status === 'in_progress' && currentQ < questions.length) {
    const q = questions[currentQ];
    const isAnswered = answers.some(a => a.question_id === q.id);

    return (
      <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <h1 style={{ fontSize: 22, fontWeight: 700 }}>Mock Interview: {activeInterview.role_target}</h1>
          <span className="badge badge-primary">Q{currentQ + 1} / {questions.length}</span>
        </div>

        {error && <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>{error}</div>}

        {lastEval && (
          <div className="glass-card" style={{ padding: 20, borderLeft: '3px solid #10b981' }}>
            <div style={{ fontSize: 14, fontWeight: 600, color: '#34d399', marginBottom: 8 }}>Previous Answer Evaluation — Score: {lastEval.score}/100</div>
            <p style={{ color: '#94a3b8', fontSize: 13 }}>{lastEval.feedback}</p>
            <p style={{ color: '#64748b', fontSize: 12, marginTop: 4 }}>💡 {lastEval.suggestedImprovement}</p>
          </div>
        )}

        <div className="glass-card" style={{ padding: '24px 28px' }}>
          <span className="badge badge-neutral" style={{ marginBottom: 10 }}>{q.category} · {q.difficulty}</span>
          <h2 style={{ fontSize: 17, fontWeight: 600, lineHeight: 1.6, marginBottom: 20 }}>{q.question_text}</h2>

          {isAnswered ? (
            <p style={{ color: '#34d399', fontSize: 14 }}>✓ Answer submitted for this question.</p>
          ) : (
            <>
              <textarea
                className="input-field"
                placeholder="Type your answer here... Be specific about architectural decisions, trade-offs, and implementation details."
                value={answerText}
                onChange={e => setAnswerText(e.target.value)}
                style={{ minHeight: 140, resize: 'vertical', marginBottom: 16 }}
                id="interview-answer"
              />
              <button className="btn-primary" onClick={submitAnswer} disabled={submitting || !answerText.trim()} id="submit-answer">
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
      <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
        <h1 style={{ fontSize: 24, fontWeight: 700 }}>Interview Complete</h1>
        <div className="glass-card glow-accent" style={{ padding: 28, textAlign: 'center' }}>
          <div style={{ fontSize: 52, fontWeight: 800, marginBottom: 8 }} className="gradient-text">
            {activeInterview.overall_score}%
          </div>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>Overall Score for {activeInterview.role_target}</p>
          <p style={{ color: '#64748b', fontSize: 12, marginTop: 4 }}>
            {activeInterview.completed_questions} / {activeInterview.total_questions} questions answered
          </p>
        </div>

        {answers.map((a: any, i: number) => (
          <div key={i} className="glass-card" style={{ padding: '16px 20px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
              <span style={{ fontSize: 13, fontWeight: 600, color: '#e2e8f0' }}>Q{i + 1}</span>
              <span className="badge badge-success">Score: {a.score}</span>
            </div>
            <p style={{ color: '#94a3b8', fontSize: 12 }}>{a.feedback}</p>
          </div>
        ))}

        <button className="btn-primary" onClick={() => { setActiveInterview(null); setQuestions([]); setAnswers([]); setCurrentQ(0); setLastEval(null); }}>
          Back to History
        </button>
      </div>
    );
  }

  // Interview history & start
  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Mock Interview Simulator</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>Practice real technical questions with instant rubric scoring.</p>
        </div>
        <button className="btn-primary" onClick={startInterview} disabled={starting} id="start-interview">
          {starting ? 'Starting…' : 'Start New Interview'}
        </button>
      </div>

      {error && <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>{error}</div>}

      {interviews.length === 0 ? (
        <div className="glass-card" style={{ padding: 40, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>No interviews yet. Start your first mock interview session.</p>
        </div>
      ) : (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <h3 style={{ fontSize: 15, fontWeight: 600 }}>Interview History</h3>
          {interviews.map(iv => (
            <div key={iv.id} className="glass-card" style={{
              padding: '14px 18px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', cursor: 'pointer'
            }} onClick={() => loadInterview(iv.id)}>
              <div>
                <div style={{ fontSize: 14, fontWeight: 600, color: '#e2e8f0' }}>{iv.role_target}</div>
                <div style={{ fontSize: 12, color: '#64748b' }}>{iv.difficulty} · {iv.completed_questions}/{iv.total_questions} answered · {new Date(iv.created_at).toLocaleDateString()}</div>
              </div>
              <div style={{ textAlign: 'right' }}>
                <span className={`badge ${iv.status === 'completed' ? 'badge-success' : 'badge-warning'}`}>{iv.status}</span>
                {iv.overall_score > 0 && <div style={{ fontSize: 18, fontWeight: 700, color: '#60a5fa', marginTop: 4 }}>{iv.overall_score}%</div>}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
