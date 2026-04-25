import React, { useRef } from 'react';
import { useToast } from '../context/ToastContext';

export default function ToastContainer() {
  const { toasts } = useToast();
  return (
    <div className="toast-container">
      {toasts.map(t => <Toast key={t.id} toast={t} />)}
    </div>
  );
}

function Toast({ toast }) {
  const ref = useRef(null);
  const typeClass = toast.type === 'success' ? 'toast-success'
    : toast.type === 'error' ? 'toast-error' : 'toast-warning';

  const icon = toast.type === 'success' ? '✓' : toast.type === 'error' ? '✕' : '⚠';

  return (
    <div ref={ref} className={`toast ${typeClass} ${toast.removing ? 'removing' : ''}`}>
      <span style={{ fontSize: '1rem', fontWeight: 700 }}>{icon}</span>
      <span>{toast.message}</span>
    </div>
  );
}
