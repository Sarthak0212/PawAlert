import React from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

const NAV_ITEMS = [
  { path: '/volunteer', icon: '🏠', label: 'Home', roles: ['volunteer'] },
  { path: '/reporter',  icon: '📋', label: 'Home', roles: ['reporter'] },
  { path: '/report',    icon: '➕', label: 'Report', roles: ['reporter'] },
  { path: '/admin',     icon: '⚙️', label: 'Admin', roles: ['admin'] },
  { path: '/profile',   icon: '👤', label: 'Profile', roles: ['volunteer', 'reporter', 'admin'] },
];

export default function Navbar({ role = 'volunteer' }) {
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const items = NAV_ITEMS.filter(n => n.roles.includes(role));

  return (
    <nav className="bottom-nav" role="navigation" aria-label="Main navigation">
      {items.map(item => (
        <button
          key={item.path}
          className={`nav-item ${pathname === item.path ? 'active' : ''}`}
          onClick={() => navigate(item.path)}
          aria-label={item.label}
          aria-current={pathname === item.path ? 'page' : undefined}
        >
          <span style={{ fontSize: '1.2rem' }}>{item.icon}</span>
          {item.label}
        </button>
      ))}
    </nav>
  );
}
