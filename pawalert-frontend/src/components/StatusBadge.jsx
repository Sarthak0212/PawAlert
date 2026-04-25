import React from 'react';

const dot = { width: 6, height: 6, borderRadius: '50%', display: 'inline-block', marginRight: 4 };

const configs = {
  OPEN:      { label: 'Open',      cls: 'badge-open',      color: 'var(--primary)' },
  URGENT:    { label: 'Urgent',    cls: 'badge-urgent',     color: 'var(--danger)' },
  ASSIGNED:  { label: 'Assigned',  cls: 'badge-assigned',   color: 'var(--warning)' },
  COMPLETED: { label: 'Completed', cls: 'badge-completed',  color: 'var(--primary-dark)' },
  PENDING:   { label: 'Pending',   cls: 'badge-pending',    color: 'var(--warning)' },
  APPROVED:  { label: 'Approved',  cls: 'badge-approved',   color: 'var(--primary)' },
  REJECTED:  { label: 'Rejected',  cls: 'badge-rejected',   color: 'var(--danger)' },
  RESCUED:   { label: 'Rescued',   cls: 'badge-completed',  color: 'var(--primary-dark)' },
};

export default function StatusBadge({ status }) {
  const cfg = configs[status?.toUpperCase()] || configs.PENDING;
  return (
    <span className={`badge ${cfg.cls}`}>
      <span style={{ ...dot, background: cfg.color }} />
      {cfg.label}
    </span>
  );
}
