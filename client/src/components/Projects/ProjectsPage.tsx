import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function ProjectsPage() {
  const [projects, setProjects] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState({
    title: '',
    description: '',
    repository_url: '',
    live_url: '',
    status: 'in_progress',
    technologies: '',
    skills_targeted: '',
  });

  const loadProjects = async () => {
    setError('');
    try {
      const data = await api('/projects');
      setProjects(data.projects || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load projects.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProjects();
  }, []);

  const handleSave = async (e?: React.FormEvent) => {
    e?.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editId) {
        const data = await api(`/projects/${editId}`, { method: 'PUT', body: JSON.stringify(form) });
        setProjects(prev => prev.map(p => (p.id === editId ? data.project : p)));
      } else {
        const data = await api('/projects', { method: 'POST', body: JSON.stringify(form) });
        setProjects(prev => [data.project, ...prev]);
      }
      resetForm();
    } catch (err: any) {
      setError(err?.message || 'Failed to save project.');
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: string) => {
    setError('');
    try {
      await api(`/projects/${id}`, { method: 'DELETE' });
      setProjects(prev => prev.filter(p => p.id !== id));
    } catch (err: any) {
      setError(err?.message || 'Failed to delete project.');
    }
  };

  const startEdit = (p: any) => {
    setError('');
    setForm({
      title: p.title || '',
      description: p.description || '',
      repository_url: p.repository_url || '',
      live_url: p.live_url || '',
      status: p.status || 'in_progress',
      technologies: p.technologies || '',
      skills_targeted: p.skills_targeted || '',
    });
    setEditId(p.id);
    setShowForm(true);
  };

  const resetForm = () => {
    setForm({
      title: '',
      description: '',
      repository_url: '',
      live_url: '',
      status: 'in_progress',
      technologies: '',
      skills_targeted: '',
    });
    setEditId(null);
    setShowForm(false);
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 32 }}>Loading projects…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
        <div>
          <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 2px' }}>Portfolio Projects</h2>
          <p style={{ color: '#94a3b8', fontSize: 13, margin: 0 }}>
            Track engineering builds, repositories, and targeted skills.
          </p>
        </div>
        <button
          type="button"
          className="btn-primary"
          onClick={() => {
            resetForm();
            setError('');
            setShowForm(true);
          }}
          id="add-project"
        >
          + New Project
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

      {showForm && (
        <form onSubmit={handleSave} className="glass-card" style={{ padding: 20 }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 14 }}>{editId ? 'Edit Project' : 'New Project'}</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
            <input
              className="input-field"
              placeholder="Project Title (min 2 characters)"
              value={form.title}
              onChange={e => setForm(f => ({ ...f, title: e.target.value }))}
              required
            />
            <textarea
              className="input-field"
              placeholder="Description"
              value={form.description}
              onChange={e => setForm(f => ({ ...f, description: e.target.value }))}
              style={{ minHeight: 80, resize: 'vertical' }}
            />
            <div className="responsive-grid-2">
              <input
                className="input-field"
                placeholder="Repository URL (https://... optional)"
                value={form.repository_url}
                onChange={e => setForm(f => ({ ...f, repository_url: e.target.value }))}
              />
              <input
                className="input-field"
                placeholder="Live Demo URL (https://... optional)"
                value={form.live_url}
                onChange={e => setForm(f => ({ ...f, live_url: e.target.value }))}
              />
            </div>
            <div className="responsive-grid-2">
              <input
                className="input-field"
                placeholder="Technologies (e.g. React, Node.js, PostgreSQL)"
                value={form.technologies}
                onChange={e => setForm(f => ({ ...f, technologies: e.target.value }))}
              />
              <input
                className="input-field"
                placeholder="Skills Targeted (e.g. System Design, SQL Indexing)"
                value={form.skills_targeted}
                onChange={e => setForm(f => ({ ...f, skills_targeted: e.target.value }))}
              />
            </div>
            <select className="input-field" value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
              <option value="planning">Planning</option>
              <option value="in_progress">In Progress</option>
              <option value="completed">Completed</option>
            </select>
            <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
              <button type="submit" className="btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Save Project'}
              </button>
              <button type="button" className="btn-secondary" onClick={resetForm}>
                Cancel
              </button>
            </div>
          </div>
        </form>
      )}

      {projects.length === 0 ? (
        <div className="glass-card" style={{ padding: 32, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14, margin: 0 }}>
            No portfolio projects yet. Click "+ New Project" to add your first build.
          </p>
        </div>
      ) : (
        <div className="responsive-cards-grid">
          {projects.map(p => (
            <div key={p.id} className="glass-card" style={{ padding: '18px 20px', display: 'flex', flexDirection: 'column', gap: 10 }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
                <h4 style={{ fontSize: 16, fontWeight: 700, color: '#e2e8f0', margin: 0 }}>{p.title}</h4>
                <span
                  className={`badge ${p.status === 'completed' ? 'badge-success' : p.status === 'in_progress' ? 'badge-primary' : 'badge-neutral'}`}
                  style={{ flexShrink: 0 }}
                >
                  {p.status.replace('_', ' ')}
                </span>
              </div>

              {p.description && <p style={{ color: '#94a3b8', fontSize: 13, margin: 0, lineHeight: 1.5 }}>{p.description}</p>}

              {p.technologies && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                  {p.technologies
                    .split(',')
                    .map((t: string) => t.trim())
                    .filter(Boolean)
                    .map((t: string) => (
                      <span key={t} className="badge badge-neutral">
                        {t}
                      </span>
                    ))}
                </div>
              )}

              {p.skills_targeted && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4 }}>
                  {p.skills_targeted
                    .split(',')
                    .map((s: string) => s.trim())
                    .filter(Boolean)
                    .map((s: string) => (
                      <span key={s} className="badge badge-primary" style={{ fontSize: 11 }}>
                        {s}
                      </span>
                    ))}
                </div>
              )}

              {(p.repository_url || p.live_url) && (
                <div style={{ display: 'flex', gap: 14, flexWrap: 'wrap' }}>
                  {p.repository_url && (
                    <a href={p.repository_url} target="_blank" rel="noopener noreferrer" style={{ color: '#60a5fa', fontSize: 12.5 }}>
                      Repository →
                    </a>
                  )}
                  {p.live_url && (
                    <a href={p.live_url} target="_blank" rel="noopener noreferrer" style={{ color: '#34d399', fontSize: 12.5 }}>
                      Live Demo →
                    </a>
                  )}
                </div>
              )}

              <div style={{ display: 'flex', gap: 8, marginTop: 'auto', paddingTop: 4 }}>
                <button type="button" className="btn-secondary" style={{ fontSize: 12, padding: '6px 12px', minHeight: 36 }} onClick={() => startEdit(p)}>
                  Edit
                </button>
                <button
                  type="button"
                  style={{
                    background: 'rgba(239,68,68,0.1)',
                    border: '1px solid rgba(239,68,68,0.3)',
                    borderRadius: 8,
                    padding: '6px 12px',
                    minHeight: 36,
                    color: '#f87171',
                    cursor: 'pointer',
                    fontSize: 12,
                  }}
                  onClick={() => handleDelete(p.id)}
                >
                  Delete
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
