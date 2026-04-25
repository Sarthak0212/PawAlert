import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import ProfileCard from '../components/ProfileCard';
import StatusBadge from '../components/StatusBadge';
import Navbar from '../components/Navbar';
import { useToast } from '../context/ToastContext';

const API = 'http://localhost:8081/api';
const MOCK = {
  profile: { name: 'Arjun Nair', email: 'arjun.nair@pawalert.in', role: 'VOLUNTEER', phone: '+91 98765 43210', joinDate: '2024-01-15', location: 'Nagpur, MH' },
  stats: { total: 24, active: 2, completed: 22 },
  rating: 4.8,
  rescues: [
    { id: 1, report: { animalName: 'Dog', location: 'Sadar' }, outcome: 'RESCUED', date: '2024-04-20' },
    { id: 2, report: { animalName: 'Cat', location: 'Dharampeth' }, outcome: 'RESCUED', date: '2024-04-18' },
    { id: 3, report: { animalName: 'Bird', location: 'Civil Lines' }, outcome: 'REFERRED', date: '2024-04-10' },
  ]
};

export default function ProfileScreen() {
  const [data, setData] = useState(null);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();
  const { addToast } = useToast();

  useEffect(() => {
    fetch(`${API}/user/profile/2`)
      .then(r => r.json())
      .then(d => setData({ ...MOCK, ...d }))
      .catch(() => setData(MOCK))
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return (
      <div className="page">
        <div className="page-content" style={{ display: 'flex', justifyContent: 'center', marginTop: 80 }}>
          <div style={{ width: 40, height: 40, border: '3px solid var(--border)', borderTopColor: 'var(--primary)', borderRadius: '50%', animation: 'spin 0.8s linear infinite' }} />
        </div>
      </div>
    );
  }

  const isVolunteer = data.profile.role === 'VOLUNTEER';

  return (
    <div className="page">
      {/* Header */}
      <div className="page-header">
        <h1 style={{ fontSize: '1.3rem' }}>My Profile</h1>
        <button className="btn btn-ghost btn-sm" onClick={() => addToast('Edit profile coming soon', 'warning')}>
          ✏️ Edit
        </button>
      </div>

      <div className="page-content">
        {/* Profile card */}
        <ProfileCard profile={data.profile} stats={data.stats} />

        {/* Rating (volunteers only) */}
        {isVolunteer && (
          <div className="card" style={{ marginBottom: 16 }}>
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <div>
                <h3>Volunteer Rating</h3>
                <p style={{ fontSize: '0.8rem', marginTop: 4 }}>Based on {data.stats.completed} rescues</p>
              </div>
              <div style={{ textAlign: 'center' }}>
                <div style={{ fontSize: '2rem', fontWeight: 800, color: 'var(--secondary)' }}>{data.rating}</div>
                <div style={{ color: 'var(--secondary)', fontSize: '1rem' }}>{'★'.repeat(Math.round(data.rating))}{'☆'.repeat(5 - Math.round(data.rating))}</div>
              </div>
            </div>
          </div>
        )}

        {/* Account details */}
        <div className="card" style={{ marginBottom: 16 }}>
          <h3 style={{ marginBottom: 14 }}>Account Details</h3>
          <InfoRow icon="📱" label="Phone" value={data.profile.phone} />
          <div className="divider" />
          <InfoRow icon="📍" label="Location" value={data.profile.location} />
          <div className="divider" />
          <InfoRow icon="📅" label="Member Since" value={new Date(data.profile.joinDate).toLocaleDateString('en-IN', { month: 'long', year: 'numeric' })} />
        </div>

        {/* Recent activity */}
        <div className="card" style={{ marginBottom: 16 }}>
          <h3 style={{ marginBottom: 14 }}>Recent Activity</h3>
          {(isVolunteer ? data.rescues : []).length === 0 ? (
            <div className="empty-state" style={{ padding: '24px 0' }}>
              <div className="empty-icon">📋</div>
              <p>No activity yet</p>
            </div>
          ) : (
            (isVolunteer ? data.rescues : []).map((item, idx) => (
              <div key={item.id}>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '10px 0' }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
                    <div style={{ width: 40, height: 40, borderRadius: 10, background: 'var(--bg-muted)', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: '1.2rem' }}>
                      🐾
                    </div>
                    <div>
                      <div style={{ fontWeight: 600, fontSize: '0.875rem' }}>{item.report.animalName}</div>
                      <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)' }}>📍 {item.report.location}</div>
                    </div>
                  </div>
                  <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: 4 }}>
                    <StatusBadge status={item.outcome} />
                    <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{item.date}</span>
                  </div>
                </div>
                {idx < data.rescues.length - 1 && <div className="divider" style={{ margin: '0' }} />}
              </div>
            ))
          )}
        </div>

        {/* Logout */}
        <button
          className="btn btn-outline-danger btn-full"
          style={{ marginTop: 8 }}
          onClick={() => addToast('Logging out…', 'warning')}
        >
          🚪 Log Out
        </button>
      </div>

      <Navbar role={data.profile.role.toLowerCase()} />
    </div>
  );
}

function InfoRow({ icon, label, value }) {
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '6px 0' }}>
      <span style={{ fontSize: '1.1rem', width: 24, textAlign: 'center' }}>{icon}</span>
      <div>
        <div style={{ fontSize: '0.72rem', color: 'var(--text-muted)', marginBottom: 1 }}>{label}</div>
        <div style={{ fontSize: '0.875rem', fontWeight: 500 }}>{value}</div>
      </div>
    </div>
  );
}
