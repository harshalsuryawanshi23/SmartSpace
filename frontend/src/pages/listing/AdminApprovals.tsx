import React, { useState, useEffect } from 'react';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';

export const AdminApprovals: React.FC = () => {
    const [societies, setSocieties] = useState<any[]>([]);
    const [halls, setHalls] = useState<any[]>([]);

    const fetchData = async () => {
        try {
            const [socRes, hallRes] = await Promise.all([
                api.get('/v1/admin/approvals/societies'),
                api.get('/v1/admin/approvals/halls')
            ]);
            setSocieties(socRes.data);
            setHalls(hallRes.data);
        } catch (error) {
            toast.error('Failed to fetch approval data');
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const handleSocietyReview = async (id: string, approve: boolean) => {
        const reason = approve ? '' : prompt('Reason for rejection:');
        if (!approve && !reason) return;
        try {
            await api.post(`/v1/admin/approvals/societies/${id}`, { approve, reason });
            toast.success(approve ? 'Society Approved' : 'Society Rejected');
            fetchData();
        } catch (error) {
            toast.error('Review failed');
        }
    };

    const handleHallReview = async (id: string, approve: boolean) => {
        const reason = approve ? '' : prompt('Reason for rejection:');
        if (!approve && !reason) return;
        try {
            await api.post(`/v1/admin/approvals/halls/${id}`, { approve, reason });
            toast.success(approve ? 'Hall Approved' : 'Hall Rejected');
            fetchData();
        } catch (error) {
            toast.error('Review failed');
        }
    };

    return (
        <div className="max-w-4xl mx-auto mt-8 p-6 bg-white rounded shadow">
            <h1 className="text-3xl font-bold mb-8">Admin Approvals Console</h1>
            
            <section className="mb-12">
                <h2 className="text-2xl font-bold mb-4 border-b pb-2">Pending Societies</h2>
                {societies.length === 0 ? <p className="text-gray-500">No pending societies.</p> : (
                    <div className="space-y-4">
                        {societies.map((s) => (
                            <div key={s.publicId} className="p-4 border rounded flex justify-between items-center">
                                <div>
                                    <h3 className="font-bold">{s.name}</h3>
                                    <p className="text-sm text-gray-600">{s.addressLine}, {s.locality}, {s.city}</p>
                                </div>
                                <div className="space-x-2">
                                    <button onClick={() => handleSocietyReview(s.publicId, true)} className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600">Approve</button>
                                    <button onClick={() => handleSocietyReview(s.publicId, false)} className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600">Reject</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>

            <section>
                <h2 className="text-2xl font-bold mb-4 border-b pb-2">Pending Halls</h2>
                {halls.length === 0 ? <p className="text-gray-500">No pending halls.</p> : (
                    <div className="space-y-4">
                        {halls.map((h) => (
                            <div key={h.publicId} className="p-4 border rounded flex justify-between items-center">
                                <div>
                                    <h3 className="font-bold">{h.name}</h3>
                                    <p className="text-sm text-gray-600">Society ID: {h.societyId}</p>
                                    <p className="text-sm text-gray-600">Capacity: {h.capacitySeated} seated / {h.capacityStanding} standing</p>
                                </div>
                                <div className="space-x-2">
                                    <button onClick={() => handleHallReview(h.publicId, true)} className="px-3 py-1 bg-green-500 text-white rounded hover:bg-green-600">Approve</button>
                                    <button onClick={() => handleHallReview(h.publicId, false)} className="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600">Reject</button>
                                </div>
                            </div>
                        ))}
                    </div>
                )}
            </section>
        </div>
    );
};
