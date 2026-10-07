import { useState, useEffect } from "react";
import { apiClient as api } from "../../lib/apiClient";
import { toast } from "react-hot-toast";

export const AdminConsole: React.FC = () => {
  const [activeTab, setActiveTab] = useState<'users' | 'disputes' | 'platform'>('users');
  const [users, setUsers] = useState<any[]>([]);
  const [disputes, setDisputes] = useState<any[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      setError(null);
      try {
        if (activeTab === 'users') {
          const res = await api.get('/admin/users');
          setUsers(res.data);
        } else if (activeTab === 'disputes') {
          const res = await api.get('/admin/disputes');
          setDisputes(res.data);
        }
      } catch (err: any) {
        if (err.response?.status === 404) {
          setError("Endpoint not yet implemented. Waiting for backend completion.");
          setUsers([]);
          setDisputes([]);
        } else {
          setError("Failed to load data.");
        }
      } finally {
        setLoading(false);
      }
    };
    fetchData();
  }, [activeTab]);

  const handleSuspend = async (userId: number) => {
    try {
      await api.post(`/admin/users/${userId}/suspend?reason=Admin Action`);
      toast.success("User suspended");
      setUsers(users.map(u => u.id === userId ? { ...u, status: 'SUSPENDED' } : u));
    } catch {
      toast.error("Failed to suspend user");
    }
  };

  const handleResolveDispute = async (id: number, resolutionType: string) => {
    try {
      await api.post(`/admin/disputes/${id}/resolve`, {
        resolutionType,
        adminNotes: 'Resolved via admin console',
        amountToRefund: 0,
        amountToCharge: 0
      });
      toast.success("Dispute resolved");
      setDisputes(disputes.filter(d => d.id !== id));
    } catch {
      toast.error("Failed to resolve dispute");
    }
  };

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
          {loading ? (
             <div className="p-8 text-center text-gray-500">Loading users...</div>
          ) : error ? (
             <div className="p-8 text-center text-red-500">{error}</div>
          ) : users.length === 0 ? (
             <div className="p-8 text-center text-gray-500">No users found.</div>
          ) : (
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {users.map((user: any) => (
                  <tr key={user.id}>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="text-sm font-medium text-gray-900">{user.firstName ? `${user.firstName} ${user.lastName || ''}` : user.id}</div>
                      <div className="text-sm text-gray-500">{user.email || 'No email'}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">{user.role || 'UNKNOWN'}</td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${user.status === 'SUSPENDED' ? 'bg-red-100 text-red-800' : 'bg-green-100 text-green-800'}`}>
                        {user.status || 'ACTIVE'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                      {user.status !== 'SUSPENDED' && (
                        <button onClick={() => handleSuspend(user.id)} className="text-red-600 hover:text-red-900 mr-3">Suspend</button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {activeTab === 'disputes' && (
        <div className="space-y-4">
          {loading ? (
             <div className="p-8 text-center bg-white rounded-lg shadow text-gray-500">Loading disputes...</div>
          ) : error ? (
             <div className="p-8 text-center bg-white rounded-lg shadow text-red-500">{error}</div>
          ) : disputes.length === 0 ? (
             <div className="p-8 text-center bg-white rounded-lg shadow text-gray-500">No open disputes.</div>
          ) : (
            disputes.map((dispute: any) => (
              <div key={dispute.id} className="bg-white p-4 rounded-lg shadow border border-gray-100">
                <div className="flex justify-between items-start">
                  <div>
                    <h3 className="text-lg font-semibold">Dispute #{dispute.id} ({dispute.type})</h3>
                    <p className="text-sm text-gray-600 mt-1">Booking: {dispute.bookingRef}</p>
                    <p className="text-sm font-medium text-gray-900 mt-2">Claim: {dispute.description}</p>
                  </div>
                  <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">
                    {dispute.status}
                  </span>
                </div>
                <div className="mt-4 flex space-x-3">
                  <button onClick={() => handleResolveDispute(dispute.id, 'REFUND_RENTER')} className="bg-green-600 text-white px-3 py-1.5 text-sm rounded hover:bg-green-700">Approve Refund</button>
                  <button onClick={() => handleResolveDispute(dispute.id, 'DISMISSED')} className="bg-red-600 text-white px-3 py-1.5 text-sm rounded hover:bg-red-700">Dismiss</button>
                </div>
              </div>
            ))
          )}
        </div>
      )}

      {activeTab === 'platform' && (
        <div className="grid grid-cols-1 gap-4">
          <div className="bg-white p-6 rounded-lg shadow border border-gray-100 text-center">
            <h3 className="text-sm font-medium text-gray-500 uppercase">Analytics Endpoints pending</h3>
            <p className="text-lg text-gray-900 mt-2">Waiting for backend integration...</p>
          </div>
        </div>
      )}
    </div>
  );
};
