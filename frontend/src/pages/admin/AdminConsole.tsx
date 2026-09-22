import React, { useState } from 'react';

export const AdminConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'users' | 'disputes' | 'platform'>('users');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <h1 className="text-3xl font-bold text-gray-900 mb-8">Admin Console</h1>

      <div className="flex border-b border-gray-200 mb-6">
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'users' ? 'border-red-600 text-red-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('users')}
        >
          User Management
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'disputes' ? 'border-red-600 text-red-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('disputes')}
        >
          Dispute Resolution
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'platform' ? 'border-red-600 text-red-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('platform')}
        >
          Platform Analytics
        </button>
      </div>

      {activeTab === 'users' && (
        <div className="bg-white rounded-lg shadow overflow-hidden border border-gray-100">
          <div className="p-4 border-b border-gray-200 flex justify-between items-center">
            <input type="text" placeholder="Search users by ID, name, email..." className="border rounded-md px-3 py-2 w-64" />
          </div>
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Trust Score</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              <tr>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">John Doe</div>
                  <div className="text-sm text-gray-500">john@example.com</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">OWNER</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    ACTIVE
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">4.8 / 5.0</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                  <button className="text-red-600 hover:text-red-900 mr-3">Suspend</button>
                  <button className="text-orange-600 hover:text-orange-900">Revoke KYC</button>
                </td>
              </tr>
              {/* More rows... */}
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'disputes' && (
        <div className="space-y-4">
          <div className="bg-white p-4 rounded-lg shadow border border-gray-100">
            <div className="flex justify-between items-start">
              <div>
                <h3 className="text-lg font-semibold">Dispute #DSP-8992 (Damage Claim)</h3>
                <p className="text-sm text-gray-600 mt-1">Booking: BK-10294 | Against: Renter (Rahul S.)</p>
                <p className="text-sm font-medium text-gray-900 mt-2">Claim: ₹5,000 for broken chair</p>
              </div>
              <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                OPEN (48h left)
              </span>
            </div>
            <div className="mt-4 flex space-x-3">
              <button className="bg-green-600 text-white px-3 py-1.5 text-sm rounded hover:bg-green-700">Approve Claim</button>
              <button className="bg-red-600 text-white px-3 py-1.5 text-sm rounded hover:bg-red-700">Reject Claim</button>
              <button className="bg-gray-100 text-gray-700 px-3 py-1.5 text-sm rounded hover:bg-gray-200">View Evidence</button>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'platform' && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          <div className="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Active Users</h3>
            <p className="text-4xl font-bold text-gray-900 mt-2">2,405</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Total Bookings (MTD)</h3>
            <p className="text-4xl font-bold text-gray-900 mt-2">482</p>
          </div>
          <div className="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Open Disputes</h3>
            <p className="text-4xl font-bold text-red-600 mt-2">14</p>
          </div>
        </div>
      )}
    </div>
  );
};
