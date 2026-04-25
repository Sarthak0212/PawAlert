import React, { useState, useEffect } from 'react';
import RescueCard from '../components/RescueCard';
import Navbar from '../components/Navbar';
import { useToast } from '../context/ToastContext';
import { useNavigate } from 'react-router-dom';

const API = 'http://localhost:8081/api';

const MOCK_REPORTS = [
  { id: 1, animalName: 'Dog', location: 'Sadar, Nagpur', status: 'URGENT', distance: '0.8 km', reportDate: new Date(Date.now() - 300000).toISOString(), description: 'Injured dog found near the market. Bleeding from hind leg. Needs immediate attention.' },
  { id: 2, animalName: 'Cat', location: 'Dharampeth, Nagpur', status: 'OPEN', distance: '1.4 km', reportDate: new Date(Date.now() - 1200000).toISOString(), description: 'Kitten stuck in a drain. Cannot get out on its own.' },
  { id: 3, animalName: 'Bird', location: 'Civil Lines, Nagpur', status: 'OPEN', distance: '2.1 km', reportDate: new Date(Date.now() - 3600000).toISOString(), description: 'Injured parrot with broken wing spotted on rooftop.' },
  { id: 4, animalName: 'Cow', location: 'Itwari, Nagpur', status: 'ASSIGNED', distance: '3.5 km', reportDate: new Date(Date.now() - 7200000).toISOString(), description: 'Stray cow injured by vehicle. Lying on road.' },
];

export default function VolunteerDashboard() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('ALL');
  const { addToast } = useToast();
  const navigate = useNavigate();

  useEffect(() => {
    fetch(`${API}/dashboard/volunteer/2`)
      .then(r => r.json())
      .then(d => {
        setReports(d.rescueTasks?.map(t => ({ ...t.report, distance: '~' + (Math.random() * 4 + 0.5).toFixed(1) + ' km' })) || MOCK_REPORTS);
      })
      .catch(() => setReports(MOCK_REPORTS))
      .finally(() => setLoading(false));
  }, []);

  const filtered = reports.filter(r => filter === 'ALL' || r.status === filter);

  async function handleAccept(report) {
    await new Promise(r => setTimeout(r, 700));
    setReports(prev => prev.map(r => r.id === report.id ? { ...r, status: 'ASSIGNED' } : r));
    addToast(`Rescue accepted! Heading to ${report.location}`, 'success');
    setTimeout(() => navigate(`/rescue/${report.id}`), 800);
  }

  async function handleReject(report) {
    await new Promise(r => setTimeout(r, 500));
    setReports(prev => prev.filter(r => r.id !== report.id));
    addToast('Rescue request declined', 'warning');
  }

  return (
    <div className="page">
      {/* Header */}
      <div className="page-header">
        <div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 2 }}>Good morning 👋</div>
          <h1 style={{ fontSize: '1.3rem' }}>Rescue Requests</h1>
        </div>
        <div style={{
          width: 40, height: 40, borderRadius: 12, background: 'var(--primary)',
          display: 'flex', alignItems: 'center', justifyContent: 'center',
          color: '#fff', fontSize: '1.2rem', cursor: 'pointer'
        }}
          onClick={() => navigate('/profile')}
        >
          🐾
        </div>
      </div>

      <div className="page-content">
        {/* Stats */}
        <div className="stats-row">
          <div className="stat-chip">
            <div className="stat-chip-value">{reports.filter(r => r.status === 'OPEN' || r.status === 'URGENT').length}</div>
            <div className="stat-chip-label">Open</div>
          </div>
          <div className="stat-chip">
            <div className="stat-chip-value" style={{ color: 'var(--secondary)' }}>{reports.filter(r => r.status === 'ASSIGNED').length}</div>
            <div className="stat-chip-label">Active</div>
          </div>
          <div className="stat-chip">
            <div className="stat-chip-value" style={{ color: 'var(--primary-dark)' }}>{reports.filter(r => r.status === 'COMPLETED').length}</div>
            <div className="stat-chip-label">Done</div>
          </div>
        </div>

        {/* Filters */}
        <div className="filter-tabs" style={{ marginBottom: 16 }}>
          {['ALL', 'URGENT', 'OPEN', 'ASSIGNED', 'COMPLETED'].map(f => (
            <button key={f} className={`filter-tab ${filter === f ? 'active' : ''}`} onClick={() => setFilter(f)}>
              {f === 'ALL' ? 'All' : f.charAt(0) + f.slice(1).toLowerCase()}
            </button>
          ))}
        </div>

        {/* Cards */}
        {loading ? (
          <SkeletonList />
        ) : filtered.length === 0 ? (
          <EmptyState filter={filter} />
        ) : (
          filtered.map(r => (
            <RescueCard
              key={r.id}
              report={r}
              onAccept={handleAccept}
              onReject={handleReject}
              showActions
            />
          ))
        )}
      </div>

      <Navbar role="volunteer" />
    </div>
  );
}

function SkeletonList() {
  return (
    <>
      {[1, 2, 3].map(i => (
        <div key={i} className="card" style={{ marginBottom: 12 }}>
          <div style={{ display: 'flex', gap: 12, alignItems: 'center', marginBottom: 12 }}>
            <div className="skeleton" style={{ width: 48, height: 48, borderRadius: 12 }} />
            <div style={{ flex: 1 }}>
              <div className="skeleton" style={{ height: 16, width: '60%', marginBottom: 8 }} />
              <div className="skeleton" style={{ height: 12, width: '40%' }} />
            </div>
          </div>
          <div className="skeleton" style={{ height: 12, width: '100%', marginBottom: 8 }} />
          <div className="skeleton" style={{ height: 12, width: '80%' }} />
        </div>
      ))}
    </>
  );
}

function EmptyState({ filter }) {
  return (
    <div className="empty-state">
      <div className="empty-icon">🐾</div>
      <h3>No {filter === 'ALL' ? '' : filter.toLowerCase() + ' '}requests</h3>
      <p>All clear! No rescue requests matching your filter right now. Check back soon.</p>
    </div>
  );
}
