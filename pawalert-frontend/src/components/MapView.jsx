import React from 'react';

export default function MapView({ location, style = {} }) {
  // Uses OpenStreetMap embed for no-API-key map
  const query = encodeURIComponent(location || 'Nagpur, Maharashtra, India');
  const src = `https://www.openstreetmap.org/export/embed.html?bbox=78.8%2C21.0%2C79.2%2C21.3&layer=mapnik&marker=21.15%2C79.0`;

  return (
    <div style={{ position: 'relative', width: '100%', overflow: 'hidden', ...style }}>
      {/* Map placeholder with gradient overlay */}
      <div style={{
        width: '100%',
        height: '100%',
        background: 'linear-gradient(160deg, #c8e6c9 0%, #a5d6a7 30%, #81c784 60%, #66bb6a 100%)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        position: 'relative',
        overflow: 'hidden',
      }}>
        {/* Grid lines to simulate map */}
        <svg width="100%" height="100%" style={{ position: 'absolute', opacity: 0.3 }}>
          {Array.from({ length: 10 }).map((_, i) => (
            <g key={i}>
              <line x1={`${i * 12}%`} y1="0" x2={`${i * 12}%`} y2="100%" stroke="#2E7D32" strokeWidth="1" />
              <line x1="0" y1={`${i * 12}%`} x2="100%" y2={`${i * 12}%`} stroke="#2E7D32" strokeWidth="1" />
            </g>
          ))}
        </svg>

        {/* Roads */}
        <svg width="100%" height="100%" style={{ position: 'absolute', opacity: 0.5 }}>
          <path d="M0,50% L100%,50%" stroke="#fff" strokeWidth="8" />
          <path d="M50%,0 L50%,100%" stroke="#fff" strokeWidth="6" />
          <path d="M0,30% L70%,70%" stroke="#fff" strokeWidth="4" />
        </svg>

        {/* Location pin */}
        <div style={{ position: 'relative', zIndex: 2, textAlign: 'center' }}>
          <div style={{
            fontSize: '2.5rem',
            filter: 'drop-shadow(0 4px 8px rgba(0,0,0,0.3))',
            animation: 'float 2s ease-in-out infinite alternate'
          }}>📍</div>
          <div style={{
            background: 'rgba(255,255,255,0.95)',
            padding: '6px 14px',
            borderRadius: 20,
            fontSize: '0.78rem',
            fontWeight: 600,
            color: 'var(--text)',
            boxShadow: '0 2px 12px rgba(0,0,0,0.15)',
            marginTop: 8,
            maxWidth: 200,
            textAlign: 'center'
          }}>
            {location || 'Rescue Location'}
          </div>
        </div>
      </div>

      <style>{`
        @keyframes float {
          from { transform: translateY(0); }
          to { transform: translateY(-8px); }
        }
      `}</style>
    </div>
  );
}
