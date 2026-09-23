import React, { useState } from 'react';

interface EvidenceUploaderProps {
  bookingId: number;
}

export const EvidenceUploader: React.FC<EvidenceUploaderProps> = ({ bookingId }) => {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState('');

  const handleUpload = async () => {
    if (!file) return;
    setLoading(true);
    setSuccess('');
    
    // In a real app, you'd use FormData to upload the file to cloud storage,
    // and then send the hash/URL to the backend endpoint.
    // We mock this by just calling the checkin-photos endpoint.
    
    try {
      const res = await fetch(\/api/v1/bookings/\/checkin-photos\, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ filename: file.name, hash: 'mock-hash' }),
      });
      
      if (!res.ok) throw new Error('Upload failed');
      setSuccess('Photo uploaded successfully');
      setFile(null);
    } catch (err: any) {
      alert(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleDownloadPdf = () => {
    window.open(\/api/v1/bookings/\/evidence.pdf\, '_blank');
  };

  return (
    <div className="p-4 bg-white rounded shadow">
      <h3 className="text-lg font-bold mb-4">Evidence Pack</h3>
      <div className="space-y-4">
        <div>
          <label className="block text-sm font-medium mb-1">Upload BEFORE / AFTER Photo</label>
          <input type="file" accept="image/*" onChange={(e) => setFile(e.target.files?.[0] || null)} className="border p-2 w-full" />
        </div>
        <button onClick={handleUpload} disabled={!file || loading} className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700 disabled:opacity-50">
          {loading ? 'Uploading...' : 'Upload Photo'}
        </button>
        {success && <p className="text-sm text-green-600">{success}</p>}
        
        <hr />
        <div>
          <button onClick={handleDownloadPdf} className="w-full bg-gray-800 text-white px-4 py-2 rounded flex justify-center items-center gap-2 hover:bg-gray-900">
            <span>📄</span> Download Evidence PDF
          </button>
        </div>
      </div>
    </div>
  );
};
