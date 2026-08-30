import React, { useState } from 'react';
import { motion, AnimatePresence } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { Search, Bell, Sparkles, RefreshCw, Command, CheckCircle2, ShieldCheck, Layers } from 'lucide-react';

interface AppHeaderProps {
  activePage: string;
  onNavigate: (page: string) => void;
}

export const AppHeader: React.FC<AppHeaderProps> = ({ activePage, onNavigate }) => {
  const { user } = useAuth();
  const [isSyncing, setIsSyncing] = useState(false);
  const [showNotification, setShowNotification] = useState(false);

  const handleSync = () => {
    setIsSyncing(true);
    setTimeout(() => setIsSyncing(false), 1200);
  };

  // Animation variants for container orchestration
  const containerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.08,
        delayChildren: 0.1,
      },
    },
  };

  const iconVariants = {
    hidden: { scale: 0, rotate: -25, opacity: 0 },
    visible: {
      scale: 1,
      rotate: 0,
      opacity: 1,
      transition: {
        type: 'spring',
        stiffness: 260,
        damping: 18,
      },
    },
  };

  const itemVariants = {
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

  const letterContainerVariants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: {
        staggerChildren: 0.035,
        delayChildren: 0.2,
      },
    },
  };

  const letterVariants = {
    hidden: { opacity: 0, y: 8, filter: 'blur(4px)' },
    visible: {
      opacity: 1,
      y: 0,
      filter: 'blur(0px)',
      transition: {
        duration: 0.35,
        ease: [0.2, 0.65, 0.3, 0.9],
      },
    },
  };

  const titleWords = ['Career', 'Hub'];

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
      <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <motion.div
          variants={iconVariants}
          whileHover={{ scale: 1.06, rotate: 6 }}
          whileTap={{ scale: 0.95 }}
          onClick={() => onNavigate('dashboard')}
          style={{
            position: 'relative',
            width: 44,
            height: 44,
            borderRadius: 12,
            background: 'linear-gradient(135deg, #3b82f6 0%, #06b6d4 100%)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 0 24px rgba(59, 130, 246, 0.45)',
            cursor: 'pointer',
            overflow: 'hidden',
          }}
        >
          {/* Animated Background Shimmer */}
          <motion.div
            animate={{
              x: ['-100%', '200%'],
            }}
            transition={{
              repeat: Infinity,
              duration: 3.5,
              ease: 'easeInOut',
              delay: 1,
            }}
            style={{
              position: 'absolute',
              top: 0,
              left: 0,
              width: '60%',
              height: '100%',
              background: 'linear-gradient(90deg, transparent, rgba(255,255,255,0.35), transparent)',
              transform: 'skewX(-20deg)',
            }}
          />
          <span style={{ fontSize: 22, fontWeight: 900, color: '#ffffff', textShadow: '0 2px 8px rgba(0,0,0,0.3)' }}>
            ⬡
          </span>
        </motion.div>

        {/* Staggered Brand Typography */}
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <motion.div
            variants={letterContainerVariants}
            style={{ display: 'flex', alignItems: 'center', gap: 6 }}
          >
            {titleWords.map((word, wordIdx) => (
              <span key={wordIdx} style={{ display: 'inline-flex' }}>
                {word.split('').map((char, charIdx) => (
                  <motion.span
                    key={charIdx}
                    variants={letterVariants}
                    style={{
                      fontSize: 20,
                      fontWeight: 800,
                      letterSpacing: '-0.02em',
                      fontFamily: '"Plus Jakarta Sans", sans-serif',
                      color: wordIdx === 0 ? '#f8fafc' : '#38bdf8',
                      textShadow: wordIdx === 1 ? '0 0 16px rgba(56, 189, 248, 0.4)' : 'none',
                    }}
                  >
                    {char}
                  </motion.span>
                ))}
              </span>
            ))}

            <motion.div
              variants={itemVariants}
              style={{
                marginLeft: 6,
                padding: '2px 8px',
                borderRadius: 6,
                background: 'rgba(59, 130, 246, 0.15)',
                border: '1px solid rgba(59, 130, 246, 0.35)',
                fontSize: 10,
                fontWeight: 700,
                letterSpacing: '0.05em',
                color: '#60a5fa',
                textTransform: 'uppercase',
              }}
            >
              OS v2.4
            </motion.div>
          </motion.div>

          <motion.div
            variants={itemVariants}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              fontSize: 11,
              fontWeight: 500,
              color: '#94a3b8',
              letterSpacing: '0.02em',
            }}
          >
            <span>Engineering Portfolio & Career OS</span>
            <span style={{ color: '#475569' }}>•</span>
            <span style={{ display: 'inline-flex', alignItems: 'center', gap: 4, color: '#34d399', fontSize: 10.5 }}>
              <span
                style={{
                  width: 6,
                  height: 6,
                  borderRadius: '50%',
                  background: '#10b981',
                  boxShadow: '0 0 8px #10b981',
                }}
              />
              Synced Local / Cloud
            </span>
          </motion.div>
        </div>
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
