export default function BestWorstPanel({ rows }) {
  if (rows.length === 0) {
    return <p className="stats-empty">No habits in range yet.</p>;
  }

  return (
    <div className="best-worst-panel">
      {rows.map((row, index) => (
        <div className="rank-row" key={row.habitId}>
          <div className="rank-row-label">
            <span className="rank-badge">{index === 0 ? '🏆' : index === rows.length - 1 && rows.length > 1 ? '⚠️' : index + 1}</span>
            <span className="rank-name">{row.name}</span>
            {row.category && <span className="tag tag-muted">{row.category}</span>}
          </div>
          <div className="rank-bar-track">
            <div className="rank-bar-fill" style={{ width: `${Math.round(row.completionRate * 100)}%` }} />
          </div>
          <span className="rank-value">{Math.round(row.completionRate * 100)}%</span>
        </div>
      ))}
    </div>
  );
}
