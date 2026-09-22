import React, { useState, useEffect } from 'react';

export const DecoratorConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'profile' | 'packages' | 'inbox'>('profile');
  const [enquiries, setEnquiries] = useState<any[]>([]); // mock

  useEffect(() => {
    // In a real app, fetch enquiries here
    setEnquiries([
      { id: 'enq-123', renterName: 'Rahul', event: 'Birthday Party', status: 'SENT', date: '2023-11-20' },
      { id: 'enq-124', renterName: 'Priya', event: 'Wedding Reception', status: 'ACCEPTED', date: '2023-12-05' }
    ]);
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Decorator Console</h1>
      
      <div className="flex border-b border-gray-200 mb-6">
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'profile' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('profile')}
        >
          My Profile
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'packages' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('packages')}
        >
          Packages
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'inbox' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('inbox')}
        >
          Enquiries Inbox
          {enquiries.filter(e => e.status === 'SENT').length > 0 && (
            <span className="ml-2 bg-red-100 text-red-600 py-0.5 px-2 rounded-full text-xs">
              {enquiries.filter(e => e.status === 'SENT').length}
            </span>
          )}
        </button>
      </div>

      <div className="bg-white rounded-lg shadow p-6 min-h-[400px]">
        {activeTab === 'profile' && (
          <div>
            <h2 className="text-xl font-semibold mb-4">Profile Details</h2>
            <p className="text-gray-600 mb-4">Manage your business profile, service radius, and portfolio.</p>
            {/* Form placeholders */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700">Business Name</label>
                <input type="text" className="mt-1 block w-full rounded-md border-gray-300 shadow-sm p-2 border" defaultValue="Elegant Events" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700">Service Radius (km)</label>
                <input type="number" className="mt-1 block w-full rounded-md border-gray-300 shadow-sm p-2 border" defaultValue="15" />
              </div>
            </div>
            <button className="mt-6 bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">Save Profile</button>
          </div>
        )}

        {activeTab === 'packages' && (
          <div>
            <div className="flex justify-between items-center mb-4">
              <h2 className="text-xl font-semibold">Your Packages</h2>
              <button className="bg-green-600 text-white px-4 py-2 rounded-md hover:bg-green-700">+ Add Package</button>
            </div>
            <div className="border rounded-md divide-y">
              <div className="p-4 flex justify-between items-center">
                <div>
                  <h3 className="font-semibold">Premium Birthday Setup</h3>
                  <p className="text-sm text-gray-500">₹8,000 base • Min 50 guests</p>
                </div>
                <button className="text-blue-600 hover:underline">Edit</button>
              </div>
            </div>
          </div>
        )}

        {activeTab === 'inbox' && (
          <div>
            <h2 className="text-xl font-semibold mb-4">Enquiries</h2>
            <div className="space-y-4">
              {enquiries.map(enq => (
                <div key={enq.id} className="border rounded-md p-4 flex flex-col sm:flex-row justify-between sm:items-center">
                  <div>
                    <h3 className="font-semibold">{enq.event}</h3>
                    <p className="text-sm text-gray-600">From: {enq.renterName} • Date: {enq.date}</p>
                    <span className={`inline-block mt-2 text-xs px-2 py-1 rounded-full font-medium ${enq.status === 'SENT' ? 'bg-yellow-100 text-yellow-800' : 'bg-green-100 text-green-800'}`}>
                      {enq.status}
                    </span>
                  </div>
                  <div className="mt-4 sm:mt-0">
                    {enq.status === 'SENT' ? (
                      <div className="space-x-2">
                        <button className="bg-blue-600 text-white px-3 py-1.5 rounded text-sm hover:bg-blue-700">Respond & Quote</button>
                        <button className="bg-gray-100 text-gray-700 px-3 py-1.5 rounded text-sm hover:bg-gray-200">Decline</button>
                      </div>
                    ) : (
                      <button className="text-blue-600 hover:underline text-sm">View Details</button>
                    )}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
