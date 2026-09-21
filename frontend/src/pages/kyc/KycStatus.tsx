import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ShieldCheck, ShieldAlert, XCircle, Clock } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';

interface KycStatusData {
  status: string;
  verifiedName: string | null;
  maskedId: string | null;
  assuranceLevel: string;
  expiresAt: string | null;
}

export const KycStatus: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [revoking, setRevoking] = useState(false);
  const [data, setData] = useState<KycStatusData | null>(null);

  const fetchStatus = async () => {
    try {
      const res = await axios.get('/api/kyc/status');
      setData(res.data);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchStatus();
  }, []);

  const handleRevoke = async () => {
    if (!window.confirm("Are you sure you want to revoke your KYC consent? You will not be able to book halls until you verify again.")) {
      return;
    }
    setRevoking(true);
    try {
      await axios.post('/api/kyc/revoke');
      await fetchStatus();
    } catch (err) {
      console.error(err);
      alert('Failed to revoke KYC.');
    } finally {
      setRevoking(false);
    }
  };

  if (loading) {
    return <div className="p-8 text-center text-gray-500">Loading identity status...</div>;
  }

  const isVerified = data?.status === 'VERIFIED';
  const isFailed = data?.status === 'FAILED';
  const isNone = data?.status === 'NONE' || data?.status === 'REVOKED';

  return (
    <div className="max-w-xl mx-auto p-4 md:p-8 space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Identity Status</h1>
      
      <Card className="p-6">
        {isVerified && (
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-green-100 mb-4">
              <ShieldCheck className="w-8 h-8 text-green-600" />
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">Identity Verified</h2>
            <p className="text-gray-500 mb-6">Your identity has been successfully verified.</p>
            
            <div className="bg-gray-50 rounded-lg p-4 text-left space-y-3 mb-6 border">
              <div className="grid grid-cols-2 gap-2">
                <span className="text-gray-500 text-sm">Verified Name:</span>
                <span className="font-medium text-gray-900">{data.verifiedName}</span>
                
                <span className="text-gray-500 text-sm">Document ID:</span>
                <span className="font-medium text-gray-900">{data.maskedId}</span>
                
                <span className="text-gray-500 text-sm">Assurance Level:</span>
                <span className={`font-medium ${data.assuranceLevel === 'HIGH' ? 'text-green-600' : 'text-blue-600'}`}>
                  {data.assuranceLevel}
                </span>

                <span className="text-gray-500 text-sm">Valid Until:</span>
                <span className="font-medium text-gray-900">
                  {data.expiresAt ? new Date(data.expiresAt).toLocaleDateString() : 'N/A'}
                </span>
              </div>
            </div>

            <div className="flex gap-3 justify-center">
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>
                Back to Dashboard
              </Button>
              <Button variant="destructive" onClick={handleRevoke} loading={revoking}>
                Revoke Consent
              </Button>
            </div>
          </div>
        )}

        {isFailed && (
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-red-100 mb-4">
              <XCircle className="w-8 h-8 text-red-600" />
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">Verification Failed</h2>
            <p className="text-gray-500 mb-6">We could not verify your identity. Please try again.</p>
            
            <div className="flex gap-3 justify-center">
              <Button onClick={() => navigate('/kyc')}>Try Again</Button>
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>Cancel</Button>
            </div>
          </div>
        )}

        {isNone && (
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-gray-100 mb-4">
              <ShieldAlert className="w-8 h-8 text-gray-400" />
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">Not Verified</h2>
            <p className="text-gray-500 mb-6">You need to verify your identity to book halls.</p>
            
            <div className="flex gap-3 justify-center">
              <Button onClick={() => navigate('/kyc')}>Start Verification</Button>
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>Back</Button>
            </div>
          </div>
        )}

        {data?.status === 'PENDING' && (
          <div className="text-center">
            <div className="inline-flex items-center justify-center w-16 h-16 rounded-full bg-yellow-100 mb-4">
              <Clock className="w-8 h-8 text-yellow-600" />
            </div>
            <h2 className="text-xl font-bold text-gray-900 mb-2">Verification Pending</h2>
            <p className="text-gray-500 mb-6">Your verification is currently in progress.</p>
            
            <div className="flex gap-3 justify-center">
              <Button onClick={() => fetchStatus()}>Refresh Status</Button>
              <Button variant="secondary" onClick={() => navigate('/dashboard')}>Dashboard</Button>
            </div>
          </div>
        )}
      </Card>
    </div>
  );
};
