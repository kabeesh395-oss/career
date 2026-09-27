import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { useAuth } from '../../context/AuthContext';
import { CareerHubLogo } from '../Common/CareerHubLogo';

export interface NavItem {
  id: string;
  label: string;
  icon?: string;
}

export interface NavSection {
  title: string;
  items: NavItem[];
}

export const navSections: NavSection[] = [
  {
    title: 'Overview',
    items: [
      { id: 'dashboard', label: 'Dashboard', icon: '⬡' },
      { id: 'career', label: 'Career Analysis', icon: '◈' },
      { id: 'roadmap', label: 'Roadmap', icon: '◎' },
    ],
  },
  {
    title: 'Workspace',
    items: [
      { id: 'resume', label: 'Resume', icon: '◉' },
      { id: 'projects', label: 'Projects', icon: '▣' },
      { id: 'interview', label: 'Interview', icon: '◆' },
      { id: 'learning', label: 'Learning', icon: '◇' },
    ],
  },
  {
    title: 'Platform',
    items: [
      { id: 'edge-ai', label: 'Edge AI (Snapdragon)', icon: '⚡' },
      { id: 'integrations', label: 'Integrations', icon: '⬢' },
      { id: 'analytics', label: 'Analytics', icon: '▦' },
      { id: 'templates', label: 'UI Templates', icon: '❖' },
      { id: 'profile', label: 'Profile', icon: '○' },
    ],
  },
];

export const navItems: NavItem[] = navSections.flatMap((section) => section.items);

interface Props {
  activePage: string;
  onNavigate: (page: string) => void;
}

// Crisp, pixel-aligned vector icons for navigation
const renderNavIcon = (id: string, isActive: boolean) => {
  const strokeColor = isActive ? '#3b82f6' : '#64748b';
  const size = 16;

  switch (id) {
    case 'dashboard':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect width="7" height="9" x="3" y="3" rx="1.5" />
          <rect width="7" height="5" x="14" y="3" rx="1.5" />
          <rect width="7" height="9" x="14" y="12" rx="1.5" />
          <rect width="7" height="5" x="3" y="16" rx="1.5" />
        </svg>
      );
    case 'career':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="10" />
          <polygon points="16.24 7.76 14.12 14.12 7.76 16.24 9.88 9.88 16.24 7.76" fill={isActive ? 'rgba(59,130,246,0.25)' : 'none'} />
        </svg>
      );
    case 'roadmap':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <line x1="18" x2="18" y1="20" y2="10" />
          <line x1="12" x2="12" y1="20" y2="4" />
          <line x1="6" x2="6" y1="20" y2="14" />
        </svg>
      );
    case 'resume':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M14.5 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V7.5L14.5 2z" />
          <polyline points="14 2 14 8 20 8" />
          <line x1="16" x2="8" y1="13" y2="13" />
          <line x1="16" x2="8" y1="17" y2="17" />
          <line x1="10" x2="8" y1="9" y2="9" />
        </svg>
      );
    case 'projects':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polyline points="16 18 22 12 16 6" />
          <polyline points="8 6 2 12 8 18" />
        </svg>
      );
    case 'interview':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          <line x1="8" y1="9" x2="16" y2="9" />
          <line x1="8" y1="13" x2="13" y2="13" />
        </svg>
      );
    case 'learning':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2z" />
          <path d="M22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z" />
        </svg>
      );
    case 'integrations':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect width="8" height="8" x="2" y="2" rx="2" />
          <path d="M14 2c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2" />
          <path d="M20 2c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2" />
          <rect width="8" height="8" x="2" y="14" rx="2" />
          <path d="M14 14c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2" />
          <path d="M20 14c1.1 0 2 .9 2 2v4c0 1.1-.9 2-2 2" />
        </svg>
      );
    case 'analytics':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M3 3v18h18" />
          <path d="m19 9-5 5-4-4-3 3" />
        </svg>
      );
    case 'templates':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <rect width="14" height="20" x="5" y="2" rx="2" ry="2" />
          <line x1="12" x2="12.01" y1="18" y2="18" />
        </svg>
      );
    case 'edge-ai':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <polygon points="13 2 3 14 12 14 11 22 21 10 12 10 13 2" fill={isActive ? 'rgba(59,130,246,0.25)' : 'none'} />
        </svg>
      );
    case 'profile':
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <path d="M19 21v-2a4 4 0 0 0-4-4H9a4 4 0 0 0-4 4v2" />
          <circle cx="12" cy="7" r="4" />
        </svg>
      );
    default:
      return (
        <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke={strokeColor} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
          <circle cx="12" cy="12" r="10" />
        </svg>
      );
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
      transition={{ delay: 0.05 + index * 0.02, duration: 0.25 }}
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
        padding: '8px 16px', // Balanced vertical padding system: 8px top/bottom, 16px left/right
        minHeight: 38,
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
        fontSize: 13,
        fontWeight: isActive ? 600 : 500,
        fontFamily: 'inherit',
        textAlign: 'left',
        width: '100%',
        boxSizing: 'border-box',
        outline: 'none',
        transition: 'background-color 0.15s ease, border-color 0.15s ease, color 0.15s ease',
      }}
    >
      {/* Robust, non-decorative active indicator: high-contrast solid anchor bar */}
      {isActive && (
        <motion.div
          layoutId="sidebarActiveIndicator"
          className="sidebar-active-indicator"
          transition={{ type: 'spring', stiffness: 400, damping: 32 }}
          style={{
            position: 'absolute',
            left: 0,
            top: 6,
            bottom: 6,
            width: 3.5,
            backgroundColor: '#3b82f6',
            borderRadius: '0 3px 3px 0',
            boxShadow: '0 0 8px rgba(59, 130, 246, 0.5)',
          }}
        />
      )}

      {/* Structured Icon Badge */}
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

      {/* Label */}
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

      {/* Secondary Active Confirmation Marker */}
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

