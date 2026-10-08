import React, { useState, useEffect } from "react";
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';

export const OwnerDashboard: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'analytics' | 'bookings' | 'halls' | 'societies'>('analytics');
  
  const [halls, setHalls] = useState<any[]>([]);
  const [societies, setSocieties] = useState<any[]>([]);
  const [analytics, setAnalytics] = useState<any>(null);
  const [loading, setLoading] = useState(true);

  // For Modals
  const [showSocietyModal, setShowSocietyModal] = useState(false);
  const [newSociety, setNewSociety] = useState({ name: '', locality: '', city: '', state: '', pincode: '', docUrl: '' });

  const [showHallModal, setShowHallModal] = useState(false);
  const [newHall, setNewHall] = useState({ societyId: '', name: '', description: '', maxCapacity: 100, category: 'BANQUET', advanceDaysPublic: 30, advanceDaysMember: 60, memberDiscountPercent: 10 });

  useEffect(() => {
      fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
      try {
          setLoading(true);
          const thirtyDaysAgo = new Date();
          thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
          
          const [hallRes, socRes, analyticsRes] = await Promise.all([
              api.get('/v1/owner/halls'),
              api.get('/v1/owner/societies'),
              api.get(`/v1/owner/analytics/summary?start=${thirtyDaysAgo.toISOString()}&end=${new Date().toISOString()}`)
          ]);
          setHalls(hallRes.data);
          setSocieties(socRes.data);
          setAnalytics(analyticsRes.data);
      } catch (error) {
          toast.error('Failed to load dashboard data');
      } finally {
          setLoading(false);
      }
  };

  const submitHall = async (id: string) => {
      try {
          await api.post(`/v1/owner/halls/${id}/submit`);
          toast.success('Hall submitted for approval!');
          fetchDashboardData();
      } catch (error) {
          toast.error('Failed to submit hall');
      }
  };

  const handleAddSociety = async (e: React.FormEvent) => {
      e.preventDefault();
      try {
          await api.post('/v1/owner/societies', { ...newSociety, gpsCoordinates: '0,0' });
          toast.success('Society added successfully');
          setShowSocietyModal(false);
          fetchDashboardData();
      } catch (err) {
          toast.error('Failed to add society');
      }
  };

  const handleAddHall = async (e: React.FormEvent) => {
      e.preventDefault();
      if (!newHall.societyId) {
          toast.error('Please select a society');
          return;
      }
      try {
          await api.post('/v1/owner/halls', { 
              ...newHall, 
              contactPhone: '9999999999', 
              contactEmail: 'owner@example.com',
              amenities: [],
              rules: []
          });
          toast.success('Hall added successfully');
          setShowHallModal(false);
          fetchDashboardData();
      } catch (err) {
          toast.error('Failed to add hall');
      }
  };

  if (loading) {
      return (
          <div className="max-w-7xl mx-auto px-4 py-8">
              <div className="animate-pulse flex flex-col space-y-4">
                  <div className="h-10 bg-gray-200 rounded w-1/4"></div>
                  <div className="h-4 bg-gray-200 rounded w-1/2"></div>
                  <div className="h-64 bg-gray-200 rounded w-full"></div>
              </div>
          </div>
      );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-8">
        <h1 className="text-3xl font-bold text-gray-900">Owner Dashboard</h1>
        <button 
          onClick={() => {
            const thirtyDaysAgo = new Date();
            thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);
            window.location.href = `/api/v1/owner/analytics/export.csv?start=${thirtyDaysAgo.toISOString()}&end=${new Date().toISOString()}`;
          }} 
          className="bg-primary-600 text-white px-4 py-2 rounded-md hover:bg-primary-700 shadow-sm transition"
        >
          Export Analytics (CSV)
        </button>
      </div>

      <div className="flex border-b border-gray-200 mb-6 overflow-x-auto">
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'analytics' ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('analytics')}
        >
          Analytics & Insights
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'bookings' ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('bookings')}
        >
          Manage Bookings
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'societies' ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('societies')}
        >
          My Societies
        </button>
        <button
          className={`py-2 px-4 font-medium text-sm border-b-2 whitespace-nowrap transition-colors ${activeTab === 'halls' ? 'border-primary-600 text-primary-600' : 'border-transparent text-gray-500 hover:text-gray-700'}`}
          onClick={() => setActiveTab('halls')}
        >
          My Halls
        </button>
      </div>

      {activeTab === 'analytics' && (
        <div className="space-y-6">
          {!analytics ? (
              <div className="text-center py-12 text-gray-500">Loading analytics...</div>
          ) : (
              <>
                  <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col justify-center items-center">
                          <h3 className="text-sm font-medium text-gray-500 mb-1">Occupancy Rate</h3>
                          <p className="text-3xl font-bold text-gray-900">{analytics.occupancyPercentage?.toFixed(1) || '0'}%</p>
                      </div>
                      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col justify-center items-center">
                          <h3 className="text-sm font-medium text-gray-500 mb-1">Total Revenue</h3>
                          <p className="text-3xl font-bold text-primary-600">₹{analytics.grossRevenue?.toLocaleString() || '0'}</p>
                      </div>
                      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col justify-center items-center">
                          <h3 className="text-sm font-medium text-gray-500 mb-1">Cancellation Rate</h3>
                          <p className="text-3xl font-bold text-gray-900">{analytics.cancellationRate?.toFixed(1) || '0'}%</p>
                      </div>
                      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col justify-center items-center">
                          <h3 className="text-sm font-medium text-gray-500 mb-1">Peak vs Declared</h3>
                          <p className="text-3xl font-bold text-gray-900">{analytics.avgHeadcountDeclaredRatio?.toFixed(1) || '0'}%</p>
                      </div>
                  </div>
                  
                  <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
                      <h3 className="text-lg font-medium text-gray-900 mb-4">Usage Heatmap (Last 30 Days)</h3>
                      {Object.keys(analytics.usageHeatmap || {}).length === 0 ? (
                          <div className="text-gray-500 text-center py-8">No booking data in this period.</div>
                      ) : (
                          <div className="flex flex-wrap gap-2">
                              {Object.entries(analytics.usageHeatmap || {}).map(([date, count]: [string, any]) => (
                                  <div key={date} className="group relative">
                                      <div 
                                        className={`w-6 h-6 rounded-sm ${count > 2 ? 'bg-primary-700' : count > 0 ? 'bg-primary-400' : 'bg-gray-100'}`}
                                      ></div>
                                      <div className="hidden group-hover:block absolute bottom-full mb-1 left-1/2 -translate-x-1/2 bg-gray-900 text-white text-xs px-2 py-1 rounded whitespace-nowrap z-10">
                                          {date}: {count} bookings
                                      </div>
                                  </div>
                              ))}
                          </div>
                      )}
                  </div>
              </>
          )}
        </div>
      )}

      {activeTab === 'bookings' && (
        <div className="bg-white rounded-lg shadow-sm overflow-hidden border border-gray-100">
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
              <tr className="hover:bg-gray-50 transition">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Nov 24, 2023 (18:00 - 22:00)</td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">Rahul Sharma</td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">
                    CONFIRMED
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">₹12,000</td>
                <td onClick={() => toast('Feature coming soon')} className="px-6 py-4 whitespace-nowrap text-sm text-primary-600 hover:text-primary-900 font-medium cursor-pointer">
                  View Details
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      )}

      {activeTab === 'societies' && (
        <section className="space-y-4">
            <div className="flex justify-end mb-4">
                <button 
                  onClick={() => setShowSocietyModal(true)}
                  className="bg-primary-600 text-white px-4 py-2 rounded shadow-sm hover:bg-primary-700 transition"
                >
                  + Add Society
                </button>
            </div>
            {societies.length === 0 ? (
                <div className="text-gray-500 bg-white p-8 rounded-lg shadow-sm border text-center">
                    <p className="mb-4 text-lg">No societies listed.</p>
                    <button onClick={() => setShowSocietyModal(true)} className="text-primary-600 font-medium hover:underline">Add your first society</button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {societies.map(s => (
                        <div key={s.id} className="p-5 bg-white rounded-lg shadow-sm border border-gray-100 flex flex-col justify-between hover:shadow-md transition">
                            <div>
                                <div className="flex justify-between items-start mb-2">
                                    <h3 className="font-bold text-lg text-gray-900">{s.name}</h3>
                                    <span className={`px-2 py-1 text-xs rounded-full font-bold ${s.verificationStatus === 'APPROVED' ? 'bg-green-100 text-green-800' : 'bg-yellow-100 text-yellow-800'}`}>
                                        {s.verificationStatus || 'PENDING'}
                                    </span>
                                </div>
                                <p className="text-sm text-gray-600 mb-2">{s.locality}, {s.city}</p>
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </section>
      )}

      {activeTab === 'halls' && (
        <section className="space-y-4">
            <div className="flex justify-end mb-4">
                <button 
                  onClick={() => setShowHallModal(true)}
                  className="bg-primary-600 text-white px-4 py-2 rounded shadow-sm hover:bg-primary-700 transition"
                >
                  + Add Hall
                </button>
            </div>
            {halls.length === 0 ? (
                <div className="text-gray-500 bg-white p-8 rounded-lg shadow-sm border text-center">
                    <p className="mb-4 text-lg">No halls listed.</p>
                    <button onClick={() => setShowHallModal(true)} className="text-primary-600 font-medium hover:underline">Add your first hall</button>
                </div>
            ) : (
                <div className="grid grid-cols-1 gap-4">
                    {halls.map(h => (
                        <div key={h.id} className="p-5 bg-white rounded-lg shadow-sm border border-gray-100 flex flex-col sm:flex-row justify-between items-start sm:items-center hover:shadow-md transition">
                            <div className="mb-4 sm:mb-0">
                                <h3 className="font-bold text-lg text-gray-900">{h.name}</h3>
                                <div className="flex items-center space-x-3 mt-1 text-sm">
                                    <span className="text-gray-600">Category: {h.category}</span>
                                    <span className="text-gray-300">|</span>
                                    <span className={`font-medium ${
                                        h.status === 'APPROVED' ? 'text-green-600' :
                                        h.status === 'REJECTED' ? 'text-red-600' :
                                        'text-yellow-600'
                                    }`}>Status: {h.status}</span>
                                </div>
                                {h.status === 'REJECTED' && <p className="text-sm text-red-500 mt-1">Reason: {h.rejectionReason}</p>}
                            </div>
                            <div className="flex space-x-2">
                                <button onClick={() => toast('Feature coming soon')} className="bg-white border border-gray-300 text-gray-700 px-4 py-2 rounded hover:bg-gray-50 transition shadow-sm">
                                    Edit
                                </button>
                                {(h.status === 'DRAFT' || h.status === 'REJECTED') && (
                                    <button 
                                        onClick={() => submitHall(h.id)} 
                                        className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700 transition shadow-sm"
                                    >
                                        Submit for Approval
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </section>
      )}

      {/* Society Modal */}
      {showSocietyModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
              <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
                  <h3 className="text-xl font-bold mb-4">Add New Society</h3>
                  <form onSubmit={handleAddSociety} className="space-y-4">
                      <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Name</label>
                          <input type="text" required value={newSociety.name} onChange={e => setNewSociety({...newSociety, name: e.target.value})} className="w-full border rounded p-2" />
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">Locality</label>
                              <input type="text" required value={newSociety.locality} onChange={e => setNewSociety({...newSociety, locality: e.target.value})} className="w-full border rounded p-2" />
                          </div>
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">City</label>
                              <input type="text" required value={newSociety.city} onChange={e => setNewSociety({...newSociety, city: e.target.value})} className="w-full border rounded p-2" />
                          </div>
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">State</label>
                              <input type="text" required value={newSociety.state} onChange={e => setNewSociety({...newSociety, state: e.target.value})} className="w-full border rounded p-2" />
                          </div>
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">Pincode</label>
                              <input type="text" required value={newSociety.pincode} onChange={e => setNewSociety({...newSociety, pincode: e.target.value})} className="w-full border rounded p-2" />
                          </div>
                      </div>
                      <div className="flex justify-end space-x-3 mt-6">
                          <button type="button" onClick={() => setShowSocietyModal(false)} className="px-4 py-2 text-gray-600 hover:text-gray-800">Cancel</button>
                          <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700">Save</button>
                      </div>
                  </form>
              </div>
          </div>
      )}

      {/* Hall Modal */}
      {showHallModal && (
          <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50">
              <div className="bg-white rounded-lg shadow-xl w-full max-w-md p-6">
                  <h3 className="text-xl font-bold mb-4">Add New Hall</h3>
                  <form onSubmit={handleAddHall} className="space-y-4">
                      <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Select Society</label>
                          <select required value={newHall.societyId} onChange={e => setNewHall({...newHall, societyId: e.target.value})} className="w-full border rounded p-2 bg-white">
                              <option value="">-- Choose Society --</option>
                              {societies.map(s => (
                                  <option key={s.id} value={s.id}>{s.name}</option>
                              ))}
                          </select>
                      </div>
                      <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Hall Name</label>
                          <input type="text" required value={newHall.name} onChange={e => setNewHall({...newHall, name: e.target.value})} className="w-full border rounded p-2" />
                      </div>
                      <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Category</label>
                          <select required value={newHall.category} onChange={e => setNewHall({...newHall, category: e.target.value})} className="w-full border rounded p-2 bg-white">
                              <option value="BANQUET">Banquet</option>
                              <option value="COMMUNITY_CENTER">Community Center</option>
                              <option value="OPEN_GROUND">Open Ground</option>
                          </select>
                      </div>
                      <div>
                          <label className="block text-sm font-medium text-gray-700 mb-1">Max Capacity</label>
                          <input type="number" required min="1" value={newHall.maxCapacity} onChange={e => setNewHall({...newHall, maxCapacity: parseInt(e.target.value)})} className="w-full border rounded p-2" />
                      </div>
                      <div className="grid grid-cols-2 gap-4">
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">Member Advance Days</label>
                              <input type="number" required min="0" value={newHall.advanceDaysMember} onChange={e => setNewHall({...newHall, advanceDaysMember: parseInt(e.target.value)})} className="w-full border rounded p-2" />
                          </div>
                          <div>
                              <label className="block text-sm font-medium text-gray-700 mb-1">Member Discount %</label>
                              <input type="number" required min="0" max="100" value={newHall.memberDiscountPercent} onChange={e => setNewHall({...newHall, memberDiscountPercent: parseInt(e.target.value)})} className="w-full border rounded p-2" />
                          </div>
                      </div>
                      <div className="flex justify-end space-x-3 mt-6">
                          <button type="button" onClick={() => setShowHallModal(false)} className="px-4 py-2 text-gray-600 hover:text-gray-800">Cancel</button>
                          <button type="submit" className="px-4 py-2 bg-primary-600 text-white rounded hover:bg-primary-700">Save</button>
                      </div>
                  </form>
              </div>
          </div>
      )}
    </div>
  );
};
