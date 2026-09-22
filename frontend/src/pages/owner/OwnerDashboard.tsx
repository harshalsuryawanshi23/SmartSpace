import React, { useState } from 'react';

export const OwnerDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'analytics' | 'bookings'>('analytics');

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Owner Dashboard</h1>
        <button className="bg-blue-600 text-white px-4 py-2 rounded-md hover:bg-blue-700">
          Export Analytics (CSV)
        </button>
      </div>

      <div className="flex border-b border-gray-200 mb-6">
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'analytics' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('analytics')}
        >
          Analytics & Insights
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 ${activeTab === 'bookings' ? 'border-blue-600 text-blue-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('bookings')}
        >
          Manage Bookings
        </button>
      </div>

      {activeTab === 'analytics' && (
        <div className="space-y-6">
          {/* Key Metrics */}
          <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
            <div className="bg-white p-4 rounded-lg shadow border border-gray-100">
              <p className="text-sm text-gray-500 font-medium">Occupancy %</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">78.5%</p>
              <p className="text-xs text-green-600 mt-1">↑ 5% vs last month</p>
            </div>
            <div className="bg-white p-4 rounded-lg shadow border border-gray-100">
              <p className="text-sm text-gray-500 font-medium">Gross Revenue</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">₹4,25,000</p>
            </div>
            <div className="bg-white p-4 rounded-lg shadow border border-gray-100">
              <p className="text-sm text-gray-500 font-medium">Cancellation Rate</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">4.2%</p>
            </div>
            <div className="bg-white p-4 rounded-lg shadow border border-gray-100">
              <p className="text-sm text-gray-500 font-medium">Total Bookings</p>
              <p className="text-2xl font-bold text-gray-900 mt-1">142</p>
            </div>
          </div>

          {/* Usage Heatmap (Mocked with CSS grid) */}
          <div className="bg-white p-6 rounded-lg shadow border border-gray-100">
            <h3 className="text-lg font-semibold text-gray-800 mb-4">Usage Heatmap (Last 8 Weeks)</h3>
            <div className="overflow-x-auto">
              <div className="min-w-[600px]">
                <div className="grid grid-cols-8 gap-1 mb-1">
                  <div></div>
                  {['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'].map(d => (
                    <div key={d} className="text-center text-xs font-medium text-gray-500">{d}</div>
                  ))}
                </div>
                {[
                  '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00'
                ].map((time, rowIdx) => (
                  <div key={time} className="grid grid-cols-8 gap-1 mb-1">
                    <div className="text-xs text-gray-500 flex items-center">{time}</div>
                    {[0,1,2,3,4,5,6].map(colIdx => {
                      // Mock intensity
                      const isWeekend = colIdx >= 5;
                      const isEvening = rowIdx >= 4;
                      let intensity = 'bg-blue-100';
                      if (isWeekend && isEvening) intensity = 'bg-blue-600';
                      else if (isWeekend || isEvening) intensity = 'bg-blue-400';
                      else if (rowIdx === 2) intensity = 'bg-blue-300'; // lunch time
                      
                      return <div key={`${rowIdx}-${colIdx}`} className={`h-8 rounded ${intensity}`} title={`${time} ${colIdx}`}></div>;
                    })}
                  </div>
                ))}
              </div>
            </div>
            <div className="flex items-center justify-end mt-4 text-xs text-gray-500 space-x-2">
              <span>Less</span>
              <div className="flex space-x-1">
                <div className="w-3 h-3 bg-blue-100"></div>
                <div className="w-3 h-3 bg-blue-300"></div>
                <div className="w-3 h-3 bg-blue-400"></div>
                <div className="w-3 h-3 bg-blue-600"></div>
              </div>
              <span>More</span>
            </div>
          </div>
        </div>
      )}

      {activeTab === 'bookings' && (
        <div className="bg-white rounded-lg shadow overflow-hidden border border-gray-100">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Renter</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              <tr>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Nov 24, 2023 (18:00 - 22:00)</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Rahul Sharma</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    CONFIRMED
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">₹12,000</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-blue-600 hover:text-blue-900">
                  <button>View Details</button>
                </td>
              </tr>
              {/* More rows... */}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};
