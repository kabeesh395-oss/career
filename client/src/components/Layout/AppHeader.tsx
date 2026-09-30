import { useState, useEffect, useRef } from 'react';
import { motion, AnimatePresence, Variants } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { Search, Bell, RefreshCw, Command, Check, AlertCircle, X, ChevronRight, Menu } from 'lucide-react';
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
  { id: 'dash', title: 'Dashboard', category: 'Navigation', description: 'Overview, key metrics, and recommended actions', page: 'dashboard' },
  { id: 'career', title: 'Career Readiness & Skills', category: 'Intelligence', description: 'Benchmark catalog, role readiness, and gap analysis', page: 'career' },
  { id: 'roadmap', title: 'Career Roadmap', category: 'Progression', description: 'Phased milestones, action plans, and trackable goals', page: 'roadmap' },
  { id: 'resume', title: 'Resume ATS Scanner', category: 'Tools', description: 'Upload resumes, run ATS keyword scans, and rewrite bullets', page: 'resume' },
  { id: 'projects', title: 'Portfolio Projects', category: 'Proof', description: 'Track verified projects and link GitHub repositories', page: 'projects' },
  { id: 'interview', title: 'Interview Simulation', category: 'Preparation', description: 'Practice behavioral and system architecture questions', page: 'interview' },
  { id: 'learning', title: 'Learning Resources', category: 'Curriculum', description: 'Technical study modules, quizzes, and skill tracks', page: 'learning' },
  { id: 'integrations', title: 'Integrations & GitHub', category: 'Sync', description: 'Lookup public GitHub profiles and external tooling', page: 'integrations' },
  { id: 'analytics', title: 'Analytics & Telemetry', category: 'Metrics', description: 'Audit trails, verified events, and career velocity', page: 'analytics' },
  { id: 'edge-ai', title: 'Edge AI Assistant', category: 'Local AI', description: 'Client-side inference and offline career intelligence', page: 'edge-ai' },
  { id: 'profile', title: 'Profile & Settings', category: 'Account', description: 'Target role, compensation model, and candidate details', page: 'profile' },
];

