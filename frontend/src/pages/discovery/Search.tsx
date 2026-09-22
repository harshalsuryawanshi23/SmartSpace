import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import { TrustBadge } from '../../components/trust/TrustBadge';

export default function Search() {
  const [searchParams] = useSearchParams();
  const [halls, setHalls] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  // Mock trust score for now
  const mockTrustScore = {
    score: 73.9,
    badge: 'TRUSTED' as const,
    componentsJson: JSON.stringify({
      priorMean: 0.7,
      sumWeightedScore: 6.8,
      sumWeight: 8,
      penalty: 0.0667,
      ownerCancellations: 1
    })
  };

  useEffect(() => {
    // Basic mock fetch for MVP, assume lat/lng are 18.5, 73.8
    const fetchHalls = async () => {
      setLoading(true);
      try {
        const params = new URLSearchParams();
        params.append('lat', '18.53');
        params.append('lng', '73.89');
        if (searchParams.get('guests')) {
          params.append('guests', searchParams.get('guests')!);
        }
        
        const res = await fetch('/api/v1/halls/search?' + params.toString());
        if (res.ok) {
          const data = await res.json();
          setHalls(data);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchHalls();
  }, [searchParams]);

  return (
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8 flex">
      <div className="w-1/4 pr-4">
        <h2 className="text-lg font-medium text-gray-900 mb-4">Filters</h2>
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700">Price</label>
            <input type="range" className="w-full mt-1" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">Amenities</label>
            <div className="mt-2 space-y-2">
              {['AC', 'Parking', 'Kitchen'].map((am) => (
                <div key={am} className="flex items-center">
                  <input type="checkbox" className="h-4 w-4 text-teal-600 border-gray-300 rounded" />
                  <label className="ml-2 block text-sm text-gray-900">{am}</label>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>
      <div className="w-3/4">
        <h2 className="text-lg font-medium text-gray-900 mb-4">Results ({halls.length})</h2>
        {loading ? (
          <p>Loading...</p>
        ) : (
          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
            {halls.map((hall) => (
              <div key={hall.id} className="bg-white overflow-hidden shadow rounded-lg border border-gray-200">
                <div className="px-4 py-5 sm:p-6">
                  <div className="flex justify-between items-start">
                    <h3 className="text-lg leading-6 font-medium text-gray-900">{hall.name}</h3>
                    <TrustBadge score={mockTrustScore.score} badge={mockTrustScore.badge} componentsJson={mockTrustScore.componentsJson} showLabel={false} />
                  </div>
                  <div className="mt-2 max-w-xl text-sm text-gray-500">
                    <p>{hall.locality}, {hall.city}</p>
                    <p>?{hall.basePricePerHour} / hr</p>
                    <p>Up to {hall.capacityStanding} guests</p>
                  </div>
                  <div className="mt-3 text-sm">
                    <Link to={`/halls/${hall.id}`} className="font-medium text-teal-600 hover:text-teal-500">
                      View Details &rarr;
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
