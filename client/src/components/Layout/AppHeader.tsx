import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence, Variants } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { Search, Bell, RefreshCw, Command, Check, AlertCircle, X, ChevronRight } from 'lucide-react';
import { CareerHubLogo } from '../Common/CareerHubLogo';

interface AppHeaderProps {
  activePage?: string;
  onNavigate: (page: string) => void;
  onToggleMobileMenu?: () => void;
}

interface CommandItem {
  id: string;
  title: string;
  category: string;
  description: string;
  page: string;
}

const COMMAND_PALETTE_ITEMS: CommandItem[] = [
  { id: 'home', title: 'Home', category: 'Primary', description: 'Career overview, readiness metrics, and recommended next action', page: 'home' },
  { id: 'home-analytics', title: 'Home · Telemetry & Audit', category: 'Home', description: 'Audit event stream and empirical progress metrics', page: 'home/analytics' },
  { id: 'resume', title: 'Resume', category: 'Primary', description: 'Upload resumes, ATS keyword scoring, and JD matching', page: 'resume' },
  { id: 'practice', title: 'Practice · Mock Interview', category: 'Primary', description: 'Technical mock interview rounds with rubric scoring', page: 'practice/interview' },
  { id: 'practice-learning', title: 'Practice · Learning Resources', category: 'Practice', description: 'Verified technical references and progress tracking', page: 'practice/learning' },
  { id: 'skills', title: 'Skills · My Skills', category: 'Primary', description: 'Manage verified proficiency levels across technical catalog', page: 'skills/my-skills' },
  { id: 'skills-gaps', title: 'Skills · Gap Analysis', category: 'Skills', description: 'Benchmark readiness and target role skill gaps', page: 'skills/gaps' },
  { id: 'skills-roadmap', title: 'Skills · Roadmap & Milestones', category: 'Skills', description: 'Phased curriculum and trackable completion checklist', page: 'skills/roadmap' },
  { id: 'skills-projects', title: 'Skills · Portfolio Projects', category: 'Skills', description: 'Track portfolio builds, repositories, and targeted skills', page: 'skills/projects' },
  { id: 'profile', title: 'Profile', category: 'Primary', description: 'Career background, target role, compensation, and presets', page: 'profile/general' },
  { id: 'profile-integrations', title: 'Profile · Integrations', category: 'Profile', description: 'Public GitHub lookup and external data settings', page: 'profile/integrations' },
  { id: 'profile-system', title: 'Profile · Theme & System AI', category: 'Profile', description: 'Appearance theme, Edge AI status, and account controls', page: 'profile/system' },
];

