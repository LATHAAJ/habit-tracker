export default function DashboardSummary({ habits }) {
  if (habits.length === 0) return null;

  const doneToday = habits.filter((h) => h.completedToday).length;
  const total = habits.length;
  const percent = total === 0 ? 0 : Math.round((doneToday / total) * 100);

  const topHabit = habits.reduce(
    (best, h) => (h.currentStreak > (best?.currentStreak || 0) ? h : best),
    null
  );

  return (
    <div className="summary-hero">
      <div className="summary-main">
        <span className="summary-label">Today's progress</span>
        <span className="summary-value">
          {doneToday} / {total} done
        </span>
        <div className="summary-bar-track">
          <div className="summary-bar-fill" style={{ width: `${percent}%` }} />
        </div>
      </div>
      {topHabit && topHabit.currentStreak > 0 && (
        <div className="summary-streak">
          <span className="summary-streak-flame">🔥</span>
          <div>
            <span className="summary-streak-value">{topHabit.currentStreak}-day streak</span>
            <span className="summary-streak-name">{topHabit.name}</span>
          </div>
        </div>
      )}
    </div>
  );
}
