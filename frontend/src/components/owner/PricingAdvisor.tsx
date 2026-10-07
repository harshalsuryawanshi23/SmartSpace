import { useState, useEffect } from "react";

interface PricingSuggestion {
  id: number;
  hall: { name: string };
  dayOfWeek: string;
  startHour: number;
  endHour: number;
  suggestionType: string;
  percentage: number;
  rationaleJson: string;
}

export const PricingAdvisor: React.FC<{ hallId: number }> = ({ hallId }) => {
  const [suggestions, setSuggestions] = useState<PricingSuggestion[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchSuggestions();
  }, [hallId]);

  const fetchSuggestions = async () => {
    try {
      const res = await fetch(`/api/v1/owner/halls/${hallId}/pricing-suggestions`);
      if (res.ok) {
        const data = await res.json();
        setSuggestions(data);
      }
    } catch (err) {
      console.error('Failed to fetch pricing suggestions', err);
    } finally {
      setLoading(false);
    }
  };

  const handleAction = async (id: number, action: 'APPLY' | 'DISMISS') => {
    try {
      await fetch(`/api/v1/owner/halls/${hallId}/pricing-suggestions/${id}/respond`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ action }),
      });
      // Remove from list
      setSuggestions(suggestions.filter(s => s.id !== id));
    } catch (err) {
      console.error('Failed to respond to suggestion', err);
    }
  };

  if (loading) return <div>Loading insights...</div>;
  if (suggestions.length === 0) return <div>No new pricing insights at this time.</div>;

  return (
    <div className="bg-white rounded shadow p-6">
      <h2 className="text-xl font-bold mb-4 flex items-center gap-2">
        <span className="text-blue-500">💡</span> Smart Pricing Advisor
      </h2>
      <div className="space-y-4">
        {suggestions.map(s => {
          const reason = JSON.parse(s.rationaleJson || '{}').reason || 'System identified an opportunity.';
          return (
            <div key={s.id} className="border rounded p-4 flex justify-between items-center bg-gray-50">
              <div>
                <div className="font-semibold flex items-center gap-2">
                  {s.suggestionType === 'SURGE' ? (
                    <span className="bg-orange-100 text-orange-700 px-2 py-0.5 rounded text-xs">SURGE {s.percentage}%</span>
                  ) : (
                    <span className="bg-green-100 text-green-700 px-2 py-0.5 rounded text-xs">DISCOUNT {s.percentage}%</span>
                  )}
                  <span>{s.dayOfWeek}s, {s.startHour}:00 - {s.endHour}:00</span>
                </div>
                <p className="text-sm text-gray-600 mt-1">{reason}</p>
              </div>
              <div className="flex gap-2">
                <button 
                  onClick={() => handleAction(s.id, 'DISMISS')} 
                  className="px-3 py-1 border border-gray-300 rounded text-gray-600 hover:bg-gray-100 text-sm"
                >
                  Dismiss
                </button>
                <button 
                  onClick={() => handleAction(s.id, 'APPLY')} 
                  className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700 text-sm font-medium"
                >
                  Apply Rule
                </button>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
