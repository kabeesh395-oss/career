import { useState } from 'react';
import { motion } from 'framer-motion';
import { Home, FileText, Mic, Layers, User } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { CareerHubLogo } from '../Common/CareerHubLogo';
import type { PrimarySection } from '../../navigation';

export interface NavItem {
  id: PrimarySection;
  label: string;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

export const navSections: NavSection[] = [
  {
    title: 'Career Hub',
    items: [
      { id: 'home', label: 'Home' },
      { id: 'resume', label: 'Resume' },
      { id: 'practice', label: 'Practice' },
      { id: 'skills', label: 'Skills' },
      { id: 'profile', label: 'Profile' },
    ],
  },
];

export const navItems: NavItem[] = navSections.flatMap((section) => section.items);

interface Props {
  activePage: string;
  onNavigate: (page: string) => void;
  isOpenMobile?: boolean;
  onCloseMobile?: () => void;
}

const renderNavIcon = (id: PrimarySection, isActive: boolean) => {
  const color = isActive ? '#3b82f6' : '#64748b';
  const size = 16;

  switch (id) {
    case 'home':
      return <Home size={size} color={color} />;
    case 'resume':
      return <FileText size={size} color={color} />;
    case 'practice':
      return <Mic size={size} color={color} />;
    case 'skills':
      return <Layers size={size} color={color} />;
    case 'profile':
      return <User size={size} color={color} />;
  }
};

function NavButton({
  item,
  isActive,
  onNavigate,
  index,
}: {
  item: NavItem;
  isActive: boolean;
  onNavigate: (id: string) => void;
  index: number;
}) {
  const [isHovered, setIsHovered] = useState(false);

  return (
    <motion.button
      initial={{ opacity: 0, x: -8 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.04 + index * 0.02, duration: 0.2 }}
      whileHover={{ x: 2 }}
      whileTap={{ scale: 0.985 }}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      onClick={() => onNavigate(item.id)}
      id={`nav-${item.id}`}
      role="menuitem"
      aria-current={isActive ? 'page' : undefined}
      className={`sidebar-nav-item ${isActive ? 'active' : ''}`}
      style={{
        position: 'relative',
        display: 'flex',
        alignItems: 'center',
        gap: 12,
        padding: '10px 16px',
        minHeight: 44,
        borderRadius: 8,
        background: isActive
          ? 'rgba(30, 41, 59, 0.85)'
          : isHovered
          ? 'rgba(30, 41, 59, 0.45)'
          : 'transparent',
        border: isActive
          ? '1px solid rgba(59, 130, 246, 0.35)'
          : isHovered
          ? '1px solid rgba(51, 65, 85, 0.4)'
          : '1px solid transparent',
        boxShadow: isActive
          ? 'inset 0 1px 0 rgba(255, 255, 255, 0.06), 0 2px 4px rgba(0, 0, 0, 0.3)'
          : 'none',
        color: isActive ? '#f8fafc' : isHovered ? '#e2e8f0' : '#94a3b8',
        cursor: 'pointer',
        fontSize: 13.5,
        fontWeight: isActive ? 600 : 500,
        fontFamily: 'inherit',
        textAlign: 'left',
        width: '100%',
        boxSizing: 'border-box',
        outline: 'none',
        transition: 'background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease',
      }}
    >
      {isActive && (
        <motion.div
          layoutId="sidebarActiveIndicator"
          className="sidebar-active-indicator"
          transition={{ type: 'spring', stiffness: 400, damping: 32 }}
          style={{
            position: 'absolute',
            left: 0,
            top: 8,
            bottom: 8,
            width: 3.5,
            backgroundColor: '#3b82f6',
            borderRadius: '0 3px 3px 0',
            boxShadow: '0 0 8px rgba(59, 130, 246, 0.5)',
          }}
        />
      )}

      <span
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          width: 20,
          height: 20,
          flexShrink: 0,
        }}
      >
        {renderNavIcon(item.id, isActive)}
      </span>

      <span
        style={{
          flex: 1,
          whiteSpace: 'nowrap',
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          letterSpacing: '-0.01em',
        }}
      >
        {item.label}
      </span>

      {isActive && (
        <span
          style={{
            width: 5,
            height: 5,
            borderRadius: '50%',
            backgroundColor: '#3b82f6',
            boxShadow: '0 0 6px rgba(59, 130, 246, 0.7)',
            flexShrink: 0,
          }}
        />
      )}
    </motion.button>
  );
}

