import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import RescueCard from '../components/RescueCard';
import Navbar from '../components/Navbar';

const API = 'http://localhost:8081/api';
const MOCK = {
  profile: { name: 'Priya Sharma', email: 'priya@example.com', role: 'REPORTER' },
  reports: [
    { id: 1, animalName: 'Dog', location: 'Sadar, Nagpur', status: 'OPEN', description: 'Injured dog near market.', reportDate: new Date(Date.now() - 300000).toISOString() },
    { id: 2, animalName: 'Cat', location: 'Dharampeth', status: 'ASSIGNED', description: 'Kitten in drain.', reportDate: new Date(Date.now() - 3600000).toISOString() },
  ],
  stats: { total: 5, active: 1, completed: 4 },
};

export default function ReporterDashboard() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    fetch(`${API}/dashboard/reporter/1`)
      .then(r => r.json())
      .then(d => setData({ profile: d.profile, reports: d.reports?.map(r => r.report) || MOCK.reports, stats: { total: d.reports?.length || 0, active: d.reports?.filter(r => r.report?.status === 'OPEN').length || 0, completed: d.reports?.filter(r => r.report?.status === 'RESCUED').length || 0 } }))
      .catch(() => setData(MOCK))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="page"><div className="empty-state"><div style={{ width: 36, height: 36, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} /></div></div>;

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 2 }}>Welcome back 👋</div>
          <h1 style={{ fontSize: '1.3rem' }}>{data.profile?.name || 'Reporter'}</h1>
        </div>
        <div style={{ width: 40, height: 40, borderRadius: 12, background: 'var(--primary)', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#fff', fontSize: '1.1rem', cursor: 'pointer' }} onClick={() => navigate('/profile')}>
          👤
        </div>
      </div>

      <div className="page-content">
        <div className="stats-row">
          <div className="stat-chip"><div className="stat-chip-value">{data.stats.total}</div><div className="stat-chip-label">Reports</div></div>
          <div className="stat-chip"><div className="stat-chip-value" style={{ color: 'var(--secondary)' }}>{data.stats.active}</div><div className="stat-chip-label">Active</div></div>
          <div className="stat-chip"><div className="stat-chip-value" style={{ color: 'var(--primary-dark)' }}>{data.stats.completed}</div><div className="stat-chip-label">Rescued</div></div>
        </div>

        {/* Quick report CTA */}
        <div style={{
          background: 'linear-gradient(135deg, var(--secondary) 0%, var(--secondary-dark) 100%)',
          borderRadius: 'var(--radius-lg)', padding: '20px', marginBottom: 16, cursor: 'pointer',
          boxShadow: '0 4px 20px rgba(255,112,67,0.3)', color: '#fff',
          display: 'flex', alignItems: 'center', justifyContent: 'space-between'
        }}
          onClick={() => navigate('/report')}
        >
          <div>
            <div style={{ fontWeight: 700, fontSize: '1rem', marginBottom: 4 }}>See an animal in distress?</div>
            <div style={{ fontSize: '0.8rem', opacity: 0.9 }}>Tap to file a rescue report now</div>
          </div>
          <div style={{ fontSize: '2rem' }}>🚨</div>
        </div>

        <h2 style={{ marginBottom: 12 }}>My Reports</h2>
        {data.reports.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📋</div>
            <h3>No reports yet</h3>
            <p>You haven't submitted any rescue reports. Tap the button above to get started.</p>
            <button className="btn btn-secondary" onClick={() => navigate('/report')}>Report an Animal</button>
          </div>
        ) : (
          data.reports.map(r => <RescueCard key={r.id} report={r} showActions={false} />)
        )}
      </div>

      <Navbar role="reporter" />
    </div>
  );
}