export default function Sidebar({ activePage, onNavigate }: Props) {
  const { user, logout } = useAuth();

  return (
    <motion.aside
      initial={{ x: -240, opacity: 0 }}
      animate={{ x: 0, opacity: 1 }}
      transition={{ type: 'spring', stiffness: 220, damping: 24 }}
      className="sidebar-container"
      style={{
        width: 240,
        height: '100vh',
        background: 'rgba(11, 15, 25, 0.98)',
        backdropFilter: 'blur(20px)',
        WebkitBackdropFilter: 'blur(20px)',
        borderRight: '1px solid rgba(51, 65, 85, 0.5)',
        display: 'flex',
        flexDirection: 'column',
        padding: '16px 12px',
        position: 'fixed',
        left: 0,
        top: 0,
        zIndex: 50,
        boxSizing: 'border-box',
      }}
    >
      {/* Brand Header */}
      <motion.div
        initial={{ opacity: 0, y: -10 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ delay: 0.1, duration: 0.35 }}
        onClick={() => onNavigate('dashboard')}
        className="sidebar-brand"
        style={{
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          padding: '6px 8px 16px 8px',
          marginBottom: 12,
          borderBottom: '1px solid rgba(51, 65, 85, 0.35)',
          cursor: 'pointer',
        }}
      >
        <CareerHubLogo size={34} showSubtitle={true} />
      </motion.div>

      {/* Categorized Navigation with Professional Spacing & Consistent Margins */}
      <nav
        className="sidebar-nav"
        style={{
          flex: 1,
          display: 'flex',
          flexDirection: 'column',
          gap: 20, // Consistent 20px margin/spacing between category groupings
          overflowY: 'auto',
          overflowX: 'hidden',
          paddingRight: 2,
          marginRight: -2,
          scrollbarWidth: 'thin',
          scrollbarColor: 'rgba(51, 65, 85, 0.4) transparent',
        }}
      >
        {navSections.map((section, sectionIdx) => (
          <div
            key={section.title}
            className="sidebar-category-group"
            style={{
              display: 'flex',
              flexDirection: 'column',
              marginBottom: 0, // Uniform 20px spacing maintained by parent container gap
            }}
          >
            <div
              className="sidebar-category-header"
              style={{
                padding: '4px 16px 8px 16px',
                fontSize: 11,
                fontWeight: 700,
                letterSpacing: '0.08em',
                textTransform: 'uppercase',
                color: '#64748b',
                userSelect: 'none',
              }}
            >
              {section.title}
            </div>

            <div
              className="sidebar-category-items"
              style={{
                display: 'flex',
                flexDirection: 'column',
                gap: 4, // Uniform 4px spacing between items within group
              }}
            >
              {section.items.map((item, itemIdx) => {
                const isActive = activePage === item.id;
                const globalIndex = sectionIdx * 4 + itemIdx;
                return (
                  <NavButton
                    key={item.id}
                    item={item}
                    isActive={isActive}
                    onNavigate={onNavigate}
                    index={globalIndex}
                  />
                );
              })}
            </div>
          </div>
        ))}
      </nav>

      {/* User Footer */}
      <motion.div
        initial={{ opacity: 0 }}
        animate={{ opacity: 1 }}
        transition={{ delay: 0.35 }}
        className="sidebar-footer"
        style={{
          borderTop: '1px solid rgba(51, 65, 85, 0.45)',
          paddingTop: 14,
          marginTop: 12,
          display: 'flex',
          flexDirection: 'column',
          gap: 10,
        }}
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
            {/* Status dot */}
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
              {user?.email || 'user@example.com'}
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
            padding: '8px 16px', // Balanced 8px vertical, 16px horizontal padding
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
    </motion.aside>
  );
}

