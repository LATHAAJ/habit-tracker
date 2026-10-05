const THEME_KEY = 'habit_tracker_theme';

export function getStoredTheme() {
  return localStorage.getItem(THEME_KEY);
}

export function applyTheme(theme) {
  document.documentElement.dataset.theme = theme;
  localStorage.setItem(THEME_KEY, theme);
}

export function initTheme() {
  const stored = getStoredTheme();
  const theme = stored || (window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light');
  document.documentElement.dataset.theme = theme;
  return theme;
}
