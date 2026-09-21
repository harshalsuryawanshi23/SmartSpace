import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';

export const OwnerDashboard: React.FC = () => {
    const [halls, setHalls] = useState<any[]>([]);
    const [societies, setSocieties] = useState<any[]>([]);

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            const [hallRes, socRes] = await Promise.all([
                api.get('/v1/owner/halls'),
                api.get('/v1/owner/societies')
            ]);
            setHalls(hallRes.data);
            setSocieties(socRes.data);
        } catch (error) {
            toast.error('Failed to load dashboard');
        }
    };

    const submitHall = async (id: string) => {
        try {
            await api.post(/v1/owner/halls/\/submit);
            toast.success('Hall submitted for approval!');
            fetchDashboardData();
        } catch (error) {
            toast.error('Failed to submit hall');
        }
    };

    return (
        <div className="max-w-5xl mx-auto mt-8 p-6">
            <h1 className="text-3xl font-bold mb-8">Owner Dashboard</h1>

            <section className="mb-12">
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-bold">My Societies</h2>
                    <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Add Society</button>
                </div>
                {societies.length === 0 ? <p className="text-gray-500 bg-white p-4 rounded shadow">No societies listed.</p> : (
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        {societies.map(s => (
                            <div key={s.id} className="p-4 bg-white rounded shadow">
                                <h3 className="font-bold text-lg">{s.name}</h3>
                                <p className="text-sm text-gray-600 mb-2">{s.locality}, {s.city}</p>
                                <span className={px-2 py-1 text-xs rounded font-bold \}>
                                    {s.verificationStatus}
                                </span>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            <section>
                <div className="flex justify-between items-center mb-4">
                    <h2 className="text-2xl font-bold">My Halls</h2>
                    <button className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700">Add Hall</button>
                </div>
                {halls.length === 0 ? <p className="text-gray-500 bg-white p-4 rounded shadow">No halls listed.</p> : (
                    <div className="grid grid-cols-1 gap-4">
                        {halls.map(h => (
                            <div key={h.id} className="p-4 bg-white rounded shadow flex justify-between items-center">
                                <div>
                                    <h3 className="font-bold text-lg">{h.name}</h3>
                                    <p className="text-sm text-gray-600">Status: {h.status}</p>
                                    {h.status === 'REJECTED' && <p className="text-sm text-red-500">Reason: {h.rejectionReason}</p>}
                                </div>
                                <div className="space-x-2">
                                    <button className="bg-gray-200 px-4 py-2 rounded hover:bg-gray-300">Edit</button>
                                    {(h.status === 'DRAFT' || h.status === 'REJECTED') && (
                                        <button onClick={() => submitHall(h.id)} className="bg-green-600 text-white px-4 py-2 rounded hover:bg-green-700">Submit for Approval</button>
                                    )}
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
};
