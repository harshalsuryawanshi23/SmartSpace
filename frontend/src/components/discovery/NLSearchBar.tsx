import { useState } from "react";

interface ParsedFilters {
  eventType?: string;
  guests?: number;
  date?: string;
  startTime?: string;
  durationMinutes?: number;
  amenities?: string[];
}

interface NLSearchBarProps {
  onFiltersParsed: (filters: ParsedFilters) => void;
}

export const NLSearchBar: React.FC<NLSearchBarProps> = ({ onFiltersParsed }) => {
  const [query, setQuery] = useState('');
  const [loading, setLoading] = useState(false);

  const handleSearch = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!query.trim()) return;

    try {
      setLoading(true);
      const res = await fetch('/api/v1/search/parse-brief', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ text: query }),
      });
      if (res.ok) {
        const data = await res.json();
        onFiltersParsed(data);
        setQuery(''); // clear query after parsing
      }
    } catch (err) {
      console.error('Failed to parse natural language brief', err);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-sm mb-4 border">
      <form onSubmit={handleSearch} className="flex gap-2">
        <input
          type="text"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="E.g., Birthday party for 50 people tomorrow at 6pm"
          className="flex-1 p-3 border rounded-md focus:outline-none focus:border-primary"
          disabled={loading}
        />
        <button
          type="submit"
          className="bg-primary text-white px-6 py-3 rounded-md hover:bg-primary-dark font-medium whitespace-nowrap"
          disabled={loading}
        >
          {loading ? 'Parsing...' : 'Magic Search'}
        </button>
      </form>
    </div>
  );
};