export default function Sidebar({ activePage, onNavigate, isOpenMobile = false, onCloseMobile }: Props) {
  const { user, logout } = useAuth();

  const handleNav = (page: string) => {
    onNavigate(page);
    onCloseMobile?.();
  };

  return (
    <>
      {isOpenMobile && (
        <div
          className="sidebar-backdrop"
          onClick={onCloseMobile}
          data-testid="sidebar-backdrop"
        />
      )}
      <aside
        className={`sidebar-container ${isOpenMobile ? 'mobile-open' : ''}`}
        aria-label="Sidebar Navigation"
      >
        {/* Brand Header */}
        <div
          className="sidebar-brand"
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            gap: 10,
            padding: '6px 8px 16px 8px',
            marginBottom: 12,
            borderBottom: '1px solid rgba(51, 65, 85, 0.35)',
          }}
        >
          <div
            onClick={() => handleNav('home')}
            style={{ display: 'flex', alignItems: 'center', cursor: 'pointer' }}
          >
            <CareerHubLogo size={34} showSubtitle={true} />
          </div>
          <button
            type="button"
            className="sidebar-mobile-close-btn"
            onClick={onCloseMobile}
            aria-label="Close navigation"
          >
            ✕
          </button>
        </div>

        {/* 5 Primary Navigation Sections */}
        <nav className="sidebar-nav" role="menu">
          {navSections.map((section) => (
            <div key={section.title} className="sidebar-category-group">
              <div className="sidebar-category-header">{section.title}</div>
              <div className="sidebar-category-items">
                {section.items.map((item, itemIdx) => (
                  <NavButton
                    key={item.id}
                    item={item}
                    isActive={activePage === item.id}
                    onNavigate={handleNav}
                    index={itemIdx}
                  />
                ))}
              </div>
            </div>
          ))}
        </nav>

        {/* User Footer */}
        <motion.div
          initial={{ opacity: 0 }}
          animate={{ opacity: 1 }}
          transition={{ delay: 0.2 }}
          className="sidebar-footer"
        >
          <div
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              padding: '8px 12px',
              borderRadius: 8,
              background: 'rgba(15, 23, 42, 0.6)',
              border: '1px solid rgba(51, 65, 85, 0.35)',
            }}
          >
            <div style={{ position: 'relative' }}>
              <div
                style={{
                  width: 32,
                  height: 32,
                  borderRadius: 7,
                  background: 'linear-gradient(135deg, #2563eb, #1d4ed8)',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  fontSize: 12,
                  fontWeight: 700,
                  color: '#fff',
                  border: '1px solid rgba(255, 255, 255, 0.1)',
                }}
              >
                {user?.fullName?.charAt(0)?.toUpperCase() || 'U'}
              </div>
              <span
                style={{
                  position: 'absolute',
                  bottom: -1,
                  right: -1,
                  width: 7,
                  height: 7,
                  borderRadius: '50%',
                  backgroundColor: '#10b981',
                  border: '1.5px solid #0b0f19',
                }}
              />
            </div>
            <div style={{ overflow: 'hidden', flex: 1 }}>
              <div
                style={{
                  fontSize: 12.5,
                  fontWeight: 600,
                  color: '#e2e8f0',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  lineHeight: 1.2,
                }}
              >
                {user?.fullName || 'User'}
              </div>
              <div
                style={{
                  fontSize: 10.5,
                  color: '#64748b',
                  whiteSpace: 'nowrap',
                  overflow: 'hidden',
                  textOverflow: 'ellipsis',
                  lineHeight: 1.2,
                  marginTop: 2,
                }}
              >
                {user?.email || ''}
              </div>
            </div>
          </div>

          <motion.button
            whileHover={{ scale: 1.01, backgroundColor: 'rgba(239, 68, 68, 0.14)' }}
            whileTap={{ scale: 0.98 }}
            onClick={logout}
            id="nav-logout"
            style={{
              width: '100%',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: 6,
              padding: '8px 16px',
              minHeight: 40,
              borderRadius: 7,
              background: 'rgba(239, 68, 68, 0.08)',
              border: '1px solid rgba(239, 68, 68, 0.25)',
              color: '#f87171',
              cursor: 'pointer',
              fontSize: 12,
              fontWeight: 500,
              fontFamily: 'inherit',
              transition: 'background-color 0.15s ease',
            }}
          >
            <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" x2="9" y1="12" y2="12" />
            </svg>
            Sign Out
          </motion.button>
        </motion.div>
      </aside>
    </>
  );
}
