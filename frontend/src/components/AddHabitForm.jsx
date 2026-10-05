import { useState } from 'react';
import { api } from '../api.js';
import { CATEGORIES } from '../categories.js';
import FrequencyPicker from './FrequencyPicker.jsx';

const initialState = {
  name: '',
  description: '',
  category: '',
  frequencyType: 'DAILY',
  targetPerPeriod: 1,
};

export default function AddHabitForm({ onCreated }) {
  const [form, setForm] = useState(initialState);
  const [expanded, setExpanded] = useState(false);
  const [error, setError] = useState('');

  async function handleSubmit(e) {
    e.preventDefault();
    setError('');
    try {
      await api('/api/habits', {
        method: 'POST',
        body: JSON.stringify({
          name: form.name.trim(),
          description: form.description.trim(),
          category: form.category || null,
          frequencyType: form.frequencyType,
          targetPerPeriod: form.frequencyType === 'WEEKLY' ? form.targetPerPeriod : null,
        }),
      });
      setForm(initialState);
      setExpanded(false);
      onCreated();
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <form className="add-habit-card" onSubmit={handleSubmit}>
      <div className="add-habit-row">
        <input
          type="text"
          placeholder="New habit, e.g. Exercise"
          required
          maxLength={120}
          value={form.name}
          onChange={(e) => setForm({ ...form, name: e.target.value })}
          onFocus={() => setExpanded(true)}
        />
        <button type="submit" className="btn btn-primary">
          + Add habit
        </button>
      </div>

      {expanded && (
        <div className="add-habit-details">
          <input
            type="text"
            placeholder="Description (optional)"
            maxLength={500}
            value={form.description}
            onChange={(e) => setForm({ ...form, description: e.target.value })}
          />
          <select value={form.category} onChange={(e) => setForm({ ...form, category: e.target.value })}>
            <option value="">No category</option>
            {CATEGORIES.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
          <FrequencyPicker
            frequencyType={form.frequencyType}
            targetPerPeriod={form.targetPerPeriod}
            onChange={(update) => setForm({ ...form, ...update })}
          />
        </div>
      )}
      {error && <p className="form-error">{error}</p>}
    </form>
  );
}
