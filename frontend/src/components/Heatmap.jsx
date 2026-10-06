import { useEffect, useState } from 'react';
import { api } from '../api.js';

const HEATMAP_DAYS = 30;

function toIsoDate(d) {
  // Local calendar date, not toISOString()'s UTC date - that silently shifts the date
  // by a day for any timezone ahead of UTC once local midnight crosses into "yesterday UTC".
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

export default function Heatmap({ habitId, refreshKey }) {
  const [completedSet, setCompletedSet] = useState(new Set());

  useEffect(() => {
    let cancelled = false;
    const today = new Date();
    const from = new Date(today);
    from.setDate(from.getDate() - (HEATMAP_DAYS - 1));

    api(`/api/habits/${habitId}/logs?from=${toIsoDate(from)}&to=${toIsoDate(today)}`)
      .then((completed) => {
        if (!cancelled) setCompletedSet(new Set(completed));
      })
      .catch(() => {});

    return () => {
      cancelled = true;
    };
  }, [habitId, refreshKey]);

  const today = new Date();
  const from = new Date(today);
  from.setDate(from.getDate() - (HEATMAP_DAYS - 1));

  const days = [];
  for (let i = 0; i < HEATMAP_DAYS; i++) {
    const d = new Date(from);
    d.setDate(d.getDate() + i);
    const iso = toIsoDate(d);
    days.push({ iso, filled: completedSet.has(iso) });
  }

  return (
    <div className="heatmap">
      {days.map((day) => (
        <div key={day.iso} className={`day ${day.filled ? 'filled' : ''}`} />
      ))}
    </div>
  );
}
