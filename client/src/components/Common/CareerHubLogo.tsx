import React from 'react';

interface CareerHubLogoProps {
  iconOnly?: boolean;
  size?: number;
  showSubtitle?: boolean;
}

export const CareerHubLogo: React.FC<CareerHubLogoProps> = ({
  iconOnly = false,
  size = 36,
  showSubtitle = true,
}) => {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
      {/* Constellation Network Emblem with Growth Arrow */}
      <svg
        width={size}
        height={size}
        viewBox="0 0 100 100"
        fill="none"
        xmlns="http://www.w3.org/2000/svg"
        style={{ flexShrink: 0 }}
      >
        <circle cx="50" cy="50" r="46" fill="#0B2545" stroke="#134074" strokeWidth="2" />
        
        {/* Network constellation background lines */}
        <path d="M50 18 L50 38 M50 62 L50 82 M18 50 L38 50 M62 50 L82 50" stroke="#1D4ED8" strokeWidth="1.5" opacity="0.7" />
        <path d="M27 27 L40 40 M60 60 L73 73 M73 27 L60 40 M40 60 L27 73" stroke="#2563EB" strokeWidth="1.5" opacity="0.7" />
        
        {/* Core Node Circle with C */}
        <circle cx="50" cy="50" r="12" fill="#0F172A" stroke="#38BDF8" strokeWidth="2.5" />
        <text x="50" y="55" textAnchor="middle" fill="#FFFFFF" fontSize="13" fontWeight="900" fontFamily="sans-serif">C</text>
        
        {/* Constellation Outer Nodes */}
        <circle cx="50" cy="18" r="3.5" fill="#60A5FA" />
        <circle cx="50" cy="82" r="3.5" fill="#60A5FA" />
        <circle cx="18" cy="50" r="3.5" fill="#60A5FA" />
        <circle cx="82" cy="50" r="3.5" fill="#60A5FA" />
        <circle cx="27" cy="27" r="3" fill="#93C5FD" />
        <circle cx="73" cy="73" r="3" fill="#93C5FD" />
        <circle cx="27" cy="73" r="3" fill="#93C5FD" />
        
        {/* Growth Arrow shooting top-right */}
        <path d="M34 66 L68 32" stroke="#00B4D8" strokeWidth="4" strokeLinecap="round" />
        <path d="M52 32 L68 32 L68 48" fill="none" stroke="#00B4D8" strokeWidth="4" strokeLinecap="round" strokeLinejoin="round" />
      </svg>

      {!iconOnly && (
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <div style={{ display: 'flex', alignItems: 'center', fontSize: size * 0.48, fontWeight: 800, letterSpacing: '-0.02em', lineHeight: 1.1 }}>
            <span style={{ color: '#0B2545', background: 'linear-gradient(135deg, #F8FAFC, #CBD5E1)', WebkitBackgroundClip: 'text', WebkitTextFillColor: 'transparent' }}>
              Career
            </span>
            <span style={{ color: '#38BDF8' }}>Hub</span>
          </div>
          {showSubtitle && (
            <span style={{ fontSize: Math.max(8, size * 0.22), fontWeight: 700, color: '#64748B', letterSpacing: '0.1em', textTransform: 'uppercase', lineHeight: 1 }}>
              PROFESSIONAL NETWORK
            </span>
          )}
        </div>
      )}
    </div>
  );
};

export default CareerHubLogo;
