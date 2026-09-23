import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';

export default function DecoratorDashboard() {
  const { user } = useAuth();
  const [enquiries, setEnquiries] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    // In a real app we'd fetch this decorator's enquiries
    // Since we don't have a GET endpoint for this yet, we just render static for demonstration
    setEnquiries([
      {
        id: 1,
        bookingRef: 'BK-1234',
        message: 'Looking for a kids balloon setup for 50 guests.',
        status: 'SENT',
        package: 'Kids Balloon Bash',
        createdAt: new Date().toISOString()
      }
    ]);
  }, []);

  const handleRespond = async (id: number, accept: boolean) => {
    try {
      await fetch('/api/v1/vendor/enquiries/' + id + '/respond', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ accept, quotedPrice: accept ? 5000 : 0, note: accept ? 'Can do it.' : 'Busy.' })
      });
      alert('Responded to enquiry');
    } catch (e) {
      console.error(e);
    }
  };

  return (
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Decorator Dashboard</h1>
      <div className="bg-white shadow overflow-hidden sm:rounded-md">
        <ul className="divide-y divide-gray-200">
          {enquiries.map((e) => (
            <li key={e.id}>
              <div className="px-4 py-4 sm:px-6 flex justify-between items-center">
                <div>
                  <p className="text-sm font-medium text-teal-600 truncate">{e.package}</p>
                  <p className="mt-1 text-sm text-gray-500">Booking: {e.bookingRef} | Status: {e.status}</p>
                  <p className="mt-2 text-sm text-gray-700">"{e.message}"</p>
                </div>
                {e.status === 'SENT' && (
                  <div className="flex space-x-2">
                    <button onClick={() => handleRespond(e.id, true)} className="bg-green-600 text-white px-3 py-1 rounded text-sm hover:bg-green-700">Accept (Quote ₹5000)</button>
                    <button onClick={() => handleRespond(e.id, false)} className="bg-red-600 text-white px-3 py-1 rounded text-sm hover:bg-red-700">Decline</button>
                  </div>
                )}
              </div>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
