import { useState } from 'react';
import { api } from '../api.js';
import { categoryColor } from '../categories.js';
import Heatmap from './Heatmap.jsx';

function frequencyLabel(habit) {
  if (habit.frequencyType === 'WEEKLY') {
    return `${habit.targetPerPeriod}x / week`;
  }
  return 'Daily';
}

export default function HabitCard({ habit, onChanged, onDeleted }) {
  const [refreshKey, setRefreshKey] = useState(0);
  const [error, setError] = useState('');
  const [celebrate, setCelebrate] = useState(false);

  async function handleToggle() {
    setError('');
    try {
      const updated = await api(`/api/habits/${habit.id}/toggle`, { method: 'POST' });
      setRefreshKey((k) => k + 1);
      onChanged(updated);
      if (!habit.completedToday && updated.completedToday) {
        setCelebrate(true);
        setTimeout(() => setCelebrate(false), 700);
      }
    } catch (err) {
      setError(err.message);
    }
  }

  async function handleDelete() {
    if (!window.confirm(`Delete "${habit.name}"? This removes all its history.`)) return;
    try {
      await api(`/api/habits/${habit.id}`, { method: 'DELETE' });
      onDeleted(habit.id);
    } catch (err) {
      setError(err.message);
    }
  }

  return (
    <article className={`habit-card ${habit.currentStreak > 0 ? 'on-streak' : ''}`}>
      <div className="habit-card-header">
        <div>
          <h3 className="habit-name">{habit.name}</h3>
          <div className="habit-tags">
            {habit.category && (
              <span className="tag" data-color={categoryColor(habit.category)}>
                {habit.category}
              </span>
            )}
            <span className="tag tag-muted">{frequencyLabel(habit)}</span>
          </div>
        </div>
        <button className="icon-btn delete-btn" title="Delete habit" aria-label="Delete habit" onClick={handleDelete}>
          &times;
        </button>
      </div>

      {habit.description && <p className="habit-description">{habit.description}</p>}

      <div className="streak-row">
        <div className={`streak-badge current ${habit.currentStreak > 0 ? 'glowing' : ''}`}>
          <span className="streak-flame">🔥</span>
          <span className="streak-number">{habit.currentStreak}</span>
          <span className="streak-label">current</span>
        </div>
        <div className="streak-badge longest">
          <span className="streak-trophy">🏆</span>
          <span className="streak-number">{habit.longestStreak}</span>
          <span className="streak-label">best</span>
        </div>
      </div>

      <Heatmap habitId={habit.id} refreshKey={refreshKey} />

      {error && <p className="form-error">{error}</p>}

      <div className="toggle-wrap">
        <button
          type="button"
          className={`btn btn-toggle ${habit.completedToday ? 'done' : ''}`}
          onClick={handleToggle}
        >
          {habit.completedToday ? 'Done today ✓' : 'Mark today done'}
        </button>
        {celebrate && (
          <span className="celebration" aria-hidden="true">
            🎉
          </span>
        )}
      </div>
    </article>
  );
}