export const AppHeader: React.FC<AppHeaderProps> = ({ onNavigate, onToggleMobileMenu }) => {
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
      await refreshProfile();
      setSyncStatus('success');
      setTimeout(() => setSyncStatus('idle'), 3000);
    } catch (err: any) {
      setSyncStatus('error');
      setSyncErrorMessage(err.message || 'Failed to refresh data from server');
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

  // Focus search input when command palette opens
  useEffect(() => {
    if (showCommandPalette) {
      setSearchQuery('');
      setSelectedIndex(0);
      setTimeout(() => searchInputRef.current?.focus(), 50);
    }
  }, [showCommandPalette]);

  // Close notifications on outside click
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

  // Filtered command palette items
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

  // Build authentic notifications from candidate profile and system state
  const rawNotifications = [
    {
      id: 'notif_role',
      title: profile?.target_role ? `Target Role Active` : 'Target Role Not Configured',
      description: profile?.target_role 
        ? `Tracking readiness benchmarks for ${profile.target_role}.`
        : 'Set your target role in Profile to unlock tailored skill gaps.',
      actionPage: 'profile',
      type: profile?.target_role ? 'info' : 'warning',
      timestamp: 'Active'
    },
    {
      id: 'notif_readiness',
      title: 'Readiness Calibration',
      description: (profile?.current_readiness_score ?? 0) > 0
        ? `Current readiness score is ${profile?.current_readiness_score}%. Review gaps in Career tab.`
        : 'Run career readiness analysis to benchmark your engineering skills.',
      actionPage: 'career',
      type: 'info',
      timestamp: 'Today'
    },
    {
      id: 'notif_resume',
      title: 'Resume ATS Vault',
      description: 'Upload your latest technical resume to run automated ATS keyword extraction.',
      actionPage: 'resume',
      type: 'info',
      timestamp: 'Available'
    }
  ];

  const unreadCount = rawNotifications.filter(n => !readNotifications.has(n.id)).length;

  const markAllAsRead = () => {
    const allIds = new Set(rawNotifications.map(n => n.id));
    setReadNotifications(allIds);
  };

  const containerVariants: Variants = {
    hidden: { opacity: 0 },
    visible: {
      opacity: 1,
      transition: { staggerChildren: 0.08, delayChildren: 0.1 },
    },
  };

  const itemVariants: Variants = {
    hidden: { opacity: 0, y: -10 },
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
        {/* Left: Hamburger (mobile) + Brand Logo */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <button
            type="button"
            className="mobile-menu-btn"
            onClick={onToggleMobileMenu}
            aria-label="Open navigation menu"
            data-testid="mobile-menu-button"
          >
            <Menu size={20} />
          </button>
          <div style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }} onClick={() => onNavigate('dashboard')}>
            <CareerHubLogo size={36} showSubtitle={false} />
          </div>
        </div>

        {/* Right: Search, Sync, Notifications & Profile */}
        <motion.div variants={itemVariants} style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          {/* Global Search / Command Palette Trigger */}
          <motion.button
            whileHover={{ borderColor: 'rgba(59, 130, 246, 0.5)', background: 'rgba(15, 23, 42, 0.9)' }}
            whileTap={{ scale: 0.98 }}
            onClick={() => setShowCommandPalette(true)}
            title="Open Command Palette (Cmd + K)"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 8,
              padding: '7px 14px',
              borderRadius: 10,
              background: 'rgba(15, 23, 42, 0.6)',
              border: '1px solid rgba(51, 65, 85, 0.6)',
              color: 'hsl(var(--text-secondary))',
              fontSize: 12.5,
              cursor: 'pointer',
              transition: 'all 0.2s ease',
            }}
          >
            <Search size={14} color="#64748b" />
            <span className="header-search-text">Search destinations & features...</span>
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
            whileHover={{ scale: 1.05 }}
            whileTap={{ scale: 0.95 }}
            onClick={handleSync}
            disabled={isSyncing}
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
              width: 36,
              height: 36,
              borderRadius: 10,
              background: syncStatus === 'success' 
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

          {/* Genuine Notifications Bell & Popover Container */}
          <div style={{ position: 'relative' }} ref={notifRef}>
            <motion.button
              whileHover={{ scale: 1.05 }}
              whileTap={{ scale: 0.95 }}
              onClick={() => setShowNotification(prev => !prev)}
              title="Notifications"
              style={{
                position: 'relative',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                width: 36,
                height: 36,
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
                    top: 6,
                    right: 6,
                    width: 7,
                    height: 7,
                    borderRadius: '50%',
                    background: '#38bdf8',
                    boxShadow: '0 0 6px #38bdf8',
                  }}
                />
              )}
            </motion.button>

            {/* Notification Popover */}
            <AnimatePresence>
              {showNotification && (
                <motion.div
                  initial={{ opacity: 0, y: 10, scale: 0.95 }}
                  animate={{ opacity: 1, y: 0, scale: 1 }}
                  exit={{ opacity: 0, y: 10, scale: 0.95 }}
                  transition={{ duration: 0.15 }}
                  style={{
                    position: 'absolute',
                    top: 'calc(100% + 12px)',
                    right: 0,
                    width: 340,
                    background: 'rgba(15, 23, 42, 0.95)',
                    backdropFilter: 'blur(20px)',
                    border: '1px solid rgba(59, 130, 246, 0.3)',
                    borderRadius: 14,
                    boxShadow: '0 16px 40px rgba(0, 0, 0, 0.6)',
                    padding: 16,
                    zIndex: 60,
                  }}
                >
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 12 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: 6 }}>
                      <span style={{ fontSize: 14, fontWeight: 700, color: '#f1f5f9' }}>Notifications</span>
                      {unreadCount > 0 && (
                        <span style={{
                          padding: '1px 6px',
                          background: 'rgba(56, 189, 248, 0.2)',
                          color: '#38bdf8',
                          borderRadius: 8,
                          fontSize: 11,
                          fontWeight: 600
                        }}>
                          {unreadCount} new
                        </span>
                      )}
                    </div>
                    {unreadCount > 0 && (
                      <button
                        onClick={markAllAsRead}
                        style={{
                          background: 'none',
                          border: 'none',
                          color: '#94a3b8',
                          fontSize: 11,
                          cursor: 'pointer',
                          textDecoration: 'underline'
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
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'baseline', marginBottom: 4 }}>
                            <span style={{ fontSize: 12.5, fontWeight: 600, color: isRead ? '#94a3b8' : '#f1f5f9' }}>
                              {n.title}
                            </span>
                            <span style={{ fontSize: 10, color: '#64748b' }}>{n.timestamp}</span>
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
              {user?.fullName?.charAt(0)?.toUpperCase() || user?.email?.charAt(0)?.toUpperCase() || 'U'}
            </div>
            <div style={{ display: 'flex', flexDirection: 'column' }}>
              <span style={{ fontSize: 12, fontWeight: 600, color: '#f1f5f9', lineHeight: 1.2 }}>
                {user?.fullName || user?.email || 'Candidate'}
              </span>
              <span style={{ fontSize: 10, color: '#94a3b8', lineHeight: 1.1 }}>
                {profile?.target_role || 'Candidate Profile'}
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
              paddingTop: '12vh',
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
                maxWidth: 580,
                background: '#0f172a',
                border: '1px solid rgba(59, 130, 246, 0.4)',
                borderRadius: 16,
                boxShadow: '0 24px 60px rgba(0, 0, 0, 0.8)',
                overflow: 'hidden',
              }}
            >
              {/* Search Input Bar */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: 12,
                  padding: '16px 20px',
                  borderBottom: '1px solid rgba(51, 65, 85, 0.6)',
                }}
              >
                <Search size={18} color="#38bdf8" />
                <input
                  ref={searchInputRef}
                  type="text"
                  placeholder="Type to search destinations (e.g. Resume, Roadmap, Projects)..."
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
                    fontSize: 15,
                  }}
                />
                <button
                  onClick={() => setShowCommandPalette(false)}
                  style={{
                    background: 'transparent',
                    border: 'none',
                    color: '#64748b',
                    cursor: 'pointer',
                    padding: 4,
                  }}
                >
                  <X size={16} />
                </button>
              </div>

              {/* Items List */}
              <div style={{ maxHeight: 360, overflowY: 'auto', padding: 8 }}>
                {filteredItems.length === 0 ? (
                  <div style={{ padding: '24px 20px', textAlign: 'center', color: '#64748b', fontSize: 13 }}>
                    No destinations match "{searchQuery}"
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
                          padding: '10px 14px',
                          borderRadius: 10,
                          background: isSelected ? 'rgba(59, 130, 246, 0.15)' : 'transparent',
                          border: isSelected ? '1px solid rgba(59, 130, 246, 0.3)' : '1px solid transparent',
                          cursor: 'pointer',
                          transition: 'all 0.1s ease',
                        }}
                      >
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
                          <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
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

              {/* Footer Helper */}
              <div
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  padding: '10px 16px',
                  background: 'rgba(15, 23, 42, 0.9)',
                  borderTop: '1px solid rgba(51, 65, 85, 0.4)',
                  fontSize: 11,
                  color: '#64748b',
                }}
              >
                <span>Navigate with <kbd style={{ padding: '1px 4px', background: '#1e293b', borderRadius: 3 }}>↑</kbd> <kbd style={{ padding: '1px 4px', background: '#1e293b', borderRadius: 3 }}>↓</kbd> · Select with <kbd style={{ padding: '1px 4px', background: '#1e293b', borderRadius: 3 }}>↵</kbd></span>
                <span>Press <kbd style={{ padding: '1px 4px', background: '#1e293b', borderRadius: 3 }}>esc</kbd> to close</span>
              </div>
            </motion.div>
          </div>
        )}
      </AnimatePresence>
    </>
  );
};

export default AppHeader;
