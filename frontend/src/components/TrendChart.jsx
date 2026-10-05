const WIDTH = 640;
const HEIGHT = 220;
const PADDING = { top: 16, right: 16, bottom: 28, left: 40 };

function formatPeriodLabel(iso) {
  const d = new Date(iso + 'T00:00:00');
  return d.toLocaleDateString(undefined, { month: 'short', day: 'numeric' });
}

export default function TrendChart({ points }) {
  if (points.length === 0) {
    return <p className="stats-empty">Not enough data yet.</p>;
  }

  const plotWidth = WIDTH - PADDING.left - PADDING.right;
  const plotHeight = HEIGHT - PADDING.top - PADDING.bottom;

  const xFor = (i) => PADDING.left + (points.length === 1 ? plotWidth / 2 : (i / (points.length - 1)) * plotWidth);
  const yFor = (rate) => PADDING.top + plotHeight * (1 - rate);

  const linePath = points.map((p, i) => `${i === 0 ? 'M' : 'L'} ${xFor(i)} ${yFor(p.completionRate)}`).join(' ');
  const areaPath =
    `M ${xFor(0)} ${PADDING.top + plotHeight} ` +
    points.map((p, i) => `L ${xFor(i)} ${yFor(p.completionRate)}`).join(' ') +
    ` L ${xFor(points.length - 1)} ${PADDING.top + plotHeight} Z`;

  const gridLines = [0, 0.25, 0.5, 0.75, 1];
  const lastPoint = points[points.length - 1];

  return (
    <svg className="trend-chart" viewBox={`0 0 ${WIDTH} ${HEIGHT}`} role="img" aria-label="Weekly completion rate trend">
      {gridLines.map((g) => (
        <line
          key={g}
          className="chart-gridline"
          x1={PADDING.left}
          x2={WIDTH - PADDING.right}
          y1={yFor(g)}
          y2={yFor(g)}
        />
      ))}
      {gridLines.map((g) => (
        <text key={g} className="chart-axis-label" x={PADDING.left - 8} y={yFor(g)} textAnchor="end" dominantBaseline="middle">
          {Math.round(g * 100)}%
        </text>
      ))}

      <path className="chart-area" d={areaPath} />
      <path className="chart-line" d={linePath} />

      {points.map((p, i) => (
        <circle key={p.periodStart} className="chart-dot" cx={xFor(i)} cy={yFor(p.completionRate)} r={4}>
          <title>
            {formatPeriodLabel(p.periodStart)} – {Math.round(p.completionRate * 100)}%
          </title>
        </circle>
      ))}

      <text
        className="chart-end-label"
        x={xFor(points.length - 1)}
        y={yFor(lastPoint.completionRate) - 10}
        textAnchor="end"
      >
        {Math.round(lastPoint.completionRate * 100)}%
      </text>

      {points.map((p, i) =>
        i % Math.ceil(points.length / 6 || 1) === 0 ? (
          <text key={'x-' + p.periodStart} className="chart-axis-label" x={xFor(i)} y={HEIGHT - 8} textAnchor="middle">
            {formatPeriodLabel(p.periodStart)}
          </text>
        ) : null
      )}
    </svg>
  );
}
