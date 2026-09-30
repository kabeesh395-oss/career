import { useState, FormEvent } from 'react';
import { useAuth } from '../../context/AuthContext';

export default function AuthPage() {
  const { login, signup } = useAuth();
  const [isLogin, setIsLogin] = useState(true);
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [fullName, setFullName] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e: FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      if (isLogin) {
        await login(email, password);
      } else {
        await signup(email, password, fullName);
      }
    } catch (err: any) {
      setError(err.message || 'Authentication failed.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'hsl(var(--bg-base))',
      position: 'relative',
      overflow: 'hidden'
    }}>
      {/* Ambient glow orbs using theme accents */}
      <div style={{
        position: 'absolute', top: '15%', left: '20%', width: 400, height: 400,
        background: 'radial-gradient(circle, hsla(var(--primary), 0.12) 0%, transparent 70%)',
        borderRadius: '50%', filter: 'blur(60px)', pointerEvents: 'none'
      }} />
      <div style={{
        position: 'absolute', bottom: '10%', right: '15%', width: 350, height: 350,
        background: 'radial-gradient(circle, hsla(var(--accent-purple), 0.10) 0%, transparent 70%)',
        borderRadius: '50%', filter: 'blur(60px)', pointerEvents: 'none'
      }} />

      <div className="glass-panel animate-fade-in" style={{
        width: '100%', maxWidth: 440, padding: '40px 36px', position: 'relative', zIndex: 1
      }}>
        {/* Logo */}
        <div style={{ textAlign: 'center', marginBottom: 32 }}>
          <div style={{
            display: 'inline-flex', alignItems: 'center', gap: 10, marginBottom: 8
          }}>
            <div style={{
              width: 42, height: 42, borderRadius: 12,
              background: 'linear-gradient(135deg, hsl(var(--primary)), hsl(var(--accent-cyan)))',
              display: 'flex', alignItems: 'center', justifyContent: 'center',
              fontSize: 20, fontWeight: 800, color: '#fff',
              boxShadow: '0 0 20px hsla(var(--primary), 0.4)'
            }}>⬡</div>
            <span style={{ fontSize: 22, fontWeight: 700 }} className="gradient-text">
              Career Hub
            </span>
          </div>
          <p style={{ color: 'hsl(var(--text-secondary))', fontSize: 14 }}>
            {isLogin ? 'Welcome back. Sign in to your developer workspace.' : 'Create your engineer portfolio & career workspace.'}
          </p>
        </div>

        {error && (
          <div style={{
            background: 'hsla(var(--danger), 0.12)',
            border: '1px solid hsla(var(--danger), 0.35)',
            borderRadius: 10,
            padding: '10px 14px',
            marginBottom: 20,
            color: 'hsl(var(--danger))',
            fontSize: 13
          }}>
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {!isLogin && (
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: 'hsl(var(--text-secondary))', marginBottom: 6 }}>
                Full Name
              </label>
              <input
                className="input-field"
                type="text"
                placeholder="Alex Rivera"
                value={fullName}
                onChange={e => setFullName(e.target.value)}
                required={!isLogin}
                id="auth-fullname"
              />
            </div>
          )}
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: 'hsl(var(--text-secondary))', marginBottom: 6 }}>
              Email Address
            </label>
            <input
              className="input-field"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              id="auth-email"
            />
          </div>
          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: 'hsl(var(--text-secondary))', marginBottom: 6 }}>
              Password
            </label>
            <input
              className="input-field"
              type="password"
              placeholder="••••••••"
              value={password}
              onChange={e => setPassword(e.target.value)}
              required
              minLength={6}
              id="auth-password"
            />
          </div>

          <button
            className="btn-primary"
            type="submit"
            disabled={loading}
            style={{ width: '100%', marginTop: 8, padding: '12px 20px', fontSize: 15, opacity: loading ? 0.7 : 1 }}
            id="auth-submit"
          >
            {loading ? 'Processing…' : (isLogin ? 'Sign In' : 'Create Account')}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: 24 }}>
          <span style={{ color: 'hsl(var(--text-muted))', fontSize: 13 }}>
            {isLogin ? "Don't have an account?" : 'Already have an account?'}
          </span>
          <button
            onClick={() => { setIsLogin(!isLogin); setError(''); }}
            style={{
              background: 'none',
              border: 'none',
              color: 'hsl(var(--primary))',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 13,
              marginLeft: 6
            }}
            id="auth-toggle"
          >
            {isLogin ? 'Sign Up' : 'Sign In'}
          </button>
        </div>
      </div>
    </div>
  );
}
