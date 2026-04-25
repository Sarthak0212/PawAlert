import React, { useState } from 'react';
import StatusBadge from './StatusBadge';
import { useToast } from '../context/ToastContext';

const ANIMAL_EMOJI = { Dog: '🐕', Cat: '🐈', Bird: '🐦', Cow: '🐄', default: '🐾' };

export default function RescueCard({ report, onAccept, onReject, showActions = true, style = {} }) {
  const [loading, setLoading] = useState(null);
  const { addToast } = useToast();

  const emoji = ANIMAL_EMOJI[report.animalName] || ANIMAL_EMOJI.default;
  const isUrgent = report.status === 'URGENT' || report.urgency === 'HIGH';

  async function handleAction(type) {
    setLoading(type);
    try {
      if (type === 'accept' && onAccept) await onAccept(report);
      if (type === 'reject' && onReject) await onReject(report);
    } finally {
      setLoading(null);
    }
  }

  return (
    <div
      className="card card-clickable animate-fade-in"
      style={{ marginBottom: 12, position: 'relative', overflow: 'hidden', ...style }}
    >
      {isUrgent && (
        <div style={{
          position: 'absolute', top: 0, left: 0, right: 0, height: 3,
          background: 'linear-gradient(90deg, var(--danger), var(--secondary))'
        }} />
      )}

      {/* Header row */}
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', marginBottom: 12 }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <div style={{
            width: 48, height: 48, borderRadius: 12,
            background: 'var(--bg-muted)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.6rem', flexShrink: 0
          }}>
            {emoji}
          </div>
          <div>
            <h3 style={{ marginBottom: 2 }}>{report.animalName || 'Unknown Animal'}</h3>
            <p style={{ fontSize: '0.78rem', color: 'var(--text-muted)' }}>
              📍 {report.location || 'Location not specified'}
            </p>
          </div>
        </div>
        <StatusBadge status={report.status} />
      </div>

      {/* Description */}
      {report.description && (
        <p style={{ fontSize: '0.82rem', marginBottom: 12, color: 'var(--text-secondary)', lineHeight: 1.5 }}>
          {report.description.length > 90 ? report.description.slice(0, 90) + '…' : report.description}
        </p>
      )}

      {/* Meta row */}
      <div style={{ display: 'flex', gap: 16, marginBottom: showActions ? 14 : 0 }}>
        <MetaTag icon="📏" text={report.distance || '1.2 km'} />
        <MetaTag icon="🕒" text={formatTime(report.reportDate)} />
        {report.reporterName && <MetaTag icon="👤" text={report.reporterName} />}
      </div>

      {/* Actions */}
      {showActions && report.status === 'OPEN' && (
        <div style={{ display: 'flex', gap: 10 }}>
          <button
            className="btn btn-secondary"
            style={{ flex: 1 }}
            disabled={loading !== null}
            onClick={() => handleAction('accept')}
          >
            {loading === 'accept' ? <Spinner /> : '✓ Accept'}
          </button>
          <button
            className="btn btn-outline-danger"
            style={{ flex: 1 }}
            disabled={loading !== null}
            onClick={() => handleAction('reject')}
          >
            {loading === 'reject' ? <Spinner /> : '✕ Reject'}
          </button>
        </div>
      )}

      {showActions && report.status === 'ASSIGNED' && (
        <div style={{ display: 'flex', gap: 10 }}>
          <button className="btn btn-primary" style={{ flex: 1 }} onClick={() => addToast('Navigating to rescue location', 'success')}>
            🗺 Navigate
          </button>
        </div>
      )}
    </div>
  );
}

function MetaTag({ icon, text }) {
  return (
    <span style={{ display: 'flex', alignItems: 'center', gap: 4, fontSize: '0.75rem', color: 'var(--text-muted)' }}>
      {icon} {text}
    </span>
  );
}

function Spinner() {
  return (
    <span style={{
      width: 16, height: 16, border: '2px solid rgba(255,255,255,0.3)',
      borderTopColor: '#fff', borderRadius: '50%',
      animation: 'spin 0.7s linear infinite', display: 'inline-block'
    }} />
  );
}

function formatTime(dateStr) {
  if (!dateStr) return 'Just now';
  const d = new Date(dateStr);
  const now = new Date();
  const diff = Math.floor((now - d) / 60000);
  if (diff < 1) return 'Just now';
  if (diff < 60) return `${diff}m ago`;
  if (diff < 1440) return `${Math.floor(diff / 60)}h ago`;
  return d.toLocaleDateString();
}
