import { useState } from 'react';
import { api } from '../api.js';
import FrequencyPicker from './FrequencyPicker.jsx';

const initialState = {
  name: '',
  description: '',
  category: '',
  frequencyType: 'DAILY',
  targetPerPeriod: 1,
};

export default function AddHabitForm({ categories, onCreated }) {
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
          category: form.category.trim() || null,
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
          placeholder="New habit, e.g. Drink water"
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
          <input
            type="text"
            placeholder="Category (optional)"
            maxLength={60}
            list="category-suggestions"
            value={form.category}
            onChange={(e) => setForm({ ...form, category: e.target.value })}
          />
          <datalist id="category-suggestions">
            {categories.map((c) => (
              <option key={c} value={c} />
            ))}
          </datalist>
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
