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
