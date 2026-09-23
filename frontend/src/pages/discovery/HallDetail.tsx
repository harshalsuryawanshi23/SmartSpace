import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import SlotGrid from '../../components/discovery/SlotGrid';
import { TrustBadge } from '../../components/trust/TrustBadge';
import DecoratorMatches from '../../components/discovery/DecoratorMatches';

export default function HallDetail() {
  const { id } = useParams<{ id: string }>();
  const [hall, setHall] = useState<any>(null);
  const [loading, setLoading] = useState(true);
  const [selectedSlots, setSelectedSlots] = useState<string[]>([]);

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

  // Mock slots for now
  const mockSlots = [
    { time: '09:00', state: 'PAST' as const },
    { time: '09:30', state: 'PAST' as const },
    { time: '10:00', state: 'FREE' as const },
    { time: '10:30', state: 'FREE' as const },
    { time: '11:00', state: 'FREE' as const },
    { time: '11:30', state: 'FREE' as const },
    { time: '12:00', state: 'BOOKED' as const },
    { time: '12:30', state: 'BOOKED' as const },
    { time: '13:00', state: 'BUFFER' as const },
    { time: '13:30', state: 'FREE' as const },
    { time: '14:00', state: 'FREE' as const },
    { time: '14:30', state: 'CLOSED' as const },
  ];

  useEffect(() => {
    const fetchHall = async () => {
      setLoading(true);
      try {
        const res = await fetch(`/api/v1/halls/${id}`);
        if (res.ok) {
          const data = await res.json();
          setHall(data);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    if (id) fetchHall();
  }, [id]);

  if (loading) return <div className="p-8 text-center">Loading...</div>;
  if (!hall) return <div className="p-8 text-center">Hall not found</div>;

  return (
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div className="mb-4">
        <Link to="/search" className="text-teal-600 hover:text-teal-900">&larr; Back to Search</Link>
      </div>
      <div className="bg-white shadow overflow-hidden sm:rounded-lg">
        <div className="px-4 py-5 sm:px-6 flex justify-between items-start">
          <div>
            <h3 className="text-lg leading-6 font-medium text-gray-900">{hall.name}</h3>
            <p className="mt-1 max-w-2xl text-sm text-gray-500">
              {hall.locality}, {hall.city} &middot; ₹{hall.basePricePerHour} / hr &middot; {hall.capacityStanding} guests max
            </p>
          </div>
          <TrustBadge score={mockTrustScore.score} badge={mockTrustScore.badge} componentsJson={mockTrustScore.componentsJson} />
        </div>
        <div className="border-t border-gray-200 px-4 py-5 sm:p-0">
          <dl className="sm:divide-y sm:divide-gray-200">
            <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
              <dt className="text-sm font-medium text-gray-500">Description</dt>
              <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                {hall.description || 'No description provided.'}
              </dd>
            </div>
            <div className="py-4 sm:py-5 sm:grid sm:grid-cols-3 sm:gap-4 sm:px-6">
              <dt className="text-sm font-medium text-gray-500">Amenities</dt>
              <dd className="mt-1 text-sm text-gray-900 sm:mt-0 sm:col-span-2">
                <ul className="list-disc pl-5">
                  {hall.hasAc && <li>Air Conditioning</li>}
                  {hall.hasParking && <li>Parking</li>}
                  {hall.hasKitchen && <li>Kitchen</li>}
                  {hall.hasStage && <li>Stage</li>}
                  {hall.hasPowerBackup && <li>Power Backup</li>}
                  {hall.hasWashroom && <li>Washroom</li>}
                </ul>
              </dd>
            </div>
            <div className="py-4 sm:py-5 sm:px-6">
              <SlotGrid 
                slots={mockSlots} 
                onSelect={(time) => {
                  setSelectedSlots(prev => 
                    prev.includes(time) ? prev.filter(t => t !== time) : [...prev, time]
                  );
                }} 
              />
            </div>
          </dl>
        </div>
      </div>
      
      {selectedSlots.some(t => {
          const [hour] = t.split(':').map(Number);
          return hour >= 22 || hour < 6;
      }) && (
        <div className="mt-4 bg-indigo-50 border-l-4 border-indigo-500 p-4 rounded shadow-sm">
          <div className="flex">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-indigo-400" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd"/>
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-indigo-700">
                <strong>Quiet Hours apply after 10 PM.</strong> No loud music or noise allowed during these slots.
              </p>
            </div>
          </div>
        </div>
      )}

      {id && <DecoratorMatches hallId={id} />}

      <div className="mt-6 flex justify-end">
        <button className="bg-teal-600 text-white px-6 py-2 rounded shadow hover:bg-teal-700 disabled:opacity-50" disabled={selectedSlots.length === 0}>
          Book Now
        </button>
      </div>
    </div>
  );
}
