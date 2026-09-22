import React, { useState } from 'react';

interface RatingFormProps {
    bookingId: number;
    subjectId: number;
    raterSide: 'RENTER' | 'HALL_SIDE';
    onClose: () => void;
    onSubmitSuccess: () => void;
}

export const RatingForm: React.FC<RatingFormProps> = ({ bookingId, subjectId, raterSide, onClose, onSubmitSuccess }) => {
    const isRenter = raterSide === 'RENTER';
    
    const initialDimensions = isRenter 
        ? { cleanliness: 5, accuracy: 5, facilities: 5, helpfulness: 5 }
        : { punctuality: 5, care: 5, conduct: 5 };

    const [dimensions, setDimensions] = useState<Record<string, number>>(initialDimensions);
    const [comment, setComment] = useState('');
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    const handleStarClick = (dim: string, val: number) => {
        setDimensions(prev => ({ ...prev, [dim]: val }));
    };

    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setLoading(true);
        setError(null);

        // Average stars
        const vals = Object.values(dimensions);
        const avgStars = Math.round(vals.reduce((a, b) => a + b, 0) / vals.length);

        const payload = {
            stars: avgStars,
            dimensions,
            comment
        };

        const endpoint = isRenter 
            ? `/api/bookings/${bookingId}/ratings/hall?hallId=${subjectId}` 
            : `/api/bookings/${bookingId}/ratings/renter?renterId=${subjectId}`;

        try {
            // Mock API call
            console.log("Submitting rating to", endpoint, payload);
            // const res = await fetch(endpoint, { method: 'POST', body: JSON.stringify(payload), headers: {'Content-Type': 'application/json'} });
            // if (!res.ok) throw new Error("Failed to submit");
            
            // Simulating successful submit
            setTimeout(() => {
                onSubmitSuccess();
                onClose();
            }, 500);

        } catch (err: any) {
            setError(err.message || 'Something went wrong');
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 p-4">
            <div className="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden">
                <div className="px-6 py-4 border-b flex justify-between items-center">
                    <h2 className="text-lg font-bold">
                        {isRenter ? 'Rate the Hall' : 'Rate the Guest'}
                    </h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-800">✕</button>
                </div>
                
                <form onSubmit={handleSubmit} className="p-6">
                    {error && <div className="mb-4 text-sm text-red-600 bg-red-50 p-2 rounded">{error}</div>}
                    
                    <div className="space-y-4 mb-6">
                        {Object.keys(dimensions).map(dim => (
                            <div key={dim} className="flex items-center justify-between">
                                <span className="capitalize font-medium text-gray-700">{dim}</span>
                                <div className="flex space-x-1">
                                    {[1, 2, 3, 4, 5].map(star => (
                                        <button
                                            key={star}
                                            type="button"
                                            onClick={() => handleStarClick(dim, star)}
                                            className={`text-2xl focus:outline-none ${star <= dimensions[dim] ? 'text-yellow-400' : 'text-gray-300'}`}
                                        >
                                            ★
                                        </button>
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>

                    <div className="mb-6">
                        <label className="block text-sm font-medium text-gray-700 mb-2">Additional Comments (Optional)</label>
                        <textarea 
                            className="w-full border rounded-md p-2 focus:ring focus:ring-blue-200 focus:border-blue-500" 
                            rows={3}
                            placeholder="Share your experience..."
                            value={comment}
                            onChange={e => setComment(e.target.value)}
                        />
                    </div>

                    <div className="flex justify-end space-x-3">
                        <button 
                            type="button" 
                            onClick={onClose}
                            className="px-4 py-2 border rounded-md text-gray-700 hover:bg-gray-50"
                            disabled={loading}
                        >
                            Cancel
                        </button>
                        <button 
                            type="submit" 
                            className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50 flex items-center"
                            disabled={loading}
                        >
                            {loading ? 'Submitting...' : 'Submit Rating'}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};
