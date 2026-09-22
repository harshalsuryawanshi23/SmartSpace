import { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import api from '../../services/api';

export default function CheckoutForm() {
    const { id } = useParams();
    const navigate = useNavigate();
    const [submitting, setSubmitting] = useState(false);
    
    const [checklist, setChecklist] = useState({
        lightsOff: false,
        acOff: false,
        furnitureInPlace: false,
        floorClean: false,
        noDamage: false,
        keysReturned: false
    });
    
    const [notes, setNotes] = useState('');
    const [photos, setPhotos] = useState<string[]>([]);
    
    const handleCheckToggle = (key: keyof typeof checklist) => {
        setChecklist(prev => ({
            ...prev,
            [key]: !prev[key]
        }));
    };
    
    const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files && e.target.files.length > 0) {
            const file = e.target.files[0];
            if (photos.length >= 4) {
                alert("Maximum 4 photos allowed.");
                return;
            }
            
            // Mock read as Base64
            const reader = new FileReader();
            reader.onload = (event) => {
                if (event.target && typeof event.target.result === 'string') {
                    setPhotos([...photos, event.target.result]);
                }
            };
            reader.readAsDataURL(file);
        }
    };
    
    const handleSubmit = async (e: React.FormEvent) => {
        e.preventDefault();
        setSubmitting(true);
        
        try {
            await api.post('/entry/checkout', {
                bookingId: Number(id),
                checklist: JSON.stringify(checklist),
                notes,
                photos
            });
            alert('Checkout successful!');
            navigate('/watchman');
        } catch (err: any) {
            alert(err.response?.data?.message || 'Error during checkout');
            setSubmitting(false);
        }
    };

    const [bookingData, setBookingData] = useState<any>(null);

    useEffect(() => {
        const fetchBooking = async () => {
            try {
                const res = await api.get('/entry/today');
                const booking = res.data.find((b: any) => b.id === Number(id));
                setBookingData(booking);
            } catch (err) {
                console.error(err);
            }
        };
        fetchBooking();
    }, [id]);

    const getOverstayDetails = () => {
        if (!bookingData || !bookingData.endAt) return null;
        const endTime = new Date(bookingData.endAt);
        const now = new Date();
        if (now > endTime) {
            const minutes = Math.floor((now.getTime() - endTime.getTime()) / 60000);
            if (minutes > 0) {
                return minutes;
            }
        }
        return null;
    };

    const overstayMinutes = getOverstayDetails();

    return (
        <div className="p-4 flex flex-col min-h-screen pb-20">
            <h1 className="text-2xl font-bold mb-6 text-gray-800">Checkout Report</h1>
            <div className="mb-4 text-sm text-gray-500">Booking #{id}</div>
            
            {overstayMinutes !== null && overstayMinutes > 0 && (
                <div className="bg-red-50 border border-red-200 p-4 rounded-xl mb-6 shadow-sm">
                    <h3 className="text-red-800 font-bold flex items-center">
                        <svg className="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
                        Overstay Detected
                    </h3>
                    <p className="text-red-700 mt-1 text-sm">
                        This booking has overstayed by <strong>{overstayMinutes} minutes</strong>. 
                        Record this checkout now to finalize the overstay penalty calculation.
                    </p>
                </div>
            )}
            
            <form onSubmit={handleSubmit} className="flex-1 flex flex-col space-y-6">
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 space-y-4">
                    <h2 className="font-semibold text-gray-700">Checklist</h2>
                    
                    {Object.keys(checklist).map((key) => {
                        const isChecked = checklist[key as keyof typeof checklist];
                        return (
                            <label key={key} className="flex items-center space-x-3 cursor-pointer p-2 hover:bg-gray-50 rounded-lg">
                                <div className={`w-6 h-6 rounded-full border-2 flex items-center justify-center ${isChecked ? 'bg-green-500 border-green-500' : 'border-gray-300'}`}>
                                    {isChecked && (
                                        <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
                                        </svg>
                                    )}
                                </div>
                                <span className="text-gray-700 font-medium capitalize">{key.replace(/([A-Z])/g, ' $1').trim()}</span>
                            </label>
                        );
                    })}
                </div>
                
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="font-semibold text-gray-700 mb-2">Notes</h2>
                    <textarea 
                        value={notes}
                        onChange={(e) => setNotes(e.target.value)}
                        className="w-full border border-gray-300 rounded-lg p-3 text-gray-700 focus:ring-2 focus:ring-indigo-500 outline-none"
                        placeholder="Any damage or issues?"
                        rows={3}
                    />
                </div>
                
                <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100">
                    <h2 className="font-semibold text-gray-700 mb-2">Photos (Up to 4)</h2>
                    <div className="flex space-x-2 overflow-x-auto pb-2">
                        {photos.map((photo, idx) => (
                            <div key={idx} className="w-20 h-20 rounded-lg bg-gray-200 flex-shrink-0 bg-cover bg-center" style={{ backgroundImage: `url(${photo})` }}>
                            </div>
                        ))}
                        {photos.length < 4 && (
                            <label className="w-20 h-20 rounded-lg border-2 border-dashed border-gray-300 flex flex-col items-center justify-center text-gray-400 cursor-pointer hover:bg-gray-50 flex-shrink-0">
                                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                                </svg>
                                <span className="text-xs mt-1">Add</span>
                                <input type="file" accept="image/*" className="hidden" onChange={handlePhotoUpload} />
                            </label>
                        )}
                    </div>
                </div>

                <div className="flex-1" />
                
                <button 
                    type="submit" 
                    disabled={submitting}
                    className="w-full bg-indigo-600 text-white font-bold py-4 rounded-2xl shadow-lg hover:bg-indigo-700 active:scale-95 transition disabled:opacity-50"
                >
                    {submitting ? 'Submitting...' : 'Complete Checkout'}
                </button>
            </form>
        </div>
    );
}
