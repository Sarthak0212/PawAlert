import React from 'react';

export default function ProfileCard({ profile, stats }) {
  const initials = profile?.name
    ? profile.name.split(' ').map(n => n[0]).join('').slice(0, 2).toUpperCase()
    : '?';

  const roleColor = profile?.role === 'VOLUNTEER' ? 'var(--primary)' : 'var(--secondary)';

  return (
    <div className="card" style={{ textAlign: 'center', marginBottom: 16 }}>
      {/* Avatar */}
      <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
        <div className="avatar avatar-lg">
          {initials}
        </div>
      </div>

      {/* Name & role */}
      <h2 style={{ marginBottom: 4 }}>{profile?.name || 'User'}</h2>
      <p style={{ marginBottom: 8, fontSize: '0.85rem' }}>{profile?.email}</p>
      <span style={{
        display: 'inline-block',
        background: `${roleColor}18`,
        color: roleColor,
        padding: '3px 12px',
        borderRadius: 'var(--radius-full)',
        fontSize: '0.75rem',
        fontWeight: 700,
        marginBottom: 20,
        textTransform: 'uppercase',
        letterSpacing: '0.05em'
      }}>
        {profile?.role || 'Member'}
      </span>

      {/* Stats */}
      {stats && (
        <div style={{ display: 'flex', justifyContent: 'space-around', borderTop: '1px solid var(--border-light)', paddingTop: 16 }}>
          <StatItem value={stats.total ?? 0} label="Total" />
          <div style={{ width: 1, background: 'var(--border-light)' }} />
          <StatItem value={stats.active ?? 0} label="Active" color="var(--secondary)" />
          <div style={{ width: 1, background: 'var(--border-light)' }} />
          <StatItem value={stats.completed ?? 0} label="Done" color="var(--primary)" />
        </div>
      )}
    </div>
  );
}

function StatItem({ value, label, color }) {
  return (
    <div style={{ flex: 1, textAlign: 'center' }}>
      <div style={{ fontSize: '1.5rem', fontWeight: 800, color: color || 'var(--text)' }}>{value}</div>
      <div style={{ fontSize: '0.7rem', color: 'var(--text-muted)', marginTop: 2 }}>{label}</div>
    </div>
  );
}
