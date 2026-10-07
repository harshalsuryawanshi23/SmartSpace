import { useState, useEffect } from "react";

export default function DecoratorMatches({ hallId, slotStart, slotEnd, guestCount }: { hallId: string, slotStart?: string, slotEnd?: string, guestCount?: number }) {
  const [matches, setMatches] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchMatches = async () => {
      setLoading(true);
      try {
        const payload = {
          hallId: parseInt(hallId),
          guestCount: guestCount || 50,
          slotStart: slotStart || null,
          slotEnd: slotEnd || null,
        };
        const res = await fetch('/api/v1/decorator-matches', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        });
        if (res.ok) {
          const data = await res.json();
          setMatches(data);
        }
      } catch (e) {
        console.error(e);
      } finally {
        setLoading(false);
      }
    };
    fetchMatches();
  }, [hallId, slotStart, slotEnd, guestCount]);

  if (loading) return <div className="py-4">Finding decorator matches...</div>;
  if (!matches.length) return null;

  return (
    <div className="mt-8 border-t pt-6">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Matching Decorators for this Hall</h3>
      <div className="grid gap-6 grid-cols-1 md:grid-cols-2">
        {matches.map(m => (
          <div key={m.packageId} className="border rounded-lg p-4 shadow-sm">
            <div className="flex justify-between">
              <h4 className="font-semibold text-lg">{m.businessName}</h4>
              <span className="bg-green-100 text-green-800 px-2 py-1 rounded text-xs font-medium">Score: {m.score}</span>
            </div>
            <p className="text-sm text-gray-600 mb-2">{m.packageName} &middot; {m.distanceKm.toFixed(1)} km away</p>
            
            <div className="mb-3">
              <span className="text-sm font-medium">Est. Price: ₹{m.priceEstimate}</span>
            </div>
            
            <div className="text-sm">
              <ul className="list-disc pl-5 text-gray-600 mb-2">
                {m.reasons.map((r: string, i: number) => (
                  <li key={i}>{r}</li>
                ))}
              </ul>
              {m.warnings && m.warnings.length > 0 && (
                <ul className="list-disc pl-5 text-orange-600 mb-3">
                  {m.warnings.map((w: string, i: number) => (
                    <li key={i}>{w}</li>
                  ))}
                </ul>
              )}
            </div>
            
            <button className="w-full mt-2 bg-white border border-teal-600 text-teal-600 py-2 rounded hover:bg-teal-50">
              Request Quote
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}
