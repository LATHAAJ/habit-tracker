import { useState } from 'react';
import { api } from '../api.js';
import { CATEGORY_KEY_TO_VALUE, categoryColor } from '../categories.js';

function groupByFrequency(habits) {
  const groups = {};
  habits.forEach((habit, index) => {
    const label = habit.frequencyType === 'WEEKLY' ? `${habit.targetPerPeriod}x / week` : 'Daily';
    (groups[label] = groups[label] || []).push({ habit, index });
  });
  return groups;
}

export default function AiHabitPlanner({ onCreated }) {
  const [open, setOpen] = useState(false);
  const [goal, setGoal] = useState('');
  const [loading, setLoading] = useState(false);
  const [plan, setPlan] = useState(null);
  const [selected, setSelected] = useState({});
  const [adding, setAdding] = useState(false);
  const [error, setError] = useState('');

  function close() {
    setOpen(false);
    setPlan(null);
    setGoal('');
    setError('');
  }

  async function handleGenerate(e) {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = await api('/api/ai/habit-plan', {
        method: 'POST',
        body: JSON.stringify({ goal: goal.trim() }),
      });
      setPlan(data);
      const initial = {};
      data.habits.forEach((_, i) => { initial[i] = true; });
      setSelected(initial);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }

  function toggle(index) {
    setSelected((s) => ({ ...s, [index]: !s[index] }));
  }

  async function handleAddAll() {
    setAdding(true);
    setError('');
    try {
      const chosen = plan.habits.filter((_, i) => selected[i]);
      for (const h of chosen) {
        await api('/api/habits', {
          method: 'POST',
          body: JSON.stringify({
            name: h.name,
            description: h.description || '',
            category: CATEGORY_KEY_TO_VALUE[h.category] || null,
            frequencyType: h.frequencyType,
            targetPerPeriod: h.frequencyType === 'WEEKLY' ? h.targetPerPeriod : null,
          }),
        });
      }
      onCreated();
      close();
    } catch (err) {
      setError(err.message);
    } finally {
      setAdding(false);
    }
  }

  if (!open) {
    return (
      <button type="button" className="btn btn-ai" onClick={() => setOpen(true)}>
        ✨ Generate habit plan with AI
      </button>
    );
  }

  const selectedCount = Object.values(selected).filter(Boolean).length;

  return (
    <div className="ai-planner-card">
      <div className="ai-planner-header">
        <h3>✨ AI habit plan</h3>
        <button type="button" className="icon-btn" aria-label="Close" onClick={close}>
          &times;
        </button>
      </div>

      {!plan && (
        <form onSubmit={handleGenerate} className="ai-planner-form">
          <input
            type="text"
            placeholder="e.g. I want to become better at Java backend development"
            value={goal}
            onChange={(e) => setGoal(e.target.value)}
            maxLength={300}
            required
          />
          <button type="submit" className="btn btn-primary" disabled={loading}>
            {loading ? 'Thinking…' : 'Generate plan'}
          </button>
        </form>
      )}

      {plan && (
        <div className="ai-plan-result">
          <p className="ai-plan-goal">🎯 {plan.goalSummary}</p>

          {Object.entries(groupByFrequency(plan.habits)).map(([label, items]) => (
            <div key={label} className="ai-plan-group">
              <h4>{label}</h4>
              {items.map(({ habit, index }) => (
                <label key={index} className="ai-plan-item">
                  <input type="checkbox" checked={!!selected[index]} onChange={() => toggle(index)} />
                  <span className="tag" data-color={categoryColor(CATEGORY_KEY_TO_VALUE[habit.category])}>
                    {CATEGORY_KEY_TO_VALUE[habit.category] || habit.category}
                  </span>
                  <span className="ai-plan-item-name">{habit.name}</span>
                </label>
              ))}
            </div>
          ))}

          <div className="ai-plan-actions">
            <button type="button" className="btn btn-ghost" onClick={() => setPlan(null)}>
              Back
            </button>
            <button type="button" className="btn btn-primary" onClick={handleAddAll} disabled={adding || selectedCount === 0}>
              {adding ? 'Adding…' : `Add ${selectedCount} habit${selectedCount === 1 ? '' : 's'}`}
            </button>
          </div>
        </div>
      )}

      {error && <p className="form-error">{error}</p>}
    </div>
  );
}
