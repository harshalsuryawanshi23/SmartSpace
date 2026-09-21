import React, { useState, useEffect } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import axios from 'axios';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';

export const KycMockProvider: React.FC = () => {
  const [searchParams] = useSearchParams();
  const navigate = useNavigate();
  const sessionToken = searchParams.get('session');
  
  const [loading, setLoading] = useState(false);
  const [scenario, setScenario] = useState('VERIFIED');
  
  useEffect(() => {
    if (!sessionToken) {
      navigate('/dashboard');
    }
  }, [sessionToken, navigate]);

  const handleSubmit = async () => {
    setLoading(true);
    try {
      // In a real scenario, this form submits to the provider, which then redirects to our callback.
      // We simulate the provider calling our backend or returning to our app which then calls complete.
      
      // Prefix the sessionToken with the scenario so the MockIdentityProvider knows what to return
      const finalSessionId = `${scenario}_${sessionToken}`;
      
      await axios.post('/api/kyc/complete', {
        sessionId: finalSessionId
      });
      
      // Navigate to status page on success
      navigate('/kyc/status');
    } catch (error) {
      console.error('Failed to complete KYC', error);
      setLoading(false);
      alert('Mock KYC completion failed');
    }
  };

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <Card className="max-w-md w-full p-6 border-t-4 border-t-purple-500 shadow-lg bg-white">
        <div className="mb-6 border-b pb-4">
          <h1 className="text-xl font-bold text-gray-800">MOCK Identity Provider</h1>
          <p className="text-sm text-gray-500 font-mono mt-1">DEV ONLY - Do not deploy to production</p>
        </div>
        
        <div className="space-y-4 mb-8">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Select Scenario</label>
            <select 
              className="w-full border-gray-300 rounded-md shadow-sm p-2 border"
              value={scenario}
              onChange={(e) => setScenario(e.target.value)}
            >
              <option value="VERIFIED">Verified (Success)</option>
              <option value="NOMOBILE">Verified (No Mobile Linked)</option>
              <option value="MISMATCH">Name Mismatch</option>
              <option value="FAILED">Failed Validation</option>
            </select>
          </div>
          
          <div className="bg-blue-50 p-3 rounded text-sm text-blue-800">
            Session Token: <span className="font-mono text-xs break-all">{sessionToken}</span>
          </div>
        </div>

        <Button 
          onClick={handleSubmit} 
          loading={loading}
          className="w-full bg-purple-600 hover:bg-purple-700"
          size="lg"
        >
          Submit Mock Result
        </Button>
      </Card>
    </div>
  );
};
