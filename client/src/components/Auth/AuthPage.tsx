import { useState, FormEvent } from 'react';
import { useAuth } from '../../context/AuthContext';
import PatternWaves from '../Common/PatternWaves';
import { CareerHubLogo } from '../Common/CareerHubLogo';
import { ShieldCheck, Sparkles, Cpu, ArrowRight } from 'lucide-react';

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

  const handleQuickFill = () => {
    setEmail('demo@careerhub.dev');
    setPassword('DemoCareer2026!');
    if (!isLogin) {
      setFullName('Jordan Vance');
    }
  };

  return (
    <div style={{
      minHeight: '100vh',
      width: '100%',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      background: 'hsl(var(--bg-base))',
      position: 'relative',
      overflow: 'hidden',
      padding: '24px 16px'
    }}>
      {/* Interactive WebGL PatternWaves Ambient Canvas */}
      <div style={{
        position: 'absolute',
        inset: 0,
        zIndex: 0,
        opacity: 0.32,
        pointerEvents: 'auto'
      }}>
        <PatternWaves
          preset="silk"
          color="#3b82f6"
          backgroundColor="transparent"
          fade="edges"
          fadeSize={0.65}
          interactive={true}
          cursorSize={60}
          cursorStrength={0.7}
          speed={0.25}
          markSize={0.9}
        />
      </div>

      {/* Ambient background gradient accents */}
      <div style={{
        position: 'absolute',
        top: '12%',
        left: '18%',
        width: 480,
        height: 480,
        background: 'radial-gradient(circle, rgba(59, 130, 246, 0.12) 0%, transparent 70%)',
        borderRadius: '50%',
        filter: 'blur(80px)',
        pointerEvents: 'none'
      }} />
      <div style={{
        position: 'absolute',
        bottom: '10%',
        right: '15%',
        width: 420,
        height: 420,
        background: 'radial-gradient(circle, rgba(16, 185, 129, 0.08) 0%, transparent 70%)',
        borderRadius: '50%',
        filter: 'blur(80px)',
        pointerEvents: 'none'
      }} />

      {/* Main Glass Card */}
      <div
        className="glass-panel animate-fade-in"
        style={{
          width: '100%',
          maxWidth: 460,
          padding: '36px 32px',
          position: 'relative',
          zIndex: 10,
          borderRadius: 20,
          background: 'rgba(15, 23, 42, 0.85)',
          backdropFilter: 'blur(24px)',
          WebkitBackdropFilter: 'blur(24px)',
          border: '1px solid rgba(59, 130, 246, 0.22)',
          boxShadow: '0 20px 48px -12px rgba(0, 0, 0, 0.6), 0 0 0 1px rgba(255, 255, 255, 0.04)'
        }}
      >
        {/* Brand Header */}
        <div style={{ textAlign: 'center', marginBottom: 28 }}>
          <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 12 }}>
            <CareerHubLogo size={42} showSubtitle={true} />
          </div>
          <p style={{ color: 'hsl(var(--text-secondary))', fontSize: 13.5, lineHeight: 1.5, margin: '8px 0 0' }}>
            {isLogin
              ? 'Sign in to access your calibrated career roadmap, ATS resumes, and engineering velocity.'
              : 'Create your production portfolio workspace and start your career roadmap.'}
          </p>
        </div>

        {/* Feature Highlights Pills */}
        <div style={{
          display: 'flex',
          justifyContent: 'center',
          gap: 6,
          flexWrap: 'wrap',
          marginBottom: 22
        }}>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '3px 8px',
            borderRadius: 6,
            background: 'rgba(59, 130, 246, 0.12)',
            border: '1px solid rgba(59, 130, 246, 0.25)',
            color: '#93c5fd',
            fontSize: 11,
            fontWeight: 500
          }}>
            <Sparkles size={11} /> ATS Scanner
          </span>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '3px 8px',
            borderRadius: 6,
            background: 'rgba(16, 185, 129, 0.12)',
            border: '1px solid rgba(16, 185, 129, 0.25)',
            color: '#6ee7b7',
            fontSize: 11,
            fontWeight: 500
          }}>
            <ShieldCheck size={11} /> Phased Roadmaps
          </span>
          <span style={{
            display: 'inline-flex',
            alignItems: 'center',
            gap: 4,
            padding: '3px 8px',
            borderRadius: 6,
            background: 'rgba(168, 85, 247, 0.12)',
            border: '1px solid rgba(168, 85, 247, 0.25)',
            color: '#d8b4fe',
            fontSize: 11,
            fontWeight: 500
          }}>
            <Cpu size={11} /> Edge AI
          </span>
        </div>

        {error && (
          <div style={{
            background: 'rgba(239, 68, 68, 0.12)',
            border: '1px solid rgba(239, 68, 68, 0.35)',
            borderRadius: 10,
            padding: '11px 14px',
            marginBottom: 20,
            color: '#f87171',
            fontSize: 13,
            display: 'flex',
            alignItems: 'center',
            gap: 8
          }}>
            <span>⚠️</span>
            <span>{error}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: 16 }}>
          {!isLogin && (
            <div>
              <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>
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
                style={{
                  width: '100%',
                  padding: '10px 14px',
                  borderRadius: 8,
                  background: 'rgba(15, 23, 42, 0.6)',
                  border: '1px solid rgba(51, 65, 85, 0.8)',
                  color: '#f8fafc',
                  fontSize: 14,
                  boxSizing: 'border-box'
                }}
              />
            </div>
          )}

          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 6 }}>
              <label style={{ fontSize: 13, fontWeight: 500, color: '#94a3b8' }}>
                Email Address
              </label>
              <button
                type="button"
                onClick={handleQuickFill}
                style={{
                  background: 'none',
                  border: 'none',
                  color: '#60a5fa',
                  fontSize: 11.5,
                  cursor: 'pointer',
                  padding: 0,
                  textDecoration: 'underline'
                }}
              >
                Quick fill sample
              </button>
            </div>
            <input
              className="input-field"
              type="email"
              placeholder="you@example.com"
              value={email}
              onChange={e => setEmail(e.target.value)}
              required
              id="auth-email"
              style={{
                width: '100%',
                padding: '10px 14px',
                borderRadius: 8,
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid rgba(51, 65, 85, 0.8)',
                color: '#f8fafc',
                fontSize: 14,
                boxSizing: 'border-box'
              }}
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: 13, fontWeight: 500, color: '#94a3b8', marginBottom: 6 }}>
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
              style={{
                width: '100%',
                padding: '10px 14px',
                borderRadius: 8,
                background: 'rgba(15, 23, 42, 0.6)',
                border: '1px solid rgba(51, 65, 85, 0.8)',
                color: '#f8fafc',
                fontSize: 14,
                boxSizing: 'border-box'
              }}
            />
          </div>

          <button
            className="btn-primary"
            type="submit"
            disabled={loading}
            style={{
              width: '100%',
              marginTop: 6,
              padding: '12px 20px',
              fontSize: 14.5,
              fontWeight: 600,
              opacity: loading ? 0.7 : 1,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 8,
              cursor: loading ? 'not-allowed' : 'pointer'
            }}
            id="auth-submit"
          >
            {loading ? 'Processing…' : (isLogin ? 'Sign In' : 'Create Workspace Account')}
            {!loading && <ArrowRight size={15} />}
          </button>
        </form>

        <div style={{ textAlign: 'center', marginTop: 22, paddingTop: 16, borderTop: '1px solid rgba(51, 65, 85, 0.4)' }}>
          <span style={{ color: '#94a3b8', fontSize: 13 }}>
            {isLogin ? "Don't have an account?" : 'Already have an account?'}
          </span>
          <button
            onClick={() => { setIsLogin(!isLogin); setError(''); }}
            style={{
              background: 'none',
              border: 'none',
              color: '#3b82f6',
              cursor: 'pointer',
              fontWeight: 600,
              fontSize: 13,
              marginLeft: 6
            }}
            id="auth-toggle"
          >
            {isLogin ? 'Create Account' : 'Sign In'}
          </button>
        </div>
      </div>
    </div>
  );
}
