import { useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

export default function ProfilePage() {
  const { profile, refreshProfile } = useAuth();
  const [form, setForm] = useState({
    headline: '',
    bio: '',
    location: '',
    education: '',
    experience_years: 0,
    target_role: '',
    target_industry: '',
    target_salary: ''
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    if (profile) {
      setForm({
        headline: profile.headline || '',
        bio: profile.bio || '',
        location: profile.location || '',
        education: profile.education || '',
        experience_years: profile.experience_years || 0,
        target_role: profile.target_role || '',
        target_industry: profile.target_industry || '',
        target_salary: profile.target_salary || ''
      });
    }
  }, [profile]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await api('/profile', {
        method: 'PUT',
        body: JSON.stringify(form)
      });
      setMessage('Profile updated successfully.');
      await refreshProfile();
    } catch (err: any) {
      setError(err.message || 'Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  const applyPreset = (role: string) => {
    switch (role) {
      case 'Full Stack Engineer':
        setForm({
          headline: 'Senior Full Stack Engineer | React & Node.js',
          bio: 'Full-stack software engineer specialized in reactive web interfaces, TypeScript, distributed microservices, and Postgres database optimization.',
          location: 'San Francisco, CA',
          education: 'B.S. Computer Science',
          experience_years: 3.5,
          target_role: 'Full Stack Engineer',
          target_industry: 'Fintech & Cloud Platforms',
          target_salary: '$140,000 - $180,000'
        });
        break;
      case 'Android Mobile Engineer':
        setForm({
          headline: 'Senior Android Architect | Jetpack Compose & Kotlin',
          bio: 'Android software engineer passionate about modern declarative UI, Kotlin Coroutines/Flow, offline-first Room persistence, and scalable MVI architecture.',
          location: 'San Francisco, CA',
          education: 'B.S. Software Engineering',
          experience_years: 4,
          target_role: 'Android Mobile Engineer',
          target_industry: 'Consumer Apps & Mobile FinTech',
          target_salary: '$145,000 - $185,000'
        });
        break;
      case 'AI / Machine Learning Engineer':
        setForm({
          headline: 'Applied AI & LLM Systems Engineer',
          bio: 'Machine learning practitioner engineering enterprise RAG retrieval systems, fine-tuned agent workflows, vector embeddings, and low-latency API inference.',
          location: 'Seattle, WA',
          education: 'M.S. Data Science & Machine Learning',
          experience_years: 3,
          target_role: 'AI / Machine Learning Engineer',
          target_industry: 'Enterprise AI & Autonomous Systems',
          target_salary: '$160,000 - $210,000'
        });
        break;
      case 'DevOps / Cloud Architect':
        setForm({
          headline: 'Cloud Infrastructure & Reliability Architect',
          bio: 'DevOps engineer focused on Terraform infrastructure-as-code, Kubernetes container orchestration, zero-downtime CI/CD pipelines, and Prometheus observability.',
          location: 'Austin, TX',
          education: 'B.S. Information Technology',
          experience_years: 4.5,
          target_role: 'DevOps / Cloud Architect',
          target_industry: 'Cloud Infrastructure & Security',
          target_salary: '$150,000 - $195,000'
        });
        break;
    }
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>User Profile</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Manage your professional goals, background, and compensation expectations.</p>
      </div>

      {/* 1-Click Career Starter Presets */}
      <div className="glass-card" style={{ padding: 20, border: '1px solid rgba(56, 189, 248, 0.3)', background: 'linear-gradient(145deg, rgba(15, 23, 42, 0.85) 0%, rgba(19, 29, 53, 0.9) 100%)' }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12, flexWrap: 'wrap', gap: 8 }}>
          <div>
            <h3 style={{ fontSize: 15, fontWeight: 700, color: '#38bdf8', marginBottom: 2 }}>⚡ 1-Click Career Starter Presets</h3>
            <p style={{ fontSize: 12, color: '#94a3b8' }}>Immediately populate realistic career goals and calibration benchmarks</p>
          </div>
        </div>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(200px, 1fr))', gap: 10 }}>
          {['Full Stack Engineer', 'Android Mobile Engineer', 'AI / Machine Learning Engineer', 'DevOps / Cloud Architect'].map(preset => (
            <button
              key={preset}
              type="button"
              onClick={() => applyPreset(preset)}
              style={{
                background: form.target_role === preset ? 'rgba(59, 130, 246, 0.2)' : 'rgba(30, 41, 59, 0.6)',
                border: `1px solid ${form.target_role === preset ? '#3b82f6' : 'rgba(51, 65, 85, 0.6)'}`,
                color: form.target_role === preset ? '#60a5fa' : '#e2e8f0',
                padding: '10px 12px',
                borderRadius: 8,
                fontSize: 12,
                fontWeight: 600,
                textAlign: 'left',
                cursor: 'pointer',
                transition: 'all 0.2s ease',
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center'
              }}
            >
              <span>{preset}</span>
              <span style={{ fontSize: 11, color: form.target_role === preset ? '#38bdf8' : '#94a3b8' }}>
                {form.target_role === preset ? 'Active' : 'Apply →'}
              </span>
            </button>
          ))}
        </div>
      </div>

      <div className="glass-card" style={{ padding: 28 }}>
        {message && (
          <div style={{ background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)', borderRadius: 10, padding: '10px 14px', color: '#34d399', fontSize: 13, marginBottom: 20 }}>
            ✓ {message}
          </div>
        )}
        {error && (
          <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13, marginBottom: 20 }}>
            ✗ {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Headline</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. Senior Full Stack Engineer | React & Node.js"
                value={form.headline}
                onChange={e => setForm(prev => ({ ...prev, headline: e.target.value }))}
                id="profile-headline"
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Location</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. San Francisco, CA"
                value={form.location}
                onChange={e => setForm(prev => ({ ...prev, location: e.target.value }))}
                id="profile-location"
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Professional Bio</label>
            <textarea
              className="input-field"
              rows={4}
              placeholder="Tell us about your background and interests..."
              value={form.bio}
              onChange={e => setForm(prev => ({ ...prev, bio: e.target.value }))}
              style={{ resize: 'vertical' }}
              id="profile-bio"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 20 }}>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Education</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. B.S. in Computer Science"
                value={form.education}
                onChange={e => setForm(prev => ({ ...prev, education: e.target.value }))}
                id="profile-education"
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Years of Experience</label>
              <input
                className="input-field"
                type="number"
                min={0}
                max={50}
                value={form.experience_years}
                onChange={e => setForm(prev => ({ ...prev, experience_years: parseInt(e.target.value) || 0 }))}
                id="profile-experience"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 20 }}>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Target Role</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. Senior Software Engineer"
                value={form.target_role}
                onChange={e => setForm(prev => ({ ...prev, target_role: e.target.value }))}
                required
                id="profile-target-role"
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Target Industry</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. Artificial Intelligence"
                value={form.target_industry}
                onChange={e => setForm(prev => ({ ...prev, target_industry: e.target.value }))}
                id="profile-target-industry"
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Target Salary</label>
              <input
                className="input-field"
                type="text"
                placeholder="e.g. $160,000/yr"
                value={form.target_salary}
                onChange={e => setForm(prev => ({ ...prev, target_salary: e.target.value }))}
                id="profile-target-salary"
              />
            </div>
          </div>

          <button className="btn-primary" type="submit" disabled={loading} style={{ alignSelf: 'flex-start', marginTop: 10 }} id="save-profile">
            {loading ? 'Saving Profile…' : 'Save Changes'}
          </button>
        </form>
      </div>
    </div>
  );
}
