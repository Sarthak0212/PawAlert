import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import MapView from '../components/MapView';
import StatusBadge from '../components/StatusBadge';
import { useToast } from '../context/ToastContext';

const MOCK_DETAIL = {
  id: 1,
  animalName: 'Dog',
  location: 'Sadar Market, Nagpur',
  status: 'ASSIGNED',
  distance: '0.8 km',
  reportDate: new Date(Date.now() - 300000).toISOString(),
  description: 'Injured dog found near the market. Bleeding from hind leg. Needs immediate attention from a trained volunteer.',
  reporterName: 'Priya Sharma',
  reporterPhone: '+91 98765 43210',
};

export default function RescueDetail() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { addToast } = useToast();
  const [completed, setCompleted] = useState(false);
  const [sheetExpanded, setSheetExpanded] = useState(false);

  const report = MOCK_DETAIL;

  function handleComplete() {
    setCompleted(true);
    addToast('🎉 Rescue marked as completed!', 'success');
    setTimeout(() => navigate('/volunteer'), 1500);
  }

  return (
    <div style={{ height: '100vh', display: 'flex', flexDirection: 'column', position: 'relative', overflow: 'hidden' }}>
      {/* Map fills screen */}
      <MapView
        location={report.location}
        style={{ flex: 1, height: sheetExpanded ? '40vh' : '55vh', transition: 'height 0.3s ease' }}
      />

      {/* Top bar */}
      <div style={{
        position: 'absolute', top: 0, left: 0, right: 0,
        display: 'flex', alignItems: 'center', gap: 12,
        padding: '12px 16px',
        background: 'linear-gradient(to bottom, rgba(0,0,0,0.35), transparent)'
      }}>
        <button
          className="btn btn-icon"
          style={{ background: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(8px)', color: 'var(--text)' }}
          onClick={() => navigate(-1)}
        >
          ←
        </button>
        <div style={{ background: 'rgba(255,255,255,0.9)', backdropFilter: 'blur(8px)', borderRadius: 12, padding: '6px 14px' }}>
          <span style={{ fontWeight: 600, fontSize: '0.85rem', color: 'var(--text)' }}>Active Mission</span>
        </div>
        <div style={{ marginLeft: 'auto' }}>
          <StatusBadge status={completed ? 'COMPLETED' : report.status} />
        </div>
      </div>

      {/* Distance badge */}
      <div style={{
        position: 'absolute',
        bottom: sheetExpanded ? '52%' : '42%',
        right: 16,
        transition: 'bottom 0.3s ease',
        display: 'flex', flexDirection: 'column', gap: 8
      }}>
        <MapFAB icon="🧭" label={report.distance} />
        <MapFAB icon="⚡" label="ETA 4min" />
      </div>

      {/* Bottom Sheet */}
      <div
        className="animate-slide-up"
        style={{
          background: 'var(--bg-card)',
          borderRadius: '24px 24px 0 0',
          boxShadow: '0 -8px 32px rgba(0,0,0,0.15)',
          padding: '0 16px 32px',
          transition: 'all 0.3s ease',
          zIndex: 10,
          maxHeight: sheetExpanded ? '65vh' : '50vh',
          overflow: 'auto',
        }}
      >
        {/* Drag handle */}
        <div
          style={{ display: 'flex', justifyContent: 'center', paddingTop: 12, paddingBottom: 8, cursor: 'pointer' }}
          onClick={() => setSheetExpanded(e => !e)}
        >
          <div style={{ width: 36, height: 4, borderRadius: 2, background: 'var(--border)' }} />
        </div>

        {/* Animal header */}
        <div style={{ display: 'flex', alignItems: 'center', gap: 14, marginBottom: 16 }}>
          <div style={{
            width: 56, height: 56, borderRadius: 16,
            background: 'var(--bg-muted)',
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            fontSize: '1.8rem'
          }}>🐕</div>
          <div>
            <h2 style={{ marginBottom: 2 }}>{report.animalName} Rescue</h2>
            <p style={{ fontSize: '0.8rem' }}>📍 {report.location}</p>
          </div>
        </div>

        {/* Description */}
        <p style={{ marginBottom: 16, fontSize: '0.875rem', lineHeight: 1.6 }}>{report.description}</p>

        <div className="divider" />

        {/* Reporter info */}
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: 16, padding: '8px 0' }}>
          <div>
            <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 2 }}>Reported by</div>
            <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{report.reporterName}</div>
            <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{report.reporterPhone}</div>
          </div>
          <a
            href={`tel:${report.reporterPhone}`}
            className="btn btn-outline-primary btn-sm"
            style={{ textDecoration: 'none' }}
          >
            📞 Call
          </a>
        </div>

        <div className="divider" />

        {/* Action buttons */}
        <div style={{ display: 'flex', gap: 10, marginTop: 16 }}>
          <button
            className="btn btn-outline-primary"
            style={{ flex: 1 }}
            onClick={() => addToast('Opening navigation…', 'success')}
          >
            🗺 Navigate
          </button>
          <button
            className="btn btn-primary"
            style={{ flex: 1 }}
            onClick={handleComplete}
            disabled={completed}
          >
            {completed ? '✓ Completed!' : '✓ Mark Done'}
          </button>
        </div>
      </div>
    </div>
  );
}

function MapFAB({ icon, label }) {
  return (
    <div style={{
      background: 'rgba(255,255,255,0.95)',
      backdropFilter: 'blur(8px)',
      borderRadius: 12,
      padding: '6px 10px',
      textAlign: 'center',
      boxShadow: '0 2px 12px rgba(0,0,0,0.12)',
    }}>
      <div style={{ fontSize: '1.2rem' }}>{icon}</div>
      <div style={{ fontSize: '0.65rem', fontWeight: 700, color: 'var(--text)', marginTop: 2 }}>{label}</div>
    </div>
  );
}
