import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Scanner() {
  const [manualToken, setManualToken] = useState('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const handleScan = async (token: string) => {
    if (isProcessing) return;
    setIsProcessing(true);
    setError(null);
    try {
      const res = await fetch('/api/v1/entry/scan', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ token, deviceId: 'browser-test' })
      });
      const data = await res.json();
      
      // Navigate to verdict page with data
      navigate('/watchman/verdict', { state: { verdictData: data } });
    } catch (err) {
      console.error(err);
      setError('Failed to scan token. Please try again.');
      setIsProcessing(false);
    }
  };

  const handleManualSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (manualToken.trim()) {
      handleScan(manualToken.trim());
    }
  };

  return (
    <div className="flex flex-col items-center h-full max-w-md mx-auto relative">
      <div className="w-full flex justify-between items-center mb-6">
        <button onClick={() => navigate(-1)} className="text-gray-600 p-2">
          <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 19l-7-7 7-7"></path></svg>
        </button>
        <h2 className="text-xl font-bold text-gray-800">Scan Entry QR</h2>
        <div className="w-10"></div>
      </div>

      <div className="bg-black w-full aspect-square rounded-2xl flex items-center justify-center mb-6 relative overflow-hidden shadow-2xl border-4 border-gray-800">
        {/* Placeholder for html5-qrcode since we can't easily mock the camera in this environment */}
        <div className="absolute inset-0 flex flex-col items-center justify-center text-gray-500">
          <svg className="w-16 h-16 mb-4 opacity-50" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 9a2 2 0 012-2h.93a2 2 0 001.664-.89l.812-1.22A2 2 0 0110.07 4h3.86a2 2 0 011.664.89l.812 1.22A2 2 0 0018.07 7H19a2 2 0 012 2v9a2 2 0 01-2 2H5a2 2 0 01-2-2V9z"></path><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 13a3 3 0 11-6 0 3 3 0 016 0z"></path></svg>
          <p>Camera integration placeholder</p>
        </div>
        
        {/* Scanner crosshairs */}
        <div className="absolute w-3/4 h-3/4 border-2 border-dashed border-white/50 rounded-lg"></div>
      </div>

      {error && (
        <div className="w-full bg-red-100 text-red-800 p-3 rounded-lg mb-4 text-center font-medium">
          {error}
        </div>
      )}

      <div className="w-full bg-white p-4 rounded-xl shadow-md mt-auto">
        <p className="text-sm text-gray-500 mb-2 font-medium">Manual Fallback (Testing)</p>
        <form onSubmit={handleManualSubmit} className="flex gap-2">
          <input
            type="text"
            value={manualToken}
            onChange={(e) => setManualToken(e.target.value)}
            placeholder="Paste SS1.xxx token here"
            className="flex-grow p-3 border rounded-lg focus:ring-2 focus:ring-indigo-500 outline-none font-mono text-sm"
          />
          <button 
            type="submit" 
            disabled={isProcessing || !manualToken}
            className="bg-indigo-600 text-white px-6 py-3 rounded-lg font-bold disabled:opacity-50"
          >
            {isProcessing ? '...' : 'GO'}
          </button>
        </form>
      </div>
    </div>
  );
}
