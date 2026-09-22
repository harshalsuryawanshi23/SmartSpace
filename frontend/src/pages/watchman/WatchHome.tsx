import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

interface Booking {
  id: number;
  bookingRef: string;
  status: string;
  startAt: string;
  endAt: string;
  displayName: string;
}

export default function WatchHome() {
  const [bookings, setBookings] = useState<Booking[]>([]);
  const [loading, setLoading] = useState(true);
  const navigate = useNavigate();

  useEffect(() => {
    const fetchToday = async () => {
      try {
        const res = await fetch('/api/v1/entry/today');
        if (res.ok) {
          const data = await res.json();
          setBookings(data);
        }
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };
    fetchToday();
  }, []);

  return (
    <div className="flex flex-col h-full">
      <h2 className="text-xl font-semibold mb-4 text-gray-800">Today's Bookings</h2>
      
      <div className="flex-grow overflow-auto pb-24">
        {loading ? (
          <div className="animate-pulse flex space-x-4">
            <div className="flex-1 space-y-4 py-1">
              <div className="h-20 bg-gray-300 rounded"></div>
              <div className="h-20 bg-gray-300 rounded"></div>
            </div>
          </div>
        ) : bookings.length === 0 ? (
          <div className="text-gray-500 text-center mt-10">
            No bookings expected today.
          </div>
        ) : (
          <div className="space-y-3">
            {bookings.map((b) => (
              <div key={b.id} className={`p-4 rounded shadow bg-white border-l-4 ${b.status === 'CHECKED_IN' ? 'border-green-500' : 'border-blue-500'}`}>
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="font-bold text-gray-900">{b.displayName}</h3>
                    <p className="text-sm text-gray-500 font-mono">{b.bookingRef}</p>
                  </div>
                  <span className={`px-2 py-1 text-xs font-semibold rounded-full ${b.status === 'CHECKED_IN' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'}`}>
                    {b.status}
                  </span>
                </div>
                <div className="mt-2 text-sm text-gray-600">
                  {new Date(b.startAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})} - 
                  {new Date(b.endAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Prominent Scan QR FAB */}
      <button 
        onClick={() => navigate('/watchman/scan')}
        className="fixed bottom-6 right-6 left-6 bg-indigo-600 text-white py-4 rounded-xl shadow-xl font-bold text-lg flex items-center justify-center hover:bg-indigo-700 active:bg-indigo-800 transition-colors"
      >
        <svg className="w-6 h-6 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 20h4M4 12h4m12 0h.01M5 8h2a1 1 0 001-1V5a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1zm14 0h2a1 1 0 001-1V5a1 1 0 00-1-1h-2a1 1 0 00-1 1v2a1 1 0 001 1zM5 20h2a1 1 0 001-1v-2a1 1 0 00-1-1H5a1 1 0 00-1 1v2a1 1 0 001 1z"></path>
        </svg>
        SCAN QR CODE
      </button>
    </div>
  );
}
