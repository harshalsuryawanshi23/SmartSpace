import React, { useEffect, useState } from 'react';

export default function LiveStatusWidget() {
  // In a real app, this would poll or use WebSockets/SSE to get live updates.
  // We mock a fetch to a non-existent endpoint for now, falling back to mock data.
  const [status, setStatus] = useState<any>(null);

  useEffect(() => {
    // Mocking the live status fetch
    const fetchStatus = () => {
      // Simulate API response
      setStatus({
        hallId: 1,
        status: 'OCCUPIED',
        currentHeadcount: 18,
        capacityStanding: 40,
        booking: {
          displayName: 'Priya S.',
          endAt: new Date(Date.now() + 2 * 60 * 60 * 1000).toISOString()
        }
      });
    };
    
    fetchStatus();
    const interval = setInterval(fetchStatus, 30000);
    return () => clearInterval(interval);
  }, []);

  if (!status) return <div className="animate-pulse bg-gray-200 h-32 rounded-xl"></div>;

  const isOccupied = status.status === 'OCCUPIED';

  return (
    <div className={`p-5 rounded-2xl shadow-sm border ${isOccupied ? 'bg-indigo-50 border-indigo-100' : 'bg-white border-gray-100'}`}>
      <div className="flex justify-between items-start mb-4">
        <h3 className="text-sm font-bold text-gray-500 uppercase tracking-wider">Live Status</h3>
        <span className={`flex items-center text-xs font-bold px-2 py-1 rounded-full ${isOccupied ? 'bg-indigo-600 text-white' : 'bg-green-100 text-green-700'}`}>
          <span className={`w-2 h-2 rounded-full mr-1 ${isOccupied ? 'bg-white animate-pulse' : 'bg-green-500'}`}></span>
          {status.status}
        </span>
      </div>

      {isOccupied ? (
        <div>
          <div className="flex items-end justify-between mb-2">
            <div>
              <p className="text-2xl font-extrabold text-gray-900">{status.currentHeadcount} <span className="text-base font-normal text-gray-500">/ {status.capacityStanding} guests</span></p>
            </div>
            <div className="text-right">
              <p className="text-sm font-medium text-gray-900">{status.booking.displayName}</p>
              <p className="text-xs text-gray-500">Ends {new Date(status.booking.endAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</p>
            </div>
          </div>
          
          {/* Progress bar */}
          <div className="w-full bg-indigo-200 rounded-full h-2 mt-2">
            <div 
              className="bg-indigo-600 h-2 rounded-full transition-all duration-1000" 
              style={{ width: `${Math.min(100, (status.currentHeadcount / status.capacityStanding) * 100)}%` }}
            ></div>
          </div>
        </div>
      ) : (
        <div className="py-4 text-center">
          <p className="text-gray-500">Hall is currently empty</p>
        </div>
      )}
    </div>
  );
}
