import { useEffect, useState } from 'react';
import { api } from '../api.js';

const HISTORY_WEEKS = 8;

function toIsoDate(d) {
  // Local calendar date, not toISOString()'s UTC date - see Heatmap.jsx for why.
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

function addDays(date, days) {
  const d = new Date(date);
  d.setDate(d.getDate() + days);
  return d;
}

function mondayOf(date) {
  const d = new Date(date);
  const day = d.getDay();
  const diff = day === 0 ? -6 : 1 - day;
  d.setDate(d.getDate() + diff);
  d.setHours(0, 0, 0, 0);
  return d;
}

function formatWeekLabel(d) {
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

function rateOf(completed, target) {
  return target > 0 ? Math.min(1, completed / target) : 0;
}

function getRating(avg) {
  if (avg >= 0.9) return { emoji: '🏆', label: 'Excellent', color: 'green' };
  if (avg >= 0.7) return { emoji: '✅', label: 'Great', color: 'aqua' };
  if (avg >= 0.5) return { emoji: '🙂', label: 'Good', color: 'blue' };
  if (avg >= 0.3) return { emoji: '😐', label: 'Fair', color: 'yellow' };
  return { emoji: '💪', label: 'Needs work', color: 'red' };
}

export default function HabitDetailModal({ habit, onClose }) {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [weeks, setWeeks] = useState([]);
  const [currentWeek, setCurrentWeek] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function load() {
      const today = new Date();
      today.setHours(0, 0, 0, 0);
      const thisWeekStart = mondayOf(today);
      const lastFullWeekStart = addDays(thisWeekStart, -7);
      const oldestWeekStart = addDays(lastFullWeekStart, -7 * (HISTORY_WEEKS - 1));
      const createdAt = new Date(habit.createdAt);
      createdAt.setHours(0, 0, 0, 0);
      const target = habit.frequencyType === 'WEEKLY' ? habit.targetPerPeriod : 7;

      try {
        const completed = await api(
          `/api/habits/${habit.id}/logs?from=${toIsoDate(oldestWeekStart)}&to=${toIsoDate(today)}`
        );
        if (cancelled) return;
        const completedSet = new Set(completed);

        const pastWeeks = [];
        for (let i = 0; i < HISTORY_WEEKS; i++) {
          const start = addDays(oldestWeekStart, 7 * i);
          const end = addDays(start, 6);
          if (end < createdAt) continue; // habit didn't exist yet during this week

          let count = 0;
          for (let d = new Date(start); d <= end; d = addDays(d, 1)) {
            if (completedSet.has(toIsoDate(d))) count++;
          }
          pastWeeks.push({ start, end, count, target, rate: rateOf(count, target) });
        }

        let curCount = 0;
        for (let d = new Date(thisWeekStart); d <= today; d = addDays(d, 1)) {
          if (completedSet.has(toIsoDate(d))) curCount++;
        }

        setWeeks(pastWeeks);
        setCurrentWeek({ count: curCount, target });
      } catch (err) {
        if (!cancelled) setError(err.message);
      } finally {
        if (!cancelled) setLoading(false);
      }
    }

    load();
    return () => {
      cancelled = true;
    };
  }, [habit.id, habit.createdAt, habit.frequencyType, habit.targetPerPeriod]);

  const avgConsistency = weeks.length > 0 ? weeks.reduce((sum, w) => sum + w.rate, 0) / weeks.length : null;
  const rating = avgConsistency !== null ? getRating(avgConsistency) : null;
  const totalDays = weeks.reduce((sum, w) => sum + w.count, 0);

  function handleOverlayClick(e) {
    e.stopPropagation();
    onClose();
  }

  return (
    <div className="modal-overlay" onClick={handleOverlayClick}>
      <div className="modal-card" onClick={(e) => e.stopPropagation()}>
        <div className="modal-header">
          <h3>{habit.name}</h3>
          <button type="button" className="icon-btn" aria-label="Close" onClick={onClose}>
            &times;
          </button>
        </div>

        {loading && <p className="stats-empty">Loading…</p>}
        {error && <p className="form-error">{error}</p>}

        {!loading && !error && (
          <>
            {rating ? (
              <div className="consistency-summary" data-color={rating.color}>
                <span className="consistency-emoji">{rating.emoji}</span>
                <div>
                  <span className="consistency-label">{rating.label}</span>
                  <span className="consistency-value">
                    {Math.round(avgConsistency * 100)}% average consistency · {totalDays} day
                    {totalDays === 1 ? '' : 's'} completed over the last {weeks.length} week
                    {weeks.length === 1 ? '' : 's'}
                  </span>
                </div>
              </div>
            ) : (
              <p className="stats-empty">Not enough history yet — check back after a full week.</p>
            )}

            {currentWeek && (
              <div className="week-history">
                <div className="week-row current-week">
                  <span className="week-label">This week</span>
                  <div className="week-bar-track">
                    <div
                      className="week-bar-fill"
                      style={{ width: `${Math.round(rateOf(currentWeek.count, currentWeek.target) * 100)}%` }}
                    />
                  </div>
                  <span className="week-count">
                    {currentWeek.count}/{currentWeek.target}
                  </span>
                </div>

                {[...weeks].reverse().map((w) => (
                  <div className="week-row" key={toIsoDate(w.start)}>
                    <span className="week-label">{formatWeekLabel(w.start)}</span>
                    <div className="week-bar-track">
                      <div className="week-bar-fill" style={{ width: `${Math.round(w.rate * 100)}%` }} />
                    </div>
                    <span className="week-count">
                      {w.count}/{w.target}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </>
        )}
      </div>
    </div>
  );
}