export const AppHeader: React.FC<AppHeaderProps> = ({ onNavigate }) => {
  const { user, profile, refreshProfile } = useAuth();

  // Real Sync State
  const [isSyncing, setIsSyncing] = useState(false);
  const [syncStatus, setSyncStatus] = useState<'idle' | 'success' | 'error'>('idle');
  const [syncErrorMessage, setSyncErrorMessage] = useState('');

  // Notifications State
  const [showNotification, setShowNotification] = useState(false);
  const [readNotifications, setReadNotifications] = useState<Set<string>>(new Set());
  const notifRef = useRef<HTMLDivElement>(null);

  // Command Palette State
  const [showCommandPalette, setShowCommandPalette] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedIndex, setSelectedIndex] = useState(0);
  const searchInputRef = useRef<HTMLInputElement>(null);

  // Genuine Refresh Operation
  const handleSync = async () => {
    if (isSyncing) return;
    setIsSyncing(true);
    setSyncStatus('idle');
    setSyncErrorMessage('');

    try {
      const ok = await refreshProfile();
      if (ok) {
        setSyncStatus('success');
        setTimeout(() => setSyncStatus('idle'), 3000);
      } else {
        setSyncStatus('error');
        setSyncErrorMessage('Failed to refresh data from server');
        setTimeout(() => setSyncStatus('idle'), 4000);
      }
    } catch (err: any) {
      setSyncStatus('error');
      setSyncErrorMessage(err?.message || 'Failed to refresh data from server');
      setTimeout(() => setSyncStatus('idle'), 4000);
    } finally {
      setIsSyncing(false);
    }
  };

  // Keyboard shortcut listener: Cmd/Ctrl + K and Escape
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === 'k') {
        e.preventDefault();
        setShowCommandPalette(prev => !prev);
      } else if (e.key === 'Escape') {
        setShowCommandPalette(false);
        setShowNotification(false);
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  useEffect(() => {
    if (showCommandPalette) {
      setSearchQuery('');
      setSelectedIndex(0);
      setTimeout(() => searchInputRef.current?.focus(), 50);
    }
  }, [showCommandPalette]);

  useEffect(() => {
    const handleClickOutside = (e: MouseEvent) => {
      if (notifRef.current && !notifRef.current.contains(e.target as Node)) {
        setShowNotification(false);
      }
    };
    if (showNotification) {
      document.addEventListener('mousedown', handleClickOutside);
    }
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [showNotification]);

  const filteredItems = COMMAND_PALETTE_ITEMS.filter(item => {
    const q = searchQuery.toLowerCase().trim();
    if (!q) return true;
    return (
      item.title.toLowerCase().includes(q) ||
      item.description.toLowerCase().includes(q) ||
      item.category.toLowerCase().includes(q)
    );
  });

  const handlePaletteSelect = (page: string) => {
    setShowCommandPalette(false);
    onNavigate(page);
  };

  // Authentic notifications derived strictly from actual profile state
  const rawNotifications = [
    {
      id: 'notif_role',
      title: profile?.target_role ? 'Target Role Configured' : 'Target Role Not Set',
      description: profile?.target_role
        ? `Tracking readiness benchmarks for ${profile.target_role}.`
        : 'Set your target role in Profile to calibrate skill gaps and recommendations.',
      actionPage: 'profile',
      statusLabel: profile?.target_role ? 'Configured' : 'Action needed',
    },
    {
      id: 'notif_readiness',
      title: (profile?.current_readiness_score ?? 0) > 0 ? 'Readiness Calibrated' : 'Readiness Not Calibrated',
      description:
        (profile?.current_readiness_score ?? 0) > 0
          ? `Recorded career readiness score: ${profile?.current_readiness_score}%. Review gaps under Skills.`
          : 'Add your skills and run Career Analysis in Skills to calculate your readiness.',
      actionPage: 'skills/gaps',
      statusLabel: (profile?.current_readiness_score ?? 0) > 0 ? 'Evaluated' : 'Pending',
    },
    {
      id: 'notif_resume',
      title: 'Resume & ATS Workspace',
      description: 'Upload a PDF, DOCX, or TXT resume or match against a target job description.',
      actionPage: 'resume',
      statusLabel: 'Workspace',
    },
  ];

  const unreadCount = rawNotifications.filter(n => !readNotifications.has(n.id)).length;

  const markAllAsRead = () => {
    setReadNotifications(new Set(rawNotifications.map(n => n.id)));
  };

  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.08, delayChildren: 0.05 },
    },
  };

  const itemVariants: Variants = {
    hidden: { opacity: 0, y: -8 },
    visible: {
      opacity: 1,
      y: 0,
      transition: { type: 'spring', stiffness: 300, damping: 24 },
    },
  };

  return (
    <>
      <motion.header
        variants={containerVariants}
        initial="hidden"
        animate="visible"
        className="app-header-bar"
      >
        {/* Left: Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={() => onNavigate('home')}>
            <CareerHubLogo size={34} showSubtitle={false} />
          </div>
        </div>

        {/* Right: Search, Sync, Notifications & Profile */}
        <motion.div variants={itemVariants} style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
          {/* Global Search / Command Palette Trigger */}
          <motion.button
            type="button"
            whileHover={{ borderColor: 'rgba(59, 130, 246, 0.5)' }}
            whileTap={{ scale: 0.98 }}
            onClick={() => setShowCommandPalette(true)}
            title="Open Quick Search (Cmd + K)"
            aria-label="Open Quick Search"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              minWidth: 40,
              minHeight: 40,
              padding: '7px 12px',
              borderRadius: 10,
              background: 'rgba(15, 23, 42, 0.6)',
              border: '1px solid rgba(51, 65, 85, 0.6)',
              color: 'hsl(var(--text-secondary))',
              fontSize: 12.5,
              cursor: 'pointer',
              transition: 'all 0.2s ease',
            }}
          >
            <Search size={15} color="#94a3b8" />
            <span className="header-search-text">Search sections...</span>
            <span
              className="header-search-kbd"
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
              <Command size={10} />K
            </span>
          </motion.button>

          {/* Genuine Data Refresh Button */}
          <motion.button
            type="button"
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={handleSync}
            disabled={isSyncing}
            aria-label="Refresh profile and data"
            data-testid="sync-button"
            title={
              isSyncing
                ? 'Refreshing user profile & state...'
                : syncStatus === 'success'
                ? 'Refreshed successfully'
                : syncStatus === 'error'
                ? syncErrorMessage
                : 'Refresh Data'
            }
            style={{
              position: 'relative',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              width: 40,
              height: 40,
              borderRadius: 10,
              background:
                syncStatus === 'success'
                  ? 'rgba(16, 185, 129, 0.15)'
                  : syncStatus === 'error'
                  ? 'rgba(239, 68, 68, 0.15)'
                  : 'rgba(30, 41, 59, 0.6)',
              border: `1px solid ${
                syncStatus === 'success'
                  ? 'rgba(16, 185, 129, 0.4)'
                  : syncStatus === 'error'
                  ? 'rgba(239, 68, 68, 0.4)'
                  : 'rgba(51, 65, 85, 0.6)'
              }`,
              color: syncStatus === 'success' ? '#10b981' : syncStatus === 'error' ? '#ef4444' : '#94a3b8',
              cursor: isSyncing ? 'not-allowed' : 'pointer',
            }}
          >
            <motion.div
              animate={isSyncing ? { rotate: 360 } : { rotate: 0 }}
              transition={isSyncing ? { repeat: Infinity, duration: 0.8, ease: 'linear' } : { duration: 0.2 }}
            >
              {syncStatus === 'success' ? (
                <Check size={15} color="#10b981" />
              ) : syncStatus === 'error' ? (
                <AlertCircle size={15} color="#ef4444" />
              ) : (
                <RefreshCw size={15} color={isSyncing ? '#38bdf8' : '#94a3b8'} />
              )}
            </motion.div>
          </motion.button>

          {/* Notifications Bell & Popover */}
          <div style={{ position: 'relative' }} ref={notifRef}>
            <motion.button
              type="button"
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setShowNotification(prev => !prev)}
              title="Status Notices"
              aria-label="Status Notices"
              style={{
                position: 'relative',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 40,
                height: 40,
                borderRadius: 10,
                background: showNotification ? 'rgba(59, 130, 246, 0.2)' : 'rgba(30, 41, 59, 0.6)',
                border: showNotification ? '1px solid rgba(59, 130, 246, 0.5)' : '1px solid rgba(51, 65, 85, 0.6)',
                color: showNotification ? '#38bdf8' : '#94a3b8',
                cursor: 'pointer',
              }}
            >
              <Bell size={15} />
              {unreadCount > 0 && (
                <span
                  style={{
                    position: 'absolute',
                    top: 7,
                    right: 7,
                    width: 7,
                    height: 7,
                    borderRadius: '50%',
                    background: '#38bdf8',
                    boxShadow: '0 0 6px #38bdf8',
                  }}
                />
              )}
            </motion.button>

            <AnimatePresence>
              {showNotification && (
                <motion.div
                  initial={{ opacity: 0, y: 10, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 10, scale: 0.95 }}
                  transition={{ duration: 0.15 }}
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 10px)',
                    right: 0,
                    width: 'min(320px, calc(100vw - 24px))',
                    background: 'rgba(15, 23, 42, 0.97)',
                    backdropFilter: 'blur(20px)',
                    border: '1px solid rgba(59, 130, 246, 0.3)',
                    borderRadius: 14,
                    boxShadow: '0 16px 40px rgba(0, 0, 0, 0.6)',
                    padding: 14,
                    zIndex: 70,
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 10 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ fontSize: 13.5, fontWeight: 700, color: '#f1f5f9' }}>Workspace Status</span>
                      {unreadCount > 0 && (
                        <span
                          style={{
                            padding: '1px 6px',
                            background: 'rgba(56, 189, 248, 0.2)',
                            color: '#38bdf8',
                            borderRadius: 8,
                            fontSize: 11,
                            fontWeight: 600,
                          }}
                        >
                          {unreadCount}
                        </span>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button
                        type="button"
                        onClick={markAllAsRead}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#94a3b8',
                          fontSize: 11,
                          cursor: 'pointer',
                          textDecoration: 'underline',
                        }}
                      >
                        Mark all read
                      </button>
                    )}
                  </div>

                  <div style={{ display: 'flex', flexDirection: 'column', gap: 8, maxHeight: 300, overflowY: 'auto' }}>
                    {rawNotifications.map(n => {
                      const isRead = readNotifications.has(n.id);
                      return (
                        <div
                          key={n.id}
                          onClick={() => {
                            setReadNotifications(prev => new Set(prev).add(n.id));
                            setShowNotification(false);
                            onNavigate(n.actionPage);
                          }}
                          style={{
                            padding: '10px 12px',
                            borderRadius: 10,
                            background: isRead ? 'rgba(30, 41, 59, 0.3)' : 'rgba(30, 41, 59, 0.7)',
                            border: `1px solid ${isRead ? 'rgba(51, 65, 85, 0.3)' : 'rgba(59, 130, 246, 0.25)'}`,
                            cursor: 'pointer',
                            transition: 'background 0.15s ease',
                          }}
                        >
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', gap: 8, marginBottom: 4 }}>
                            <span style={{ fontSize: 12.5, fontWeight: 600, color: isRead ? '#94a3b8' : '#f1f5f9' }}>
                              {n.title}
                            </span>
                            <span style={{ fontSize: 10, color: '#64748b', flexShrink: 0 }}>{n.statusLabel}</span>
                          </div>
                          <p style={{ margin: 0, fontSize: 11.5, color: '#94a3b8', lineHeight: 1.4 }}>
                            {n.description}
                          </p>
                        </div>
                      );
                    })}
                  </div>
                </motion.div>
              )}
            </AnimatePresence>
          </div>

          {/* User Profile Shortcut Pill */}
          <motion.div
            whileHover={{ scale: 1.02 }}
            whileTap={{ scale: 0.98 }}
            onClick={() => onNavigate('profile')}
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              minHeight: 40,
              padding: '5px 8px',
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
              {user?.fullName?.charAt(0)?.toUpperCase() || user?.email?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div className="header-profile-meta" style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: 12, fontWeight: 600, color: '#f1f5f9', lineHeight: 1.2 }}>
                {user?.fullName || user?.email || 'Candidate'}
              </span>
              <span style={{ fontSize: 10, color: '#94a3b8', lineHeight: 1.1 }}>
                {profile?.target_role || 'Target role not set'}
              </span>
            </div>
          </motion.div>
        </motion.div>
      </motion.header>

      {/* Global Command Palette & Navigation Search Modal */}
      <AnimatePresence>
        {showCommandPalette && (
          <div
            style={{
              position: 'fixed',
              inset: 0,
              background: 'rgba(0, 0, 0, 0.75)',
              backdropFilter: 'blur(8px)',
              zIndex: 9999,
              display: 'flex',
              alignItems: 'flex-start',
              justifyContent: 'center',
              padding: '10vh 12px 12px',
            }}
            onClick={() => setShowCommandPalette(false)}
          >
            <motion.div
              initial={{ opacity: 0, scale: 0.95, y: -20 }}
              animate={{ opacity: 1, scale: 1, y: 0 }}
              exit={{ opacity: 0, scale: 0.95, y: -20 }}
              transition={{ duration: 0.15 }}
              onClick={e => e.stopPropagation()}
              style={{
                width: '100%',
                maxWidth: 560,
                background: '#0f172a',
                border: '1px solid rgba(59, 130, 246, 0.4)',
                borderRadius: 16,
                boxShadow: '0 24px 60px rgba(0, 0, 0, 0.8)',
                overflow: 'hidden',
              }}
            >
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  padding: '14px 16px',
                  borderBottom: '1px solid rgba(51, 65, 85, 0.6)',
                }}
              >
                <Search size={18} color="#38bdf8" />
                <input
                  ref={searchInputRef}
                  type="text"
                  placeholder="Jump to Home, Resume, Practice, Skills, or Profile..."
                  value={searchQuery}
                  onChange={e => {
                    setSearchQuery(e.target.value);
                    setSelectedIndex(0);
                  }}
                  onKeyDown={e => {
                    if (e.key === 'ArrowDown') {
                      e.preventDefault();
                      setSelectedIndex(prev => (prev + 1) % (filteredItems.length || 1));
                    } else if (e.key === 'ArrowUp') {
                      e.preventDefault();
                      setSelectedIndex(prev => (prev - 1 + filteredItems.length) % (filteredItems.length || 1));
                    } else if (e.key === 'Enter') {
                      e.preventDefault();
                      if (filteredItems[selectedIndex]) {
                        handlePaletteSelect(filteredItems[selectedIndex].page);
                      }
                    }
                  }}
                  style={{
                    flex: 1,
                    background: 'transparent',
                    border: 'none',
                    outline: 'none',
                    color: '#f8fafc',
                    fontSize: 14.5,
                    minWidth: 0,
                  }}
                />
                <button
                  type="button"
                  onClick={() => setShowCommandPalette(false)}
                  aria-label="Close search"
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#64748b',
                    cursor: 'pointer',
                    padding: 6,
                  }}
                >
                  <X size={16} />
                </button>
              </div>

              <div style={{ maxHeight: 340, overflowY: 'auto', padding: 8 }}>
                {filteredItems.length === 0 ? (
                  <div style={{ padding: '24px 20px', textAlign: 'center', color: '#64748b', fontSize: 13 }}>
                    No sections match "{searchQuery}"
                  </div>
                ) : (
                  filteredItems.map((item, idx) => {
                    const isSelected = idx === selectedIndex;
                    return (
                      <div
                        key={item.id}
                        onClick={() => handlePaletteSelect(item.page)}
                        onMouseEnter={() => setSelectedIndex(idx)}
                        style={{
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'space-between',
                          padding: '10px 12px',
                          borderRadius: 10,
                          background: isSelected ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
                          border: isSelected ? '1px solid rgba(59, 130, 246, 0.3)' : '1px solid transparent',
                          cursor: 'pointer',
                        }}
                      >
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 2, minWidth: 0 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8, flexWrap: 'wrap' }}>
                            <span style={{ fontSize: 13.5, fontWeight: 600, color: isSelected ? '#38bdf8' : '#f1f5f9' }}>
                              {item.title}
                            </span>
                            <span
                              style={{
                                fontSize: 10,
                                padding: '1px 6px',
                                borderRadius: 4,
                                background: 'rgba(51, 65, 85, 0.5)',
                                color: '#94a3b8',
                                fontWeight: 600,
                              }}
                            >
                              {item.category}
                            </span>
                          </div>
                          <span style={{ fontSize: 11.5, color: '#94a3b8' }}>{item.description}</span>
                        </div>
                        <ChevronRight size={14} color={isSelected ? '#38bdf8' : '#475569'} />
                      </div>
                    );
                  })
                )}
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </>
  );
};

export default AppHeader;
