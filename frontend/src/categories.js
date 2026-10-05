export const CATEGORIES = [
  { value: '💪 Health', color: 'red' },
  { value: '🧠 Learning', color: 'blue' },
  { value: '💼 Career', color: 'violet' },
  { value: '🧘 Mind', color: 'aqua' },
  { value: '🏠 Personal', color: 'yellow' },
  { value: '💰 Finance', color: 'green' },
];

export function categoryColor(value) {
  return CATEGORIES.find((c) => c.value === value)?.color || 'neutral';
}

// Maps the AI service's SuggestedCategory enum keys to the exact category
// strings used everywhere else in the UI (dropdown, tags, filter chips).
export const CATEGORY_KEY_TO_VALUE = {
  HEALTH: '💪 Health',
  LEARNING: '🧠 Learning',
  CAREER: '💼 Career',
  MIND: '🧘 Mind',
  PERSONAL: '🏠 Personal',
  FINANCE: '💰 Finance',
};
