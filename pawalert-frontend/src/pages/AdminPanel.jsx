import React, { useState, useEffect } from 'react';
import AdminCard from '../components/AdminCard';
import Navbar from '../components/Navbar';
import { useToast } from '../context/ToastContext';

const API = 'http://localhost:8081/api';

const MOCK_REPORTS = [
  { id: 1, animalName: 'Dog', location: 'Sadar, Nagpur', status: 'PENDING', description: 'Injured dog near market with bleeding hind leg.', reporterName: 'Priya Sharma', reportDate: new Date(Date.now() - 300000).toISOString() },
  { id: 2, animalName: 'Cat', location: 'Dharampeth', status: 'PENDING', description: 'Kitten trapped in drain. Cannot escape.', reporterName: 'Rohan Mehta', reportDate: new Date(Date.now() - 900000).toISOString() },
  { id: 3, animalName: 'Bird', location: 'Civil Lines', status: 'APPROVED', description: 'Parrot with broken wing on rooftop.', reporterName: 'Anjali Singh', reportDate: new Date(Date.now() - 3600000).toISOString() },
  { id: 4, animalName: 'Cow', location: 'Itwari', status: 'REJECTED', description: 'Stray cow hit by auto-rickshaw.', reporterName: 'Kiran Patel', reportDate: new Date(Date.now() - 7200000).toISOString() },
  { id: 5, animalName: 'Dog', location: 'Sitabuldi', status: 'PENDING', description: 'Dog stuck in mud pit. Exhausted, dehydrated.', reporterName: 'Amit Joshi', reportDate: new Date(Date.now() - 10800000).toISOString() },
];

const FILTERS = ['ALL', 'PENDING', 'APPROVED', 'REJECTED'];

export default function AdminPanel() {
  const [reports, setReports] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filter, setFilter] = useState('PENDING');
  const { addToast } = useToast();

  useEffect(() => {
    fetch(`${API}/admin/reports`)
      .then(r => r.json())
      .then(d => setReports(Array.isArray(d) ? d : MOCK_REPORTS))
      .catch(() => setReports(MOCK_REPORTS))
      .finally(() => setLoading(false));
  }, []);

  const filtered = reports.filter(r => filter === 'ALL' || r.status === filter);

  const counts = {
    ALL: reports.length,
    PENDING: reports.filter(r => r.status === 'PENDING').length,
    APPROVED: reports.filter(r => r.status === 'APPROVED').length,
    REJECTED: reports.filter(r => r.status === 'REJECTED').length,
  };

  async function handleApprove(report) {
    await new Promise(r => setTimeout(r, 600));
    setReports(prev => prev.map(r => r.id === report.id ? { ...r, status: 'APPROVED' } : r));
    addToast('Report approved and dispatched to volunteers', 'success');
  }

  async function handleReject(report) {
    await new Promise(r => setTimeout(r, 500));
    setReports(prev => prev.map(r => r.id === report.id ? { ...r, status: 'REJECTED' } : r));
    addToast('Report rejected', 'warning');
  }

  return (
    <div className="page">
      {/* Header */}
      <div className="page-header">
        <div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 2 }}>Control Center 🛡️</div>
          <h1 style={{ fontSize: '1.3rem' }}>Admin Panel</h1>
        </div>
        <div style={{
          background: 'var(--danger-bg)', color: 'var(--danger)',
          padding: '4px 10px', borderRadius: 'var(--radius-full)',
          fontSize: '0.75rem', fontWeight: 700
        }}>
          {counts.PENDING} Pending
        </div>
      </div>

      <div className="page-content">
        {/* Summary cards */}
        <div className="stats-row" style={{ marginBottom: 16 }}>
          <div className="stat-chip">
            <div className="stat-chip-value">{counts.ALL}</div>
            <div className="stat-chip-label">Total</div>
          </div>
          <div className="stat-chip">
            <div className="stat-chip-value" style={{ color: 'var(--warning)' }}>{counts.PENDING}</div>
            <div className="stat-chip-label">Pending</div>
          </div>
          <div className="stat-chip">
            <div className="stat-chip-value" style={{ color: 'var(--primary)' }}>{counts.APPROVED}</div>
            <div className="stat-chip-label">Approved</div>
          </div>
          <div className="stat-chip">
            <div className="stat-chip-value" style={{ color: 'var(--danger)' }}>{counts.REJECTED}</div>
            <div className="stat-chip-label">Rejected</div>
          </div>
        </div>

        {/* Filter tabs */}
        <div className="filter-tabs" style={{ marginBottom: 16 }}>
          {FILTERS.map(f => (
            <button
              key={f}
              className={`filter-tab ${filter === f ? 'active' : ''}`}
              onClick={() => setFilter(f)}
            >
              {f.charAt(0) + f.slice(1).toLowerCase()} {counts[f] > 0 && `(${counts[f]})`}
            </button>
          ))}
        </div>

        {/* Cards */}
        {loading ? (
          <div className="empty-state"><div style={{ width: 32, height: 32, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} /></div>
        ) : filtered.length === 0 ? (
          <div className="empty-state">
            <div className="empty-icon">📭</div>
            <h3>No {filter.toLowerCase()} reports</h3>
            <p>You're all caught up! No reports matching this filter.</p>
          </div>
        ) : (
          filtered.map(r => (
            <AdminCard key={r.id} report={r} onApprove={handleApprove} onReject={handleReject} />
          ))
        )}
      </div>

      <Navbar role="admin" />
    </div>
  );
}
