import { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { apiClient as api } from '../lib/apiClient';
import { toast } from 'react-hot-toast';
import LiveStatusWidget from '../components/owner/LiveStatusWidget';
import { PrivacyDashboard } from '../components/trust/PrivacyDashboard';
import { OwnerDashboard } from './owner/OwnerDashboard';
import { AdminConsole } from './admin/AdminConsole';

export const Dashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();
    const [disputes, setDisputes] = useState<any[]>([]);
    const [showRaiseDispute, setShowRaiseDispute] = useState(false);
    const [newDispute, setNewDispute] = useState({ bookingRef: '', type: 'DAMAGE_CLAIM', description: '' });

    useEffect(() => {
        if (user && user.role !== 'HALL_OWNER' && user.role !== 'ADMIN') {
            api.get('/disputes/my').then((res: any) => setDisputes(res.data)).catch(() => setDisputes([]));
        }
    }, [user]);

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    const handleRaiseDispute = async (e: React.FormEvent) => {
        e.preventDefault();
        try {
            await api.post('/disputes', newDispute);
            toast.success("Dispute raised successfully");
            setShowRaiseDispute(false);
            api.get('/disputes/my').then((res: any) => setDisputes(res.data)).catch(() => {});
        } catch (err) {
            toast.error("Failed to raise dispute");
        }
    };

    return (
        <div className="min-h-screen bg-gray-100">
            <nav className="bg-white shadow-sm sticky top-0 z-10">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex">
                            <div className="flex-shrink-0 flex items-center">
                                <h1 className="text-xl font-bold text-primary-600">SmartSpace</h1>
                            </div>
                        </div>
                        <div className="flex items-center">
                            <span className="text-gray-700 mr-4 font-medium">Welcome, {user?.firstName}</span>
                            <button
                                onClick={handleLogout}
                                className="bg-primary-50 text-primary-700 px-4 py-2 rounded-md text-sm font-medium hover:bg-primary-100 transition shadow-sm border border-primary-200"
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
                {user?.role === 'HALL_OWNER' ? (
                    <OwnerDashboard />
                ) : user?.role === 'ADMIN' ? (
                    <AdminConsole />
                ) : (
                    <>
                        <div className="px-4 py-6 sm:px-0 grid grid-cols-1 md:grid-cols-3 gap-6">
                            <div className="md:col-span-2">
                                <div className="border border-gray-200 rounded-lg p-6 bg-white shadow-sm">
                                    <div className="flex justify-between items-center mb-4">
                                        <h2 className="text-xl font-semibold text-gray-700">My Disputes</h2>
                                        <button onClick={() => setShowRaiseDispute(true)} className="bg-red-600 text-white px-3 py-1.5 rounded text-sm hover:bg-red-700">
                                            Raise Dispute
                                        </button>
                                    </div>
                                    
                                    {disputes.length === 0 ? (
                                        <div className="text-center py-8 text-gray-500 border-2 border-dashed border-gray-100 rounded-md">
                                            No active disputes.
                                        </div>
                                    ) : (
                                        <div className="space-y-3">
                                            {disputes.map((d: any) => (
                                                <div key={d.id} className="p-3 bg-gray-50 border border-gray-100 rounded-md flex justify-between">
                                                    <div>
                                                        <p className="font-medium text-sm">{d.type} - Booking {d.bookingRef}</p>
                                                        <p className="text-xs text-gray-500 mt-1">{d.description}</p>
                                                    </div>
                                                    <span className="text-xs font-semibold px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full h-min">{d.status}</span>
                                                </div>
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                            
                            {/* Resident Widgets */}
                            <div>
                                <h2 className="text-lg font-bold text-gray-700 mb-4">Live Halls</h2>
                                <LiveStatusWidget />
                            </div>
                        </div>

                        {/* Privacy Section */}
                        <div className="mt-8 px-4 sm:px-0">
                            <PrivacyDashboard />
                        </div>

                        {showRaiseDispute && (
                            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
                                <div className="bg-white p-6 rounded-lg w-full max-w-md shadow-xl">
                                    <h3 className="text-lg font-bold mb-4">Raise a Dispute</h3>
                                    <form onSubmit={handleRaiseDispute}>
                                        <div className="mb-4">
                                            <label className="block text-sm font-medium mb-1">Booking Reference</label>
                                            <input type="text" required value={newDispute.bookingRef} onChange={e => setNewDispute({...newDispute, bookingRef: e.target.value})} className="w-full border rounded p-2" placeholder="e.g. BK-10294" />
                                        </div>
                                        <div className="mb-4">
                                            <label className="block text-sm font-medium mb-1">Dispute Type</label>
                                            <select value={newDispute.type} onChange={e => setNewDispute({...newDispute, type: e.target.value})} className="w-full border rounded p-2">
                                                <option value="DAMAGE_CLAIM">Damage Claim</option>
                                                <option value="MISREPRESENTATION">Misrepresentation</option>
                                                <option value="CANCELLATION_FEE">Cancellation Fee</option>
                                            </select>
                                        </div>
                                        <div className="mb-4">
                                            <label className="block text-sm font-medium mb-1">Description</label>
                                            <textarea required value={newDispute.description} onChange={e => setNewDispute({...newDispute, description: e.target.value})} className="w-full border rounded p-2" rows={3}></textarea>
                                        </div>
                                        <div className="flex justify-end space-x-3">
                                            <button type="button" onClick={() => setShowRaiseDispute(false)} className="px-4 py-2 text-gray-600 hover:bg-gray-100 rounded">Cancel</button>
                                            <button type="submit" className="px-4 py-2 bg-red-600 text-white rounded hover:bg-red-700">Submit</button>
                                        </div>
                                    </form>
                                </div>
                            </div>
                        )}
                    </>
                )}
            </main>
        </div>
    );
};
