import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import SlotGrid from '../../components/discovery/SlotGrid';

export default function HallDetail() {
  const { id } = useParams<{ id: string }>();
  const [hall, setHall] = useState<any>(null);
  const [loading, setLoading] = useState(true);

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
        const res = await fetch(/api/v1/halls/ + id);
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
        <div className="px-4 py-5 sm:px-6">
          <h3 className="text-lg leading-6 font-medium text-gray-900">{hall.name}</h3>
          <p className="mt-1 max-w-2xl text-sm text-gray-500">
            {hall.locality}, {hall.city} &middot; ?{hall.basePricePerHour} / hr &middot; {hall.capacityStanding} guests max
          </p>
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
              <SlotGrid slots={mockSlots} onSelect={(time) => console.log('Selected', time)} />
            </div>
          </dl>
        </div>
      </div>
      <div className="mt-6 flex justify-end">
        <button className="bg-teal-600 text-white px-6 py-2 rounded shadow hover:bg-teal-700">
          Book Now
        </button>
      </div>
    </div>
  );
}
