import { useEffect, useState } from 'react';
import { api } from '../api.js';

const HEATMAP_DAYS = 30;

function toIsoDate(d) {
  return d.toISOString().slice(0, 10);
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
        <div key={day.iso} className={`day ${day.filled ? 'filled' : ''}`} title={day.iso} />
      ))}
    </div>
  );
}
