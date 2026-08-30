import { motion } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';

interface NavItem {
  id: string;
  label: string;
  icon: string;
}

const navItems: NavItem[] = [
  { id: 'dashboard', label: 'Dashboard', icon: '⬡' },
  { id: 'career', label: 'Career Analysis', icon: '◈' },
  { id: 'roadmap', label: 'Roadmap', icon: '◎' },
  { id: 'resume', label: 'Resume', icon: '◉' },
  { id: 'projects', label: 'Projects', icon: '▣' },
  { id: 'interview', label: 'Interview', icon: '◆' },
  { id: 'learning', label: 'Learning', icon: '◇' },
  { id: 'integrations', label: 'Integrations', icon: '⬢' },
  { id: 'analytics', label: 'Analytics', icon: '▦' },
  { id: 'profile', label: 'Profile', icon: '○' },
];

interface Props {
  activePage: string;
  onNavigate: (page: string) => void;
}

export default function Sidebar({ activePage, onNavigate }: Props) {
  const { user, logout } = useAuth();

  return (
    <motion.aside
      initial={{ x: -240, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ type: 'spring', stiffness: 220, damping: 24 }}
      style={{
        width: 240,
        minHeight: '100vh',
        background: 'rgba(11, 15, 25, 0.96)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderRight: '1px solid rgba(51, 65, 85, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        padding: '20px 12px',
        position: 'fixed',
        left: 0,
        top: 0,
        zIndex: 50,
      }}
    >
      {/* Brand with Framer Motion Reveal */}
      <motion.div
        initial={{ opacity: 0, y: -12 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.15, duration: 0.4 }}
        onClick={() => onNavigate('dashboard')}
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          padding: '8px 12px',
          marginBottom: 26,
          cursor: 'pointer',
        }}
      >
        <motion.div
          whileHover={{ scale: 1.1, rotate: 10 }}
          whileTap={{ scale: 0.95 }}
          style={{
            width: 36,
            height: 36,
            borderRadius: 10,
            background: 'linear-gradient(135deg, #3b82f6, #06b6d4)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            fontSize: 16,
            fontWeight: 800,
            color: '#fff',
            boxShadow: '0 0 16px rgba(59, 130, 246, 0.4)',
          }}
        >
          ⬡
        </motion.div>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <span style={{ fontSize: 16, fontWeight: 700, letterSpacing: '-0.02em' }} className="gradient-text">
            Career Hub
          </span>
          <span style={{ fontSize: 9.5, color: '#64748b', fontWeight: 600, letterSpacing: '0.04em', textTransform: 'uppercase' }}>
            Developer OS
          </span>
        </div>
      </motion.div>

      {/* Navigation list */}
      <nav style={{ flex: 1, display: 'flex', flexDirection: 'column', gap: 4 }}>
        {navItems.map((item, index) => {
          const isActive = activePage === item.id;
          return (
            <motion.button
              key={item.id}
              initial={{ opacity: 0, x: -10 }}
              animate={{ opacity: 1, x: 0 }}
              transition={{ delay: 0.1 + index * 0.03, duration: 0.3 }}
              whileHover={{ x: 3 }}
              whileTap={{ scale: 0.98 }}
              onClick={() => onNavigate(item.id)}
              id={`nav-${item.id}`}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: 10,
                padding: '10px 14px',
                borderRadius: 10,
                background: isActive
                  ? 'linear-gradient(135deg, rgba(59,130,246,0.18), rgba(6,182,212,0.12))'
                  : 'transparent',
                border: isActive
                  ? '1px solid rgba(59,130,246,0.35)'
                  : '1px solid transparent',
                color: isActive ? '#f8fafc' : '#94a3b8',
                cursor: 'pointer',
                fontSize: 13.5,
                fontWeight: isActive ? 600 : 500,
                fontFamily: 'inherit',
                textAlign: 'left',
                width: '100%',
              }}
            >
              <span
                style={{
                  fontSize: 15,
                  width: 20,
                  textAlign: 'center',
                  color: isActive ? '#38bdf8' : '#64748b',
                }}
              >
                {item.icon}
              </span>
              {item.label}
            </motion.button>
          );
        })}
      </nav>

      {/* User Footer */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.5 }}
        style={{
          borderTop: '1px solid rgba(51, 65, 85, 0.5)',
          paddingTop: 16,
          marginTop: 12,
        }}
      >
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            padding: '8px 12px',
            marginBottom: 8,
          }}
        >
          <div
            style={{
              width: 32,
              height: 32,
              borderRadius: 8,
              background: 'linear-gradient(135deg, #10b981, #059669)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 13,
              fontWeight: 700,
              color: '#fff',
            }}
          >
            {user?.fullName?.charAt(0)?.toUpperCase() || '?'}
          </div>
          <div style={{ overflow: 'hidden' }}>
            <div
              style={{
                fontSize: 13,
                fontWeight: 600,
                color: '#e2e8f0',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
              }}
            >
              {user?.fullName || 'User'}
            </div>
            <div
              style={{
                fontSize: 11,
                color: '#64748b',
                whiteSpace: 'nowrap',
                overflow: 'hidden',
                textOverflow: 'ellipsis',
              }}
            >
              {user?.email}
            </div>
          </div>
        </div>
        <motion.button
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.97 }}
          onClick={logout}
          id="nav-logout"
          style={{
            width: '100%',
            padding: '8px 14px',
            borderRadius: 8,
            background: 'rgba(239, 68, 68, 0.08)',
            border: '1px solid rgba(239, 68, 68, 0.2)',
            color: '#f87171',
            cursor: 'pointer',
            fontSize: 13,
            fontWeight: 500,
            fontFamily: 'inherit',
          }}
        >
          Sign Out
        </motion.button>
      </motion.div>
    </motion.aside>
  );
}
