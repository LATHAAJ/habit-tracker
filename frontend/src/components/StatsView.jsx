import { useEffect, useState } from 'react';
import { api } from '../api.js';
import TrendChart from './TrendChart.jsx';
import BestWorstPanel from './BestWorstPanel.jsx';

function toIsoDate(d) {
  return d.toISOString().slice(0, 10);
}

export default function StatsView() {
  const [granularity, setGranularity] = useState('WEEK');
  const [trendPoints, setTrendPoints] = useState([]);
  const [rankRows, setRankRows] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    const periods = granularity === 'WEEK' ? 8 : 6;
    api(`/api/stats/trend?granularity=${granularity}&periods=${periods}`)
      .then(setTrendPoints)
      .catch((err) => setError(err.message));
  }, [granularity]);

  useEffect(() => {
    const to = new Date();
    const from = new Date(to);
    from.setDate(from.getDate() - 29);
    api(`/api/stats/habits?from=${toIsoDate(from)}&to=${toIsoDate(to)}`)
      .then(setRankRows)
      .catch((err) => setError(err.message));
  }, []);

  return (
    <div className="stats-view">
      <section className="stats-card">
        <div className="stats-card-header">
          <h2>Completion trend</h2>
          <div className="granularity-toggle">
            <button
              type="button"
              className={`chip ${granularity === 'WEEK' ? 'active' : ''}`}
              onClick={() => setGranularity('WEEK')}
            >
              Weekly
            </button>
            <button
              type="button"
              className={`chip ${granularity === 'MONTH' ? 'active' : ''}`}
              onClick={() => setGranularity('MONTH')}
            >
              Monthly
            </button>
          </div>
        </div>
        <TrendChart points={trendPoints} />
      </section>

      <section className="stats-card">
        <h2>Best &amp; worst habits (last 30 days)</h2>
        <BestWorstPanel rows={rankRows} />
      </section>

      {error && <p className="form-error">{error}</p>}
    </div>
  );
}
