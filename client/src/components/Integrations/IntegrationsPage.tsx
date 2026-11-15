import { useEffect, useState } from 'react';
import { api } from '../../api/client';

export default function IntegrationsPage() {
  const [integrations, setIntegrations] = useState<any[]>([]);
  const [githubUser, setGithubUser] = useState('');
  const [connecting, setConnecting] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadIntegrations = async () => {
    setError('');
    try {
      const data = await api('/integrations');
      setIntegrations(data.integrations || []);
    } catch (err: any) {
      setError(err?.message || 'Failed to load integrations.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadIntegrations();
  }, []);

  const github = integrations.find(i => i.provider === 'github');

  const lookupGitHub = async () => {
    if (!githubUser.trim()) return;
    setConnecting(true);
    setError('');
    try {
      const data = await api('/integrations/github/connect', {
        method: 'POST',
        body: JSON.stringify({ username: githubUser.trim() }),
      });
      setIntegrations(prev => {
        const filtered = prev.filter(i => i.provider !== 'github');
        return [...filtered, data.integration];
      });
      setGithubUser('');
    } catch (err: any) {
      setError(err?.message || 'Failed to lookup GitHub public profile.');
    } finally {
      setConnecting(false);
    }
  };

  const disconnect = async (provider: string) => {
    setError('');
    try {
      await api(`/integrations/${provider}`, { method: 'DELETE' });
      setIntegrations(prev => prev.filter(i => i.provider !== provider));
    } catch (err: any) {
      setError(err?.message || 'Failed to disconnect integration.');
    }
  };

  if (loading) return <div style={{ color: 'hsl(var(--text-secondary))', padding: 32 }}>Loading integrations…</div>;

  return (
    <div className="animate-fade-in" style={{ display: 'flex', flexDirection: 'column', gap: 18 }}>
      <div>
        <h2 style={{ fontSize: 18, fontWeight: 700, margin: '0 0 2px' }}>Integrations & External Data</h2>
        <p style={{ color: 'hsl(var(--text-secondary))', fontSize: 13, margin: 0 }}>
          Fetch public GitHub repository metrics. External providers without configured credentials remain disabled.
        </p>
      </div>

      {error && (
        <div
          style={{
            background: 'hsla(var(--danger), 0.12)',
            border: '1px solid hsla(var(--danger), 0.35)',
            borderRadius: 10,
            padding: '10px 14px',
            color: 'hsl(var(--danger))',
            fontSize: 13,
          }}
        >
          {error}
        </div>
      )}

      {/* GitHub Public Profile Lookup */}
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12, marginBottom: 14 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: 12, minWidth: 0 }}>
            <div
              style={{
                width: 40,
                height: 40,
                borderRadius: 10,
                background: '#171717',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                fontSize: 20,
                color: '#fff',
                flexShrink: 0,
              }}
            >
              ⊙
            </div>
            <div style={{ minWidth: 0 }}>
              <div style={{ fontSize: 15.5, fontWeight: 600 }}>Public GitHub Profile Lookup</div>
              <div style={{ fontSize: 12, color: 'hsl(var(--text-muted))' }}>
                {github?.isConnected
                  ? `Viewing public profile @${github.username}`
                  : 'No public GitHub profile linked'}
              </div>
            </div>
          </div>
          {github?.isConnected && (
            <button
              type="button"
              onClick={() => disconnect('github')}
              style={{
                background: 'hsla(var(--danger), 0.1)',
                border: '1px solid hsla(var(--danger), 0.25)',
                borderRadius: 8,
                padding: '7px 14px',
                minHeight: 38,
                color: 'hsl(var(--danger))',
                cursor: 'pointer',
                fontSize: 12,
              }}
            >
              Clear Profile
            </button>
          )}
        </div>

        <p style={{ color: 'hsl(var(--text-secondary))', fontSize: 12.5, marginBottom: 14, lineHeight: 1.5 }}>
          Retrieves publicly accessible repositories, stars, and language counts from GitHub without claiming OAuth account ownership.
        </p>

        {!github?.isConnected ? (
          <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap' }}>
            <input
              className="input-field"
              placeholder="GitHub username (e.g. torvalds)"
              value={githubUser}
              onChange={e => setGithubUser(e.target.value)}
              id="github-username"
              style={{ flex: '1 1 200px' }}
            />
            <button
              type="button"
              className="btn-primary"
              onClick={lookupGitHub}
              disabled={connecting || !githubUser.trim()}
              id="connect-github"
            >
              {connecting ? 'Fetching…' : 'Lookup Profile'}
            </button>
          </div>
        ) : github?.data ? (
          <div>
            <div
              style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(auto-fit, minmax(min(100%, 110px), 1fr))',
                gap: 10,
                marginBottom: 16,
              }}
            >
              <MiniStat label="Public Repos" value={github.data.publicRepoCount ?? 0} />
              <MiniStat label="Total Stars" value={github.data.totalStars ?? 0} />
              <MiniStat label="Total Forks" value={github.data.totalForks ?? 0} />
            </div>

            {github.data.topLanguages?.length > 0 && (
              <div style={{ marginBottom: 16 }}>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'hsl(var(--text-secondary))', marginBottom: 8 }}>
                  Top Languages Detected
                </div>
                <div style={{ display: 'flex', flexWrap: 'wrap', gap: 6 }}>
                  {github.data.topLanguages.map((l: any) => (
                    <span key={l.language} className="badge badge-primary">
                      {l.language} ({l.percentage}%)
                    </span>
                  ))}
                </div>
              </div>
            )}

            {github.data.recentRepositories?.length > 0 && (
              <div>
                <div style={{ fontSize: 13, fontWeight: 600, color: 'hsl(var(--text-secondary))', marginBottom: 8 }}>
                  Recent Public Repositories
                </div>
                {github.data.recentRepositories.slice(0, 5).map((repo: any) => (
                  <div key={repo.name} style={{ padding: '8px 0', borderBottom: '1px solid hsla(var(--border-subtle), 0.5)' }}>
                    <a
                      href={repo.html_url}
                      target="_blank"
                      rel="noopener noreferrer"
                      style={{ color: 'hsl(var(--primary))', fontSize: 13, textDecoration: 'none', fontWeight: 500 }}
                    >
                      {repo.name}
                    </a>
                    <span style={{ marginLeft: 8, color: 'hsl(var(--text-muted))', fontSize: 11 }}>
                      {repo.language || 'Unknown'} · ★{repo.stargazers_count}
                    </span>
                    {repo.description && (
                      <p style={{ color: 'hsl(var(--text-muted))', fontSize: 12, marginTop: 2 }}>{repo.description}</p>
                    )}
                  </div>
                ))}
              </div>
            )}
          </div>
        ) : null}
      </div>

      {/* LinkedIn Honest Unavailable State */}
      <div className="glass-card" style={{ padding: 20 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, marginBottom: 10 }}>
          <div
            style={{
              width: 40,
              height: 40,
              borderRadius: 10,
              background: '#0a66c2',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 18,
              color: '#fff',
              fontWeight: 700,
              flexShrink: 0,
            }}
          >
            in
          </div>
          <div>
            <div style={{ fontSize: 15.5, fontWeight: 600 }}>LinkedIn Integration</div>
            <div style={{ fontSize: 12, color: 'hsl(var(--text-muted))' }}>Not Configured / Unavailable</div>
          </div>
        </div>
        <p style={{ color: 'hsl(var(--text-secondary))', fontSize: 13, lineHeight: 1.55, margin: 0 }}>
          LinkedIn integration is unavailable because official OAuth2 partner credentials are not configured. CareerHub never fabricates external social metrics.
        </p>
      </div>
    </div>
  );
}

function MiniStat({ label, value }: { label: string; value: number | string }) {
  return (
    <div
      style={{
        background: 'hsla(var(--bg-card), 0.7)',
        border: '1px solid hsla(var(--border-subtle), 0.6)',
        borderRadius: 10,
        padding: '12px 14px',
        textAlign: 'center',
      }}
    >
      <div style={{ fontSize: 20, fontWeight: 700, color: 'hsl(var(--text-primary))' }}>{value}</div>
      <div style={{ fontSize: 11, color: 'hsl(var(--text-muted))' }}>{label}</div>
    </div>
  );
}
