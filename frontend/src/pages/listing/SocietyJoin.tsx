import React, { useState } from 'react';
import { api } from '../../services/api';
import { toast } from 'react-hot-toast';

export const SocietyJoin: React.FC = () => {
    const [query, setQuery] = useState('');
    const [societies, setSocieties] = useState<any[]>([]);
    const [flatLabel, setFlatLabel] = useState('');
    const [selectedSocietyId, setSelectedSocietyId] = useState<string | null>(null);

    const handleSearch = async () => {
        try {
            const res = await api.get('/v1/societies', { params: { q: query } });
            setSocieties(res.data);
        } catch (error) {
            toast.error('Failed to search societies');
        }
    };

    const handleJoin = async () => {
        if (!selectedSocietyId || !flatLabel) {
            toast.error('Please select a society and enter flat label');
            return;
        }
        try {
            await api.post(/v1/societies//join-requests, { flatLabel });
            toast.success('Join request sent successfully!');
            setSelectedSocietyId(null);
            setFlatLabel('');
        } catch (error) {
            toast.error('Failed to send join request');
        }
    };

    return (
        <div className="max-w-2xl mx-auto mt-8 p-6 bg-white rounded shadow">
            <h1 className="text-2xl font-bold mb-4">Join a Society</h1>
            
            <div className="flex gap-2 mb-6">
                <input 
                    type="text" 
                    placeholder="Search by name or locality..." 
                    value={query}
                    onChange={(e) => setQuery(e.target.value)}
                    className="flex-1 p-2 border rounded"
                />
                <button onClick={handleSearch} className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700">
                    Search
                </button>
            </div>

            <div className="space-y-4">
                {societies.map((s) => (
                    <div key={s.id} className={p-4 border rounded } onClick={() => setSelectedSocietyId(s.id)}>
                        <h3 className="font-bold text-lg">{s.name}</h3>
                        <p className="text-gray-600">{s.locality}, {s.city}</p>
                    </div>
                ))}
            </div>

            {selectedSocietyId && (
                <div className="mt-6 p-4 border-t border-gray-200">
                    <h3 className="font-bold mb-2">Request to Join</h3>
                    <div className="flex gap-2">
                        <input 
                            type="text" 
                            placeholder="Flat / Villa / House Number" 
                            value={flatLabel}
                            onChange={(e) => setFlatLabel(e.target.value)}
                            className="flex-1 p-2 border rounded"
                        />
                        <button onClick={handleJoin} className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">
                            Submit Request
                        </button>
                    </div>
                </div>
            )}
        </div>
    );
};
