import { useState } from 'react';
import { api } from '../api.js';
import { CATEGORIES } from '../categories.js';
import FrequencyPicker from './FrequencyPicker.jsx';

export default function EditHabitModal({ habit, onClose, onSaved }) {
  const [form, setForm] = useState({
    name: habit.name,
    description: habit.description || '',
    category: habit.category || '',
    frequencyType: habit.frequencyType,
    targetPerPeriod: habit.targetPerPeriod,
  });
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');

  function handleOverlayClick(e) {
    e.stopPropagation();
    onClose();
  }

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    setSaving(true);
    try {
      const updated = await api(`/api/habits/${habit.id}`, {
        method: 'PUT',
        body: JSON.stringify({
          name: form.name.trim(),
          description: form.description.trim(),
          category: form.category || null,
          frequencyType: form.frequencyType,
          targetPerPeriod: form.frequencyType === 'WEEKLY' ? form.targetPerPeriod : null,
        }),
      });
      onSaved(updated);
    } catch (err) {
      setError(err.message);
      setSaving(false);
    }
  }

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>Edit habit</h3>
          <button type="button" className="icon-btn" aria-label="Close" onClick={onClose}>
            &times;
          </button>
        </div>

        <form className="auth-form" onSubmit={handleSubmit}>
          <label>
            Name
            <input
              type="text"
              required
              maxLength={120}
              value={form.name}
              onChange={(e) => setForm({ ...form, name: e.target.value })}
            />
          </label>
          <label>
            Description
            <input
              type="text"
              maxLength={500}
              value={form.description}
              onChange={(e) => setForm({ ...form, description: e.target.value })}
            />
          </label>
          <label>
            Category
            <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
              <option value="">No category</option>
              {CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>
                  {c.value}
                </option>
              ))}
            </select>
          </label>
          <FrequencyPicker
            frequencyType={form.frequencyType}
            targetPerPeriod={form.targetPerPeriod}
            onChange={(update) => setForm({ ...form, ...update })}
          />

          {error && <p className="form-error">{error}</p>}

          <div className="modal-actions">
            <button type="button" className="btn btn-ghost" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={saving}>
              {saving ? 'Saving…' : 'Save changes'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
