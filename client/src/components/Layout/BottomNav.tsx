import React from 'react';
import { Home, FileText, Mic, Layers, User } from 'lucide-react';

export interface PrimaryNavItem {
  id: 'home' | 'resume' | 'practice' | 'skills' | 'profile';
  label: string;
}

export const PRIMARY_NAV_ITEMS: PrimaryNavItem[] = [
  { id: 'home', label: 'Home' },
  { id: 'resume', label: 'Resume' },
  { id: 'practice', label: 'Practice' },
  { id: 'skills', label: 'Skills' },
  { id: 'profile', label: 'Profile' },
];

interface BottomNavProps {
  activePage: string;
  onNavigate: (page: string) => void;
}

function renderIcon(id: PrimaryNavItem['id'], isActive: boolean) {
  const strokeWidth = isActive ? 2.25 : 1.85;
  switch (id) {
    case 'home':
      return <Home size={19} strokeWidth={strokeWidth} />;
    case 'resume':
      return <FileText size={19} strokeWidth={strokeWidth} />;
    case 'practice':
      return <Mic size={19} strokeWidth={strokeWidth} />;
    case 'skills':
      return <Layers size={19} strokeWidth={strokeWidth} />;
    case 'profile':
      return <User size={19} strokeWidth={strokeWidth} />;
  }
}

export const BottomNav: React.FC<BottomNavProps> = ({ activePage, onNavigate }) => {
  return (
    <nav className="mobile-bottom-nav" aria-label="Primary Mobile Navigation" data-testid="bottom-nav">
      {PRIMARY_NAV_ITEMS.map((item) => {
        const isActive = activePage === item.id;
        return (
          <button
            key={item.id}
            type="button"
            id={`bottom-nav-${item.id}`}
            onClick={() => onNavigate(item.id)}
            aria-current={isActive ? 'page' : undefined}
            className={`mobile-bottom-nav-btn ${isActive ? 'active' : ''}`}
          >
            {renderIcon(item.id, isActive)}
            <span>{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export default BottomNav;
