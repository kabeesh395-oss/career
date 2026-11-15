import { Suspense, lazy, useEffect, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';

const IntegrationsPage = lazy(() => import('../Integrations/IntegrationsPage'));
const EdgeAiPage = lazy(() => import('../EdgeAI/EdgeAiPage'));

export type ProfileSubTab = 'general' | 'integrations' | 'system';

interface ProfilePageProps {
  initialTab?: ProfileSubTab;
  onNavigate?: (page: string) => void;
}

export default function ProfilePage({ initialTab = 'general', onNavigate }: ProfilePageProps) {
  const { user, profile, refreshProfile, logout } = useAuth();
  const { theme, setTheme, isDark } = useTheme();
  const [activeTab, setActiveTab] = useState<ProfileSubTab>(initialTab);

  const [form, setForm] = useState({
    headline: '',
    bio: '',
    location: '',
    education: '',
    experience_years: 0,
    target_role: '',
    target_industry: '',
    target_salary: '',
  });
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [deleteConfirmText, setDeleteConfirmText] = useState('');
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState('');

  useEffect(() => {
    setActiveTab(initialTab);
  }, [initialTab]);

  useEffect(() => {
    if (profile) {
      setForm({
        headline: profile.headline || '',
        bio: profile.bio || '',
        location: profile.location || '',
        education: profile.education || '',
        experience_years: profile.experience_years ?? 0,
        target_role: profile.target_role || '',
        target_industry: profile.target_industry || '',
        target_salary: profile.target_salary || '',
      });
    }
  }, [profile]);

  const switchTab = (tab: ProfileSubTab) => {
    setActiveTab(tab);
    onNavigate?.(`profile/${tab}`);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');
    setMessage('');
    try {
      await api('/profile', {
        method: 'PUT',
        body: JSON.stringify(form),
      });
      setMessage('Profile updated and saved to database.');
      await refreshProfile();
    } catch (err: any) {
      setError(err?.message || 'Failed to update profile.');
    } finally {
      setLoading(false);
    }
  };

  const applyPreset = (role: string) => {
    setMessage('Preset loaded into form fields below. Click "Save Changes" to persist.');
    switch (role) {
      case 'Full Stack Engineer':
        setForm(prev => ({
          ...prev,
          headline: 'Full Stack Engineer | React & Node.js',
          target_role: 'Full Stack Engineer',
          target_industry: 'Fintech & Cloud Platforms',
          target_salary: '$140,000 - $180,000',
        }));
        break;
      case 'Android Mobile Engineer':
        setForm(prev => ({
          ...prev,
          headline: 'Android Engineer | Jetpack Compose & Kotlin',
          target_role: 'Android Mobile Engineer',
          target_industry: 'Consumer Apps & Mobile FinTech',
          target_salary: '$145,000 - $185,000',
        }));
        break;
      case 'AI / Machine Learning Engineer':
        setForm(prev => ({
          ...prev,
          headline: 'Applied AI & ML Systems Engineer',
          target_role: 'AI / Machine Learning Engineer',
          target_industry: 'Enterprise AI & Autonomous Systems',
          target_salary: '$160,000 - $210,000',
        }));
        break;
      case 'DevOps / Cloud Architect':
        setForm(prev => ({
          ...prev,
          headline: 'Cloud Infrastructure & DevOps Architect',
          target_role: 'DevOps / Cloud Architect',
          target_industry: 'Cloud Infrastructure & Security',
          target_salary: '$150,000 - $195,000',
        }));
        break;
    }
  };

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
      {/* Header + Segmented Navigation */}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h1 style={{ fontSize: 22, fontWeight: 700, margin: '0 0 2px' }}>Profile & Calibration</h1>
          <p style={{ color: '#94a3b8', fontSize: 13.5, margin: 0 }}>
            {user?.fullName || 'Candidate'} ({user?.email || ''}) · Career background, integrations, and app settings.
          </p>
        </div>

        <div className="segmented-tabs" role="tablist" aria-label="Profile Views">
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'general'}
            className={`segmented-tab-btn ${activeTab === 'general' ? 'active' : ''}`}
            onClick={() => switchTab('general')}
          >
            Profile & Goals
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'integrations'}
            className={`segmented-tab-btn ${activeTab === 'integrations' ? 'active' : ''}`}
            onClick={() => switchTab('integrations')}
          >
            Integrations
          </button>
          <button
            type="button"
            role="tab"
            aria-selected={activeTab === 'system'}
            className={`segmented-tab-btn ${activeTab === 'system' ? 'active' : ''}`}
            onClick={() => switchTab('system')}
          >
            Theme & System AI
          </button>
        </div>
      </div>

      {activeTab === 'integrations' && (
        <Suspense fallback={<div style={{ color: '#94a3b8', padding: 32 }}>Loading integrations…</div>}>
          <IntegrationsPage />
        </Suspense>
      )}

      {activeTab === 'system' && (
        <div style={{ display: 'flex', flexDirection: 'column', gap: 20 }}>
          {/* Theme Appearance Control (wired to ThemeContext) */}
          <div className="glass-card" style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 12 }}>
              <div>
                <h2 style={{ fontSize: 17, fontWeight: 700, margin: '0 0 2px' }}>Appearance Theme</h2>
                <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
                  Current mode: <strong style={{ color: '#38bdf8' }}>{theme}</strong> (active: {isDark ? 'dark' : 'light'})
                </p>
              </div>
              <div className="segmented-tabs" role="group" aria-label="Theme selector">
                {(['system', 'dark', 'light'] as const).map(mode => (
                  <button
                    key={mode}
                    type="button"
                    onClick={() => setTheme(mode)}
                    className={`segmented-tab-btn ${theme === mode ? 'active' : ''}`}
                    style={{ textTransform: 'capitalize' }}
                  >
                    {mode}
                  </button>
                ))}
              </div>
            </div>
          </div>

          {/* Account Session Bar (accessible on mobile without sidebar!) */}
          <div className="glass-card" style={{ padding: 20, display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
            <div>
              <div style={{ fontSize: 15, fontWeight: 600 }}>Signed in as {user?.fullName || 'Candidate'}</div>
              <div style={{ fontSize: 12.5, color: '#94a3b8' }}>{user?.email}</div>
            </div>
            <button
              type="button"
              onClick={logout}
              id="profile-signout"
              style={{
                background: 'rgba(239, 68, 68, 0.12)',
                border: '1px solid rgba(239, 68, 68, 0.35)',
                color: '#f87171',
                padding: '9px 16px',
                minHeight: 42,
                borderRadius: 10,
                fontSize: 13,
                fontWeight: 600,
                cursor: 'pointer',
              }}
            >
              Sign Out
            </button>
          </div>

          <Suspense fallback={<div style={{ color: '#94a3b8', padding: 32 }}>Loading system AI…</div>}>
            <EdgeAiPage />
          </Suspense>
        </div>
      )}

      {activeTab === 'general' && (
        <>
          {/* Career Target Presets */}
          <div
            className="glass-card"
            style={{
              padding: 20,
              border: '1px solid rgba(56, 189, 248, 0.3)',
              background: 'linear-gradient(145deg, rgba(15, 23, 42, 0.85) 0%, rgba(19, 29, 53, 0.9) 100%)',
            }}
          >
            <div style={{ marginBottom: 12 }}>
              <h2 style={{ fontSize: 15, fontWeight: 700, color: '#38bdf8', margin: '0 0 2px' }}>
                ⚡ Career Target Presets
              </h2>
              <p style={{ fontSize: 12.5, color: '#94a3b8', margin: 0 }}>
                Updates only Headline, Target Role, Target Industry, and Target Salary in the form below without erasing your bio, location, education, or experience.
              </p>
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 200px), 1fr))', gap: 10 }}>
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
                    minHeight: 44,
                    borderRadius: 8,
                    fontSize: 12.5,
                    fontWeight: 600,
                    textAlign: 'left',
                    cursor: 'pointer',
                    display: 'flex',
                    justifyContent: 'space-between',
                    alignItems: 'center',
                    gap: 8,
                  }}
                >
                  <span>{preset}</span>
                  <span style={{ fontSize: 11, color: form.target_role === preset ? '#38bdf8' : '#94a3b8', flexShrink: 0 }}>
                    {form.target_role === preset ? 'Selected' : 'Apply →'}
                  </span>
                </button>
              ))}
            </div>
          </div>

          <div className="glass-card" style={{ padding: 24 }}>
            {message && (
              <div style={{ background: 'rgba(16,185,129,0.1)', border: '1px solid rgba(16,185,129,0.3)', borderRadius: 10, padding: '10px 14px', color: '#34d399', fontSize: 13, marginBottom: 18 }}>
                ✓ {message}
              </div>
            )}
            {error && (
              <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13, marginBottom: 18 }}>
                ✗ {error}
              </div>
            )}

            <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
              <div className="responsive-grid-2">
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Headline</label>
                  <input
                    className="input-field"
                    type="text"
                    placeholder="e.g. Full Stack Engineer | React & Node.js"
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
                  placeholder="Describe your background, engineering focus, and goals..."
                  value={form.bio}
                  onChange={e => setForm(prev => ({ ...prev, bio: e.target.value }))}
                  style={{ resize: 'vertical' }}
                  id="profile-bio"
                />
              </div>

              <div className="responsive-grid-2">
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
                    step="0.5"
                    min={0}
                    max={50}
                    value={form.experience_years}
                    onChange={e => {
                      const val = parseFloat(e.target.value);
                      setForm(prev => ({ ...prev, experience_years: Number.isNaN(val) ? 0 : val }));
                    }}
                    id="profile-experience"
                  />
                </div>
              </div>

              <div className="responsive-grid-3">
                <div>
                  <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>Target Role</label>
                  <input
                    className="input-field"
                    type="text"
                    placeholder="e.g. Full Stack Engineer"
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
                    placeholder="e.g. Cloud & AI"
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
                    placeholder="e.g. $140,000 - $180,000"
                    value={form.target_salary}
                    onChange={e => setForm(prev => ({ ...prev, target_salary: e.target.value }))}
                    id="profile-target-salary"
                  />
                </div>
              </div>

              <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', paddingTop: 4 }}>
                <button className="btn-primary" type="submit" disabled={loading} id="save-profile">
                  {loading ? 'Saving Profile…' : 'Save Changes'}
                </button>
                <button
                  type="button"
                  className="btn-secondary"
                  onClick={logout}
                  style={{ marginLeft: 'auto' }}
                >
                  Sign Out
                </button>
              </div>
            </form>
          </div>

          {/* Danger Zone: Permanent Account Deletion */}
          <div className="glass-card" style={{ padding: 20, border: '1px solid rgba(239, 68, 68, 0.35)', background: 'rgba(239, 68, 68, 0.03)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 14 }}>
              <div>
                <h3 style={{ fontSize: 15.5, fontWeight: 700, color: '#f87171', margin: '0 0 4px' }}>
                  Account & Data Permanent Erasure
                </h3>
                <p style={{ color: '#94a3b8', fontSize: 13, maxWidth: 600, margin: 0 }}>
                  Permanently delete your account, profile, resumes, ATS reports, roadmap, projects, and activity logs.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowDeleteModal(true)}
                style={{
                  background: 'rgba(239, 68, 68, 0.12)',
                  border: '1px solid rgba(239, 68, 68, 0.4)',
                  color: '#f87171',
                  padding: '8px 16px',
                  minHeight: 40,
                  borderRadius: 8,
                  fontSize: 13,
                  fontWeight: 600,
                  cursor: 'pointer',
                }}
                id="open-delete-account-modal"
              >
                Delete Account…
              </button>
            </div>

            {showDeleteModal && (
              <div
                style={{
                  marginTop: 16,
                  padding: 16,
                  background: 'rgba(15, 23, 42, 0.95)',
                  border: '1px solid rgba(239, 68, 68, 0.5)',
                  borderRadius: 10,
                }}
              >
                <h4 style={{ fontSize: 14, fontWeight: 700, color: '#f87171', margin: '0 0 6px' }}>
                  Confirm Complete Account Deletion
                </h4>
                <p style={{ fontSize: 12.5, color: '#cbd5e1', margin: '0 0 12px' }}>
                  Type <strong>DELETE</strong> below to confirm irreversible deletion:
                </p>
                {deleteError && <div style={{ color: '#f87171', fontSize: 12, marginBottom: 8 }}>{deleteError}</div>}
                <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
                  <input
                    className="input-field"
                    type="text"
                    placeholder="Type DELETE"
                    value={deleteConfirmText}
                    onChange={e => setDeleteConfirmText(e.target.value)}
                    style={{ maxWidth: 200 }}
                    id="delete-account-confirm-input"
                  />
                  <button
                    type="button"
                    disabled={deleteConfirmText !== 'DELETE' || deleting}
                    onClick={async () => {
                      setDeleting(true);
                      setDeleteError('');
                      try {
                        await api('/user/delete', { method: 'DELETE' });
                        logout();
                      } catch (err: any) {
                        setDeleteError(err?.message || 'Failed to delete account.');
                        setDeleting(false);
                      }
                    }}
                    style={{
                      background: deleteConfirmText === 'DELETE' ? '#dc2626' : 'rgba(239, 68, 68, 0.2)',
                      color: '#ffffff',
                      border: 'none',
                      padding: '8px 16px',
                      minHeight: 40,
                      borderRadius: 8,
                      fontSize: 12.5,
                      fontWeight: 600,
                      cursor: deleteConfirmText === 'DELETE' ? 'pointer' : 'not-allowed',
                    }}
                    id="confirm-delete-account-btn"
                  >
                    {deleting ? 'Erasing Data…' : 'Permanently Delete Everything'}
                  </button>
                  <button
                    type="button"
                    onClick={() => {
                      setShowDeleteModal(false);
                      setDeleteConfirmText('');
                    }}
                    className="btn-secondary"
                    style={{ fontSize: 12.5, padding: '8px 14px', minHeight: 40 }}
                  >
                    Cancel
                  </button>
                </div>
              </div>
            )}
          </div>
        </>
      )}
    </div>
  );
}
