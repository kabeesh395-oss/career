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

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>User Profile</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Manage your professional goals, background, and compensation expectations.</p>
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
