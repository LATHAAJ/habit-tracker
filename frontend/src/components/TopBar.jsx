import { useAuth } from '../AuthContext.jsx';
import ThemeToggle from './ThemeToggle.jsx';

export default function TopBar({ tab, onTabChange }) {
  const { email, name, logout } = useAuth();

  return (
    <header className="topbar">
      <div className="brand">
        <span className="brand-mark">🔥</span>
        <span className="brand-name">Habit Tracker</span>
      </div>

      <nav className="tab-nav">
        <button
          type="button"
          className={`tab-nav-btn ${tab === 'dashboard' ? 'active' : ''}`}
          onClick={() => onTabChange('dashboard')}
        >
          Dashboard
        </button>
        <button
          type="button"
          className={`tab-nav-btn ${tab === 'stats' ? 'active' : ''}`}
          onClick={() => onTabChange('stats')}
        >
          Stats
        </button>
      </nav>

      <div className="user-chip">
        <ThemeToggle />
        <span className="user-email">{name || email}</span>
        <button type="button" className="btn btn-ghost" onClick={logout}>
          Log out
        </button>
      </div>
    </header>
  );
}
