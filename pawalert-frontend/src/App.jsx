import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastProvider } from './context/ToastContext';
import ToastContainer from './components/Toast';

import VolunteerDashboard from './pages/VolunteerDashboard';
import RescueDetail from './pages/RescueDetail';
import ReporterDashboard from './pages/ReporterDashboard';
import ReporterForm from './pages/ReporterForm';
import AdminPanel from './pages/AdminPanel';
import ProfileScreen from './pages/ProfileScreen';

export default function App() {
  return (
    <ToastProvider>
      <Router>
        <ToastContainer />
        <Routes>
          <Route path="/"          element={<Navigate to="/volunteer" replace />} />
          <Route path="/volunteer" element={<VolunteerDashboard />} />
          <Route path="/rescue/:id" element={<RescueDetail />} />
          <Route path="/reporter"  element={<ReporterDashboard />} />
          <Route path="/report"    element={<ReporterForm />} />
          <Route path="/admin"     element={<AdminPanel />} />
          <Route path="/profile"   element={<ProfileScreen />} />
        </Routes>
      </Router>
    </ToastProvider>
  );
}
