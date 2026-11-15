import { useEffect, useMemo, useState } from 'react';
import { api } from '../../api/client';
import { useAuth } from '../../context/AuthContext';

interface CatalogSkill {
  id: string;
  name: string;
  category: string;
  demand_weight?: number;
}

interface UserSkill {
  id: string;
  skill_id: string;
  proficiency_level: number;
  verified?: number;
  source?: string;
  name: string;
  category: string;
}

const PROFICIENCY_LABELS: Record<number, string> = {
  1: 'L1 · Novice',
  2: 'L2 · Beginner',
  3: 'L3 · Intermediate',
  4: 'L4 · Advanced',
  5: 'L5 · Expert',
};

export default function MySkillsTab({ onNavigate }: { onNavigate?: (page: string) => void }) {
  const { refreshProfile } = useAuth();
  const [catalog, setCatalog] = useState<CatalogSkill[]>([]);
  const [userSkills, setUserSkills] = useState<UserSkill[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [search, setSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  const [draftLevels, setDraftLevels] = useState<Record<string, number>>({});
  const [savingId, setSavingId] = useState<string | null>(null);

  const loadSkills = async () => {
    setLoading(true);
    setError('');
    try {
      const [profileRes, catalogRes] = await Promise.all([
        api('/profile'),
        api('/career/skills-catalog'),
      ]);
      setUserSkills(profileRes?.skills || []);
      setCatalog(catalogRes?.skills || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load skills catalog and profile skills.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSkills();
  }, []);

  const userSkillMap = useMemo(() => {
    const map = new Map<string, UserSkill>();
    userSkills.forEach(s => map.set(s.skill_id, s));
    return map;
  }, [userSkills]);

  const categories = useMemo(() => {
    const unique = Array.from(new Set(catalog.map(c => c.category).filter(Boolean)));
    return ['All', ...unique];
  }, [catalog]);

  const filteredCatalog = useMemo(() => {
    const q = search.trim().toLowerCase();
    return catalog.filter(skill => {
      const matchesCat = selectedCategory === 'All' || skill.category === selectedCategory;
      const matchesQuery =
        !q || skill.name.toLowerCase().includes(q) || skill.category.toLowerCase().includes(q);
      return matchesCat && matchesQuery;
    });
  }, [catalog, search, selectedCategory]);

  const upsertSkill = async (skillId: string, level: number) => {
    setSavingId(skillId);
    setError('');
    try {
      const data = await api('/profile/skills', {
        method: 'POST',
        body: JSON.stringify({ skillId, proficiencyLevel: level }),
      });
      setUserSkills(data.skills || []);
      await refreshProfile();
    } catch (err: any) {
      setError(err?.message || 'Failed to save skill proficiency.');
    } finally {
      setSavingId(null);
    }
  };

  const removeSkill = async (skillId: string) => {
    setSavingId(skillId);
    setError('');
    try {
      const data = await api(`/profile/skills/${skillId}`, { method: 'DELETE' });
      setUserSkills(data.skills || []);
      await refreshProfile();
    } catch (err: any) {
      setError(err?.message || 'Failed to remove skill.');
    } finally {
      setSavingId(null);
    }
  };

  if (loading) {
    return <div style={{ color: '#94a3b8', padding: 32 }}>Loading skills catalog…</div>;
  }

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      {error && (
        <div
          style={{
            background: 'rgba(239,68,68,0.1)',
            border: '1px solid rgba(239,68,68,0.3)',
            borderRadius: 10,
            padding: '10px 14px',
            display: 'flex',
            justifyContent: 'space-between',
            alignItems: 'center',
            flexWrap: 'wrap',
            gap: 8,
            color: '#f87171',
            fontSize: 13,
          }}
        >
          <span>{error}</span>
          <button type="button" className="btn-secondary" style={{ fontSize: 12, padding: '4px 10px' }} onClick={loadSkills}>
            Retry
          </button>
        </div>
      )}

      {/* Recorded User Skills */}
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 10, marginBottom: 12 }}>
          <div>
            <h2 style={{ fontSize: 17, fontWeight: 700, margin: '0 0 2px' }}>
              Recorded Skills ({userSkills.length})
            </h2>
            <p style={{ color: '#94a3b8', fontSize: 12.5, margin: 0 }}>
              Your self-reported proficiency levels (L1–L5) feed directly into Career Gap Analysis.
            </p>
          </div>
          {onNavigate && (
            <button
              type="button"
              className="btn-secondary"
              style={{ fontSize: 12.5, padding: '7px 12px', minHeight: 38 }}
              onClick={() => onNavigate('skills/gaps')}
            >
              Run Gap Analysis →
            </button>
          )}
        </div>

        {userSkills.length === 0 ? (
          <div
            style={{
              background: 'rgba(15, 23, 42, 0.5)',
              border: '1px dashed rgba(100, 116, 139, 0.35)',
              borderRadius: 10,
              padding: 20,
              textAlign: 'center',
              color: '#94a3b8',
              fontSize: 13,
            }}
          >
            No skills added yet. Choose skills from the catalog below to record your current proficiency.
          </div>
        ) : (
          <div className="responsive-cards-grid">
            {userSkills.map(us => (
              <div
                key={us.id}
                style={{
                  background: 'rgba(15, 23, 42, 0.6)',
                  border: '1px solid rgba(59, 130, 246, 0.28)',
                  borderRadius: 12,
                  padding: '14px 16px',
                  display: 'flex',
                  flexDirection: 'column',
                  gap: 10,
                }}
              >
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
                  <div style={{ minWidth: 0 }}>
                    <div style={{ fontSize: 14.5, fontWeight: 700, color: '#f8fafc' }}>{us.name}</div>
                    <div style={{ fontSize: 11.5, color: '#94a3b8' }}>{us.category}</div>
                  </div>
                  <span className="badge badge-primary" style={{ fontSize: 11, flexShrink: 0 }}>
                    L{us.proficiency_level}/5
                  </span>
                </div>

                <div style={{ display: 'flex', gap: 8, alignItems: 'center', flexWrap: 'wrap' }}>
                  <select
                    aria-label={`Proficiency for ${us.name}`}
                    className="input-field"
                    value={us.proficiency_level}
                    disabled={savingId === us.skill_id}
                    onChange={e => upsertSkill(us.skill_id, parseInt(e.target.value, 10))}
                    style={{ flex: '1 1 130px', padding: '6px 10px', fontSize: 12.5 }}
                  >
                    {[1, 2, 3, 4, 5].map(lvl => (
                      <option key={lvl} value={lvl}>
                        {PROFICIENCY_LABELS[lvl]}
                      </option>
                    ))}
                  </select>
                  <button
                    type="button"
                    disabled={savingId === us.skill_id}
                    onClick={() => removeSkill(us.skill_id)}
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
                  >
                    Remove
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Canonical Skills Catalog */}
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ marginBottom: 14 }}>
          <h3 style={{ fontSize: 16, fontWeight: 700, margin: '0 0 4px' }}>Technical Skills Catalog</h3>
          <p style={{ color: '#94a3b8', fontSize: 12.5, margin: 0 }}>
            Search verified technical competencies from the database and add them to your profile.
          </p>
        </div>

        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginBottom: 16 }}>
          <input
            type="text"
            className="input-field"
            placeholder="Filter skills (e.g. TypeScript, PostgreSQL, Docker)..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ flex: '1 1 200px' }}
            id="skills-catalog-search"
          />
          <select
            aria-label="Filter by category"
            className="input-field"
            value={selectedCategory}
            onChange={e => setSelectedCategory(e.target.value)}
            style={{ flex: '0 1 200px' }}
          >
            {categories.map(cat => (
              <option key={cat} value={cat}>
                {cat}
              </option>
            ))}
          </select>
        </div>

        {filteredCatalog.length === 0 ? (
          <div style={{ color: '#94a3b8', fontSize: 13, textAlign: 'center', padding: 20 }}>
            No catalog skills match your current filter.
          </div>
        ) : (
          <div className="responsive-cards-grid">
            {filteredCatalog.map(skill => {
              const existing = userSkillMap.get(skill.id);
              const selectedLevel = draftLevels[skill.id] ?? existing?.proficiency_level ?? 3;
              const isSaving = savingId === skill.id;

              return (
                <div
                  key={skill.id}
                  style={{
                    background: 'rgba(15, 23, 42, 0.45)',
                    border: existing ? '1px solid rgba(16, 185, 129, 0.35)' : '1px solid rgba(51, 65, 85, 0.5)',
                    borderRadius: 12,
                    padding: '14px 16px',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 10,
                  }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', gap: 8 }}>
                    <div style={{ minWidth: 0 }}>
                      <div style={{ fontSize: 14, fontWeight: 600, color: '#f1f5f9' }}>{skill.name}</div>
                      <div style={{ fontSize: 11.5, color: '#64748b' }}>{skill.category}</div>
                    </div>
                    {existing && (
                      <span className="badge badge-success" style={{ fontSize: 10.5, flexShrink: 0 }}>
                        Saved L{existing.proficiency_level}
                      </span>
                    )}
                  </div>

                  <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
                    <select
                      aria-label={`Select level for ${skill.name}`}
                      className="input-field"
                      value={selectedLevel}
                      onChange={e =>
                        setDraftLevels(prev => ({
                          ...prev,
                          [skill.id]: parseInt(e.target.value, 10),
                        }))
                      }
                      style={{ flex: 1, padding: '6px 8px', fontSize: 12 }}
                    >
                      {[1, 2, 3, 4, 5].map(lvl => (
                        <option key={lvl} value={lvl}>
                          {PROFICIENCY_LABELS[lvl]}
                        </option>
                      ))}
                    </select>
                    <button
                      type="button"
                      className={existing ? 'btn-secondary' : 'btn-primary'}
                      disabled={isSaving}
                      onClick={() => upsertSkill(skill.id, selectedLevel)}
                      style={{ fontSize: 12, padding: '7px 12px', minHeight: 36 }}
                    >
                      {isSaving ? 'Saving…' : existing ? 'Update' : 'Add'}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
