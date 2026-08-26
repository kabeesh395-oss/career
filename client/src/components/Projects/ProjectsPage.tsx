import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function ProjectsPage() {
  const [projects, setProjects] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editId, setEditId] = useState<string | null>(null);
  const [form, setForm] = useState({ title: '', description: '', repository_url: '', live_url: '', status: 'in_progress', technologies: '', skills_targeted: '' });

  const loadProjects = async () => {
    try { const data = await api('/projects'); setProjects(data.projects || []); } catch { /* empty */ }
    setLoading(false);
  };

  useEffect(() => { loadProjects(); }, []);

  const handleSave = async () => {
    try {
      if (editId) {
        const data = await api(`/projects/${editId}`, { method: 'PUT', body: JSON.stringify(form) });
        setProjects(prev => prev.map(p => p.id === editId ? data.project : p));
      } else {
        const data = await api('/projects', { method: 'POST', body: JSON.stringify(form) });
        setProjects(prev => [data.project, ...prev]);
      }
      resetForm();
    } catch { /* empty */ }
  };

  const handleDelete = async (id: string) => {
    try { await api(`/projects/${id}`, { method: 'DELETE' }); setProjects(prev => prev.filter(p => p.id !== id)); } catch { /* empty */ }
  };

  const startEdit = (p: any) => {
    setForm({ title: p.title, description: p.description || '', repository_url: p.repository_url || '', live_url: p.live_url || '', status: p.status, technologies: p.technologies || '', skills_targeted: p.skills_targeted || '' });
    setEditId(p.id);
    setShowForm(true);
  };

  const resetForm = () => {
    setForm({ title: '', description: '', repository_url: '', live_url: '', status: 'in_progress', technologies: '', skills_targeted: '' });
    setEditId(null);
    setShowForm(false);
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading projects…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div>
          <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Portfolio Projects</h1>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>Manage real projects to demonstrate your skills.</p>
        </div>
        <button className="btn-primary" onClick={() => { resetForm(); setShowForm(true); }} id="add-project">+ New Project</button>
      </div>

      {/* Form */}
      {showForm && (
        <div className="glass-card" style={{ padding: 24 }}>
          <h3 style={{ fontSize: 16, fontWeight: 600, marginBottom: 16 }}>{editId ? 'Edit Project' : 'New Project'}</h3>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 14 }}>
            <input className="input-field" placeholder="Project Title" value={form.title} onChange={e => setForm(f => ({ ...f, title: e.target.value }))} />
            <textarea className="input-field" placeholder="Description" value={form.description} onChange={e => setForm(f => ({ ...f, description: e.target.value }))} style={{ minHeight: 80, resize: 'vertical' }} />
            <input className="input-field" placeholder="Repository URL (optional)" value={form.repository_url} onChange={e => setForm(f => ({ ...f, repository_url: e.target.value }))} />
            <input className="input-field" placeholder="Live URL (optional)" value={form.live_url} onChange={e => setForm(f => ({ ...f, live_url: e.target.value }))} />
            <input className="input-field" placeholder="Technologies (e.g. React, Node.js, PostgreSQL)" value={form.technologies} onChange={e => setForm(f => ({ ...f, technologies: e.target.value }))} />
            <select className="input-field" value={form.status} onChange={e => setForm(f => ({ ...f, status: e.target.value }))}>
              <option value="planning">Planning</option>
              <option value="in_progress">In Progress</option>
              <option value="completed">Completed</option>
            </select>
            <div style={{ display: 'flex', gap: 10 }}>
              <button className="btn-primary" onClick={handleSave}>Save Project</button>
              <button className="btn-secondary" onClick={resetForm}>Cancel</button>
            </div>
          </div>
        </div>
      )}

      {/* Projects List */}
      {projects.length === 0 ? (
        <div className="glass-card" style={{ padding: 40, textAlign: 'center' }}>
          <p style={{ color: '#94a3b8', fontSize: 14 }}>No projects yet. Create your first portfolio project.</p>
        </div>
      ) : (
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(320px, 1fr))', gap: 16 }}>
          {projects.map(p => (
            <div key={p.id} className="glass-card" style={{ padding: '20px 22px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
                <h4 style={{ fontSize: 16, fontWeight: 700, color: '#e2e8f0' }}>{p.title}</h4>
                <span className={`badge ${p.status === 'completed' ? 'badge-success' : p.status === 'in_progress' ? 'badge-primary' : 'badge-neutral'}`}>
                  {p.status.replace('_', ' ')}
                </span>
              </div>
              {p.description && <p style={{ color: '#94a3b8', fontSize: 13, marginBottom: 10, lineHeight: 1.5 }}>{p.description}</p>}
              {p.technologies && (
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 4, marginBottom: 10 }}>
                  {p.technologies.split(',').map((t: string) => (
                    <span key={t.trim()} className="badge badge-neutral">{t.trim()}</span>
                  ))}
                </div>
              )}
              {p.repository_url && <a href={p.repository_url} target="_blank" rel="noopener noreferrer" style={{ color: '#60a5fa', fontSize: 12 }}>Repository →</a>}
              <div style={{ display: 'flex', gap: 8, marginTop: 14 }}>
                <button className="btn-secondary" style={{ fontSize: 12, padding: '6px 12px' }} onClick={() => startEdit(p)}>Edit</button>
                <button style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 8, padding: '6px 12px', color: '#f87171', cursor: 'pointer', fontSize: 12 }} onClick={() => handleDelete(p.id)}>Delete</button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
