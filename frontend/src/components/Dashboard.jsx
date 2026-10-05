import { useEffect, useMemo, useState } from 'react';
import { api } from '../api.js';
import AddHabitForm from './AddHabitForm.jsx';
import CategoryFilter from './CategoryFilter.jsx';
import DashboardSummary from './DashboardSummary.jsx';
import HabitCard from './HabitCard.jsx';

export default function Dashboard() {
  const [habits, setHabits] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedCategory, setSelectedCategory] = useState(null);

  async function loadHabits() {
    try {
      const data = await api('/api/habits');
      setHabits(data);
    } catch (_) {
      // api() already handles 401 by clearing the session
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    loadHabits();
  }, []);

  const categories = useMemo(
    () => [...new Set(habits.map((h) => h.category).filter(Boolean))].sort(),
    [habits]
  );

  const visibleHabits = useMemo(
    () => (selectedCategory ? habits.filter((h) => h.category === selectedCategory) : habits),
    [habits, selectedCategory]
  );

  function handleHabitChanged(updated) {
    setHabits((current) => current.map((h) => (h.id === updated.id ? updated : h)));
  }

  function handleHabitDeleted(id) {
    setHabits((current) => current.filter((h) => h.id !== id));
  }

  if (loading) {
    return <div className="dashboard-view" />;
  }

  return (
    <div className="dashboard-view">
      <DashboardSummary habits={habits} />

      <AddHabitForm onCreated={loadHabits} />

      <CategoryFilter categories={categories} selected={selectedCategory} onSelect={setSelectedCategory} />

      {habits.length === 0 ? (
        <div className="empty-state">
          <p>No habits yet — add your first one above.</p>
        </div>
      ) : (
        <div className="habit-grid">
          {visibleHabits.map((habit) => (
            <HabitCard key={habit.id} habit={habit} onChanged={handleHabitChanged} onDeleted={handleHabitDeleted} />
          ))}
        </div>
      )}
    </div>
  );
}
