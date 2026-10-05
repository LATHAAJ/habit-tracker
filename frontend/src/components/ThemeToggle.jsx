import { useState } from 'react';
import { applyTheme, getStoredTheme } from '../theme.js';

export default function ThemeToggle() {
  const [theme, setTheme] = useState(
    document.documentElement.dataset.theme || getStoredTheme() || 'light'
  );

  function toggle() {
    const next = theme === 'dark' ? 'light' : 'dark';
    applyTheme(next);
    setTheme(next);
  }

  return (
    <button type="button" className="icon-btn theme-toggle" onClick={toggle} aria-label="Toggle theme" title="Toggle theme">
      {theme === 'dark' ? '🌙' : '☀️'}
    </button>
  );
}
