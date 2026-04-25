import React, { useState, useRef } from 'react';
import Navbar from '../components/Navbar';
import { useToast } from '../context/ToastContext';

const ANIMAL_TYPES = ['Dog', 'Cat', 'Bird', 'Cow', 'Monkey', 'Other'];
const URGENCY_LEVELS = [
  { value: 'LOW',    label: '🟢 Low – Stable condition' },
  { value: 'MEDIUM', label: '🟡 Medium – Needs help soon' },
  { value: 'HIGH',   label: '🔴 High – Immediate danger' },
];

export default function ReporterForm() {
  const { addToast } = useToast();
  const fileRef = useRef(null);
  const [form, setForm] = useState({
    animalName: '',
    description: '',
    location: '',
    urgency: 'MEDIUM',
    image: null,
  });
  const [errors, setErrors] = useState({});
  const [submitting, setSubmitting] = useState(false);
  const [imagePreview, setImagePreview] = useState(null);
  const [locating, setLocating] = useState(false);

  function set(field, value) {
    setForm(f => ({ ...f, [field]: value }));
    if (errors[field]) setErrors(e => ({ ...e, [field]: null }));
  }

  function validate() {
    const e = {};
    if (!form.animalName) e.animalName = 'Please select animal type';
    if (!form.description || form.description.length < 20)
      e.description = 'Please describe the situation (min. 20 characters)';
    if (!form.location) e.location = 'Location is required';
    return e;
  }

  function handleImage(e) {
    const file = e.target.files[0];
    if (!file) return;
    set('image', file);
    const reader = new FileReader();
    reader.onload = ev => setImagePreview(ev.target.result);
    reader.readAsDataURL(file);
  }

  function handleLocate() {
    setLocating(true);
    navigator.geolocation?.getCurrentPosition(
      pos => {
        set('location', `Lat: ${pos.coords.latitude.toFixed(4)}, Lng: ${pos.coords.longitude.toFixed(4)}`);
        setLocating(false);
        addToast('Location detected!', 'success');
      },
      () => {
        set('location', 'Sadar, Nagpur, Maharashtra');
        setLocating(false);
        addToast('Using approximate location', 'warning');
      },
      { timeout: 5000 }
    );
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setSubmitting(true);
    await new Promise(r => setTimeout(r, 1200));
    addToast('🐾 Rescue request submitted!', 'success');
    setForm({ animalName: '', description: '', location: '', urgency: 'MEDIUM', image: null });
    setImagePreview(null);
    setSubmitting(false);
  }

  return (
    <div className="page">
      <div className="page-header">
        <div>
          <div style={{ fontSize: '0.75rem', color: 'var(--text-muted)', marginBottom: 2 }}>Help an animal 🐾</div>
          <h1 style={{ fontSize: '1.3rem' }}>Report Rescue</h1>
        </div>
      </div>

      <div className="page-content">
        <form onSubmit={handleSubmit} noValidate>

          {/* Animal type */}
          <div className="form-group">
            <label className="form-label">Animal Type *</label>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: 8 }}>
              {ANIMAL_TYPES.map(type => (
                <button
                  key={type}
                  type="button"
                  onClick={() => set('animalName', type)}
                  className={`btn btn-sm ${form.animalName === type ? 'btn-primary' : 'btn-ghost'}`}
                  style={{
                    border: `1.5px solid ${form.animalName === type ? 'var(--primary)' : 'var(--border)'}`,
                    borderRadius: 'var(--radius-full)',
                  }}
                >
                  {animalEmoji(type)} {type}
                </button>
              ))}
            </div>
            {errors.animalName && <span style={{ color: 'var(--danger)', fontSize: '0.75rem' }}>{errors.animalName}</span>}
          </div>

          {/* Description */}
          <div className="form-group">
            <label className="form-label" htmlFor="description">Describe the Situation *</label>
            <textarea
              id="description"
              className={`form-input ${errors.description ? 'error' : ''}`}
              style={{ borderColor: errors.description ? 'var(--danger)' : undefined }}
              placeholder="Describe the animal's condition, injuries, behaviour…"
              value={form.description}
              onChange={e => set('description', e.target.value)}
              rows={4}
            />
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              {errors.description
                ? <span style={{ color: 'var(--danger)', fontSize: '0.75rem' }}>{errors.description}</span>
                : <span />}
              <span style={{ fontSize: '0.7rem', color: 'var(--text-muted)' }}>{form.description.length}/300</span>
            </div>
          </div>

          {/* Urgency */}
          <div className="form-group">
            <label className="form-label">Urgency Level *</label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {URGENCY_LEVELS.map(u => (
                <label key={u.value} style={{
                  display: 'flex', alignItems: 'center', gap: 12, cursor: 'pointer',
                  padding: '10px 14px', borderRadius: 'var(--radius-md)',
                  border: `1.5px solid ${form.urgency === u.value ? 'var(--secondary)' : 'var(--border)'}`,
                  background: form.urgency === u.value ? 'var(--secondary-bg)' : 'var(--bg-card)',
                  transition: 'all var(--transition)'
                }}>
                  <input
                    type="radio"
                    name="urgency"
                    value={u.value}
                    checked={form.urgency === u.value}
                    onChange={() => set('urgency', u.value)}
                    style={{ accentColor: 'var(--secondary)' }}
                  />
                  <span style={{ fontSize: '0.875rem', fontWeight: 500 }}>{u.label}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Location */}
          <div className="form-group">
            <label className="form-label" htmlFor="location">Location *</label>
            <div style={{ display: 'flex', gap: 8 }}>
              <input
                id="location"
                className={`form-input ${errors.location ? 'error' : ''}`}
                style={{ flex: 1, borderColor: errors.location ? 'var(--danger)' : undefined }}
                placeholder="Enter address or use GPS"
                value={form.location}
                onChange={e => set('location', e.target.value)}
              />
              <button
                type="button"
                className="btn btn-outline-primary"
                style={{ flexShrink: 0, padding: '0 14px' }}
                onClick={handleLocate}
                disabled={locating}
              >
                {locating ? '…' : '📍'}
              </button>
            </div>
            {errors.location && <span style={{ color: 'var(--danger)', fontSize: '0.75rem' }}>{errors.location}</span>}
          </div>

          {/* Image Upload */}
          <div className="form-group">
            <label className="form-label">Photo (optional)</label>
            <div
              className={`upload-zone ${imagePreview ? 'has-file' : ''}`}
              onClick={() => fileRef.current?.click()}
            >
              {imagePreview ? (
                <img src={imagePreview} alt="Preview" style={{ width: '100%', maxHeight: 180, objectFit: 'cover', borderRadius: 8 }} />
              ) : (
                <>
                  <div className="upload-zone-icon">📷</div>
                  <strong style={{ fontSize: '0.875rem', color: 'var(--text)' }}>Tap to upload photo</strong>
                  <p>JPG, PNG up to 10MB</p>
                </>
              )}
            </div>
            <input ref={fileRef} type="file" accept="image/*" style={{ display: 'none' }} onChange={handleImage} />
          </div>

          {/* Submit */}
          <button
            type="submit"
            className="btn btn-secondary btn-full"
            style={{ marginTop: 8, padding: '0.875rem', fontSize: '1rem' }}
            disabled={submitting}
          >
            {submitting ? (
              <span style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                <span style={{ width: 18, height: 18, border: '2px solid rgba(255,255,255,0.3)', borderTopColor: '#fff', borderRadius: '50%', animation: 'spin 0.7s linear infinite', display: 'inline-block' }} />
                Submitting…
              </span>
            ) : '🚨 Submit Rescue Request'}
          </button>
        </form>
      </div>

      <Navbar role="reporter" />
    </div>
  );
}

function animalEmoji(type) {
  const map = { Dog: '🐕', Cat: '🐈', Bird: '🐦', Cow: '🐄', Monkey: '🐒', Other: '🐾' };
  return map[type] || '🐾';
}
