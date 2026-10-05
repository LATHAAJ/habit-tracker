export default function CategoryFilter({ categories, selected, onSelect }) {
  if (categories.length === 0) return null;

  return (
    <div className="category-filter">
      <button
        type="button"
        className={`chip ${selected === null ? 'active' : ''}`}
        onClick={() => onSelect(null)}
      >
        All
      </button>
      {categories.map((category) => (
        <button
          key={category}
          type="button"
          className={`chip ${selected === category ? 'active' : ''}`}
          onClick={() => onSelect(category)}
        >
          {category}
        </button>
      ))}
    </div>
  );
}
