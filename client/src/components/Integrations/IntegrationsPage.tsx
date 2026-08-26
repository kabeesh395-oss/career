import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function IntegrationsPage() {
  const [integrations, setIntegrations] = useState<any[]>([]);
  const [githubUser, setGithubUser] = useState('');
  const [connecting, setConnecting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    async function load() {
      try { const data = await api('/integrations'); setIntegrations(data.integrations || []); } catch { /* empty */ }
      setLoading(false);
    }
    load();
  }, []);

  const github = integrations.find(i => i.provider === 'github');

  const connectGitHub = async () => {
    if (!githubUser.trim()) return;
    setConnecting(true);
    setError('');
    try {
      const data = await api('/integrations/github/connect', {
        method: 'POST', body: JSON.stringify({ username: githubUser.trim() }),
      });
      setIntegrations(prev => {
        const filtered = prev.filter(i => i.provider !== 'github');
        return [...filtered, data.integration];
      });
      setGithubUser('');
    } catch (err: any) {
      setError(err.message || 'Failed to connect GitHub.');
    }
    setConnecting(false);
  };

  const disconnect = async (provider: string) => {
    try {
      await api(`/integrations/${provider}`, { method: 'DELETE' });
      setIntegrations(prev => prev.filter(i => i.provider !== provider));
    } catch { /* empty */ }
  };

  if (loading) return <div style={{ color: '#94a3b8', padding: 40 }}>Loading integrations…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <div>
        <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 4 }}>Integrations</h1>
        <p style={{ color: '#94a3b8', fontSize: 14 }}>Connect external platforms to enrich your career intelligence.</p>
      </div>

      {error && <div style={{ background: 'rgba(239,68,68,0.1)', border: '1px solid rgba(239,68,68,0.3)', borderRadius: 10, padding: '10px 14px', color: '#f87171', fontSize: 13 }}>{error}</div>}

      {/* GitHub */}
      <div className="glass-card" style={{ padding: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
            <div style={{ width: 40, height: 40, borderRadius: 10, background: '#171717', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 22 }}>⊙</div>
            <div>
              <div style={{ fontSize: 16, fontWeight: 600 }}>GitHub</div>
              <div style={{ fontSize: 12, color: '#64748b' }}>
                {github?.isConnected ? `Connected as @${github.username}` : 'Not connected'}
              </div>
            </div>
          </div>
          {github?.isConnected && (
            <button onClick={() => disconnect('github')} style={{ background: 'rgba(239,68,68,0.08)', border: '1px solid rgba(239,68,68,0.2)', borderRadius: 8, padding: '6px 14px', color: '#f87171', cursor: 'pointer', fontSize: 12 }}>
              Disconnect
            </button>
          )}
        </div>

        {!github?.isConnected ? (
          <div style={{ display: 'flex', gap: 10 }}>
            <input className="input-field" placeholder="Enter GitHub username" value={githubUser} onChange={e => setGithubUser(e.target.value)} id="github-username" />
            <button className="btn-primary" onClick={connectGitHub} disabled={connecting} id="connect-github">
              {connecting ? 'Connecting…' : 'Connect'}
            </button>
          </div>
        ) : github?.data ? (
          <div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(120px, 1fr))', gap: 12, marginBottom: 16 }}>
              <MiniStat label="Repositories" value={github.data.publicRepoCount} />
              <MiniStat label="Total Stars" value={github.data.totalStars} />
              <MiniStat label="Total Forks" value={github.data.totalForks} />
            </div>

            {github.data.topLanguages?.length > 0 && (
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>Top Languages</div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {github.data.topLanguages.map((l: any) => (
                    <span key={l.language} className="badge badge-primary">{l.language} ({l.percentage}%)</span>
                  ))}
                </div>
              </div>
            )}

            {github.data.recentRepositories?.length > 0 && (
              <div>
                <div style={{ fontSize: 13, fontWeight: 600, color: '#94a3b8', marginBottom: 8 }}>Recent Repositories</div>
                {github.data.recentRepositories.slice(0, 5).map((repo: any) => (
                  <div key={repo.name} style={{ padding: '8px 0', borderBottom: '1px solid rgba(51,65,85,0.3)' }}>
                    <a href={repo.html_url} target="_blank" rel="noopener noreferrer" style={{ color: '#60a5fa', fontSize: 13, textDecoration: 'none' }}>
                      {repo.name}
                    </a>
                    <span style={{ marginLeft: 8, color: '#64748b', fontSize: 11 }}>
                      {repo.language} · ★{repo.stargazers_count}
                    </span>
                    {repo.description && <p style={{ color: '#64748b', fontSize: 12, marginTop: 2 }}>{repo.description}</p>}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : null}
      </div>

      {/* LinkedIn (Honest Unavailable State) */}
      <div className="glass-card" style={{ padding: 24 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 12 }}>
          <div style={{ width: 40, height: 40, borderRadius: 10, background: '#0a66c2', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 18, color: '#fff', fontWeight: 700 }}>in</div>
          <div>
            <div style={{ fontSize: 16, fontWeight: 600 }}>LinkedIn</div>
            <div style={{ fontSize: 12, color: '#64748b' }}>Integration unavailable</div>
          </div>
        </div>
        <p style={{ color: '#94a3b8', fontSize: 13, lineHeight: 1.6 }}>
          LinkedIn data cannot be fetched without a legitimate API integration. LinkedIn's API requires OAuth2 application approval.
          This integration requires an approved LinkedIn Developer application with the <code style={{ color: '#60a5fa' }}>r_liteprofile</code> scope.
        </p>
      </div>
    </div>
  );
}

function MiniStat({ label, value }: { label: string; value: number | string }) {
  return (
    <div style={{ background: 'rgba(15,23,42,0.5)', border: '1px solid rgba(51,65,85,0.3)', borderRadius: 10, padding: '12px 14px', textAlign: 'center' }}>
      <div style={{ fontSize: 22, fontWeight: 700, color: '#f1f5f9' }}>{value}</div>
      <div style={{ fontSize: 11, color: '#94a3b8' }}>{label}</div>
    </div>
  );
}
