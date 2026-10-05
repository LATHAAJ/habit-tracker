export default function FrequencyPicker({ frequencyType, targetPerPeriod, onChange }) {
  return (
    <div className="frequency-picker">
      <div className="frequency-options">
        <label className="radio-pill">
          <input
            type="radio"
            name="frequencyType"
            checked={frequencyType === 'DAILY'}
            onChange={() => onChange({ frequencyType: 'DAILY', targetPerPeriod: 1 })}
          />
          Daily
        </label>
        <label className="radio-pill">
          <input
            type="radio"
            name="frequencyType"
            checked={frequencyType === 'WEEKLY'}
            onChange={() => onChange({ frequencyType: 'WEEKLY', targetPerPeriod: targetPerPeriod || 3 })}
          />
          Weekly
        </label>
      </div>
      {frequencyType === 'WEEKLY' && (
        <label className="target-input">
          Times per week
          <input
            type="number"
            min={1}
            max={7}
            value={targetPerPeriod}
            onChange={(e) => onChange({ frequencyType, targetPerPeriod: Number(e.target.value) })}
          />
        </label>
      )}
    </div>
  );
}
