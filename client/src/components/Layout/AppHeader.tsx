import { useState } from 'react';
import { motion, Variants } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { Search, Bell, RefreshCw, Command } from 'lucide-react';
import { CareerHubLogo } from '../Common/CareerHubLogo';

interface AppHeaderProps {
  activePage?: string;
  onNavigate: (page: string) => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ onNavigate }) => {
  const { user } = useAuth();
  const [isSyncing, setIsSyncing] = useState(false);
  const [showNotification, setShowNotification] = useState(false);

  const handleSync = () => {
    setIsSyncing(true);
    setTimeout(() => setIsSyncing(false), 1200);
  };

  // Animation variants for container orchestration
  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.08,
        delayChildren: 0.1,
      },
    },
  };

  const itemVariants: Variants = {
    hidden: { opacity: 0, y: -10 },
    visible: {
      opacity: 1,
      y: 0,
      transition: {
        type: 'spring',
        stiffness: 300,
        damping: 24,
      },
    },
  };

  return (
    <motion.header
      variants={containerVariants}
      initial="hidden"
      animate="visible"
      style={{
        position: 'sticky',
        top: 0,
        zIndex: 40,
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        padding: '14px 28px',
        marginBottom: 24,
        borderRadius: 16,
        background: 'rgba(15, 23, 42, 0.75)',
        backdropFilter: 'blur(16px)',
        WebkitBackdropFilter: 'blur(16px)',
        border: '1px solid rgba(59, 130, 246, 0.18)',
        boxShadow: '0 8px 32px -4px rgba(0, 0, 0, 0.35), 0 0 0 1px rgba(255, 255, 255, 0.04)',
      }}
    >
      {/* Left: Animated Brand & Geometric Node */}
      <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={() => onNavigate('dashboard')}>
        <CareerHubLogo size={40} showSubtitle={true} />
      </div>

      {/* Right: Quick Command Search, Live Stats & Action Controls */}
      <motion.div
        variants={itemVariants}
        style={{ display: 'flex', alignItems: 'center', gap: 12 }}
      >
        {/* Search Bar / Quick Command Prompt */}
        <motion.div
          whileHover={{ borderColor: 'rgba(59, 130, 246, 0.5)', background: 'rgba(15, 23, 42, 0.9)' }}
          onClick={() => onNavigate('career')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 8,
            padding: '7px 14px',
            borderRadius: 10,
            background: 'rgba(15, 23, 42, 0.6)',
            border: '1px solid rgba(51, 65, 85, 0.6)',
            color: '#94a3b8',
            fontSize: 12.5,
            cursor: 'pointer',
            transition: 'all 0.2s ease',
          }}
        >
          <Search size={14} color="#64748b" />
          <span>Search skills, roadmap, projects...</span>
          <span
            style={{
              display: 'inline-flex',
              alignItems: 'center',
              gap: 2,
              padding: '2px 5px',
              borderRadius: 4,
              background: 'rgba(51, 65, 85, 0.5)',
              fontSize: 10,
              fontWeight: 600,
              color: '#cbd5e1',
            }}
          >
            <Command size={10} /> K
          </span>
        </motion.div>

        {/* Sync Trigger Button */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={handleSync}
          title="Force Sync State"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 36,
            height: 36,
            borderRadius: 10,
            background: 'rgba(30, 41, 59, 0.6)',
            border: '1px solid rgba(51, 65, 85, 0.6)',
            color: '#94a3b8',
            cursor: 'pointer',
          }}
        >
          <motion.div
            animate={isSyncing ? { rotate: 360 } : { rotate: 0 }}
            transition={isSyncing ? { repeat: Infinity, duration: 0.8, ease: 'linear' } : { duration: 0.2 }}
          >
            <RefreshCw size={15} color={isSyncing ? '#38bdf8' : '#94a3b8'} />
          </motion.div>
        </motion.button>

        {/* Notifications Toggle */}
        <motion.button
          whileHover={{ scale: 1.05 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => setShowNotification(!showNotification)}
          style={{
            position: 'relative',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            width: 36,
            height: 36,
            borderRadius: 10,
            background: 'rgba(30, 41, 59, 0.6)',
            border: '1px solid rgba(51, 65, 85, 0.6)',
            color: '#94a3b8',
            cursor: 'pointer',
          }}
        >
          <Bell size={15} />
          <span
            style={{
              position: 'absolute',
              top: 8,
              right: 8,
              width: 6,
              height: 6,
              borderRadius: '50%',
              background: '#38bdf8',
              boxShadow: '0 0 6px #38bdf8',
            }}
          />
        </motion.button>

        {/* User Pill / Profile Shortcut */}
        <motion.div
          whileHover={{ scale: 1.02 }}
          whileTap={{ scale: 0.98 }}
          onClick={() => onNavigate('profile')}
          style={{
            display: 'flex',
            alignItems: 'center',
            gap: 10,
            padding: '5px 12px 5px 6px',
            borderRadius: 10,
            background: 'rgba(30, 41, 59, 0.7)',
            border: '1px solid rgba(59, 130, 246, 0.25)',
            cursor: 'pointer',
          }}
        >
          <div
            style={{
              width: 28,
              height: 28,
              borderRadius: 8,
              background: 'linear-gradient(135deg, #10b981, #059669)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 12,
              fontWeight: 700,
              color: '#ffffff',
            }}
          >
            {user?.fullName?.charAt(0)?.toUpperCase() || 'A'}
          </div>
          <div style={{ display: 'flex', flexDirection: 'column' }}>
            <span style={{ fontSize: 12, fontWeight: 600, color: '#f1f5f9', lineHeight: 1.2 }}>
              {user?.fullName || 'Alex Chen'}
            </span>
            <span style={{ fontSize: 10, color: '#94a3b8', lineHeight: 1.1 }}>
              Staff Engineer
            </span>
          </div>
        </motion.div>
      </motion.div>
    </motion.header>
  );
};

export default AppHeader;
