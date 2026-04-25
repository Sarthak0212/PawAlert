import React, { useState } from 'react';
import StatusBadge from './StatusBadge';
import { useToast } from '../context/ToastContext';

export default function AdminCard({ report, onApprove, onReject }) {
  const [loading, setLoading] = useState(null);
  const { addToast } = useToast();

  async function handle(type) {
    setLoading(type);
    try {
      if (type === 'approve' && onApprove) await onApprove(report);
      if (type === 'reject' && onReject) await onReject(report);
    } finally {
      setLoading(null);
    }
  }

  const isPending = !report.status || report.status === 'PENDING' || report.status === 'OPEN';

  return (
    <div className="card animate-fade-in" style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: 10 }}>
        <div>
          <h3 style={{ marginBottom: 4 }}>{report.animalName}</h3>
          <p style={{ fontSize: '0.78rem' }}>📍 {report.location}</p>
        </div>
        <StatusBadge status={report.status || 'PENDING'} />
      </div>

      {report.description && (
        <p style={{ fontSize: '0.82rem', marginBottom: 10 }}>
          {report.description.length > 100 ? report.description.slice(0, 100) + '…' : report.description}
        </p>
      )}

      <div style={{ display: 'flex', gap: 12, marginBottom: 12, flexWrap: 'wrap' }}>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>👤 {report.reporterName || 'Anonymous'}</span>
        <span style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>🕒 {formatDate(report.reportDate)}</span>
      </div>

      {isPending && (
        <div style={{ display: 'flex', gap: 8 }}>
          <button className="btn btn-primary btn-sm" style={{ flex: 1 }} disabled={loading !== null} onClick={() => handle('approve')}>
            {loading === 'approve' ? '…' : '✓ Approve'}
          </button>
          <button className="btn btn-outline-danger btn-sm" style={{ flex: 1 }} disabled={loading !== null} onClick={() => handle('reject')}>
            {loading === 'reject' ? '…' : '✕ Reject'}
          </button>
        </div>
      )}
    </div>
  );
}

function formatDate(str) {
  if (!str) return 'Unknown date';
  return new Date(str).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
}
