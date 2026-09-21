import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import axios from 'axios';
import { ShieldCheck, ArrowRight, ShieldAlert } from 'lucide-react';
import { Button } from '../../components/ui/Button';
import { Card } from '../../components/ui/Card';

export const KycIntro: React.FC = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleAgreeAndContinue = async () => {
    setLoading(true);
    setError(null);
    try {
      // 1. Record consent
      await axios.post('/api/kyc/consent', {
        policyVersion: '1.0'
      });
      
      // 2. Start KYC session
      const startResponse = await axios.post('/api/kyc/start');
      
      // 3. Redirect to provider page (in our case, the mock provider)
      window.location.href = startResponse.data.redirectUrl;
    } catch (err: any) {
      setError(err.response?.data?.message || 'Something went wrong. Please try again.');
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 flex items-center justify-center p-4">
      <Card className="max-w-md w-full p-6">
        <div className="flex justify-center mb-6">
          <div className="bg-primary-100 p-4 rounded-full">
            <ShieldCheck className="w-12 h-12 text-primary-600" />
          </div>
        </div>
        
        <h1 className="text-2xl font-bold text-center text-gray-900 mb-4">
          Identity Check
        </h1>
        
        <p className="text-gray-600 text-center mb-8">
          We verify your identity once so the watchman can confirm it's really you at the gate.
        </p>

        <div className="space-y-4 mb-8">
          <div className="flex items-start">
            <ShieldCheck className="w-5 h-5 text-green-500 mt-0.5 mr-3 flex-shrink-0" />
            <p className="text-sm text-gray-700">We verify your name against official records.</p>
          </div>
          <div className="flex items-start">
            <ShieldAlert className="w-5 h-5 text-orange-500 mt-0.5 mr-3 flex-shrink-0" />
            <p className="text-sm text-gray-700"><strong>We never store your Aadhaar number</strong> or any biometric data.</p>
          </div>
          <div className="flex items-start">
            <ArrowRight className="w-5 h-5 text-gray-400 mt-0.5 mr-3 flex-shrink-0" />
            <p className="text-sm text-gray-700">You can revoke this consent at any time from your profile.</p>
          </div>
        </div>

        {error && (
          <div className="bg-red-50 text-red-600 p-3 rounded-md text-sm mb-4">
            {error}
          </div>
        )}

        <div className="space-y-3">
          <Button 
            onClick={handleAgreeAndContinue} 
            loading={loading}
            className="w-full"
            size="lg"
          >
            Agree and Continue
          </Button>
          <Button 
            variant="ghost" 
            onClick={() => navigate(-1)} 
            className="w-full"
            disabled={loading}
          >
            Not now
          </Button>
        </div>
      </Card>
    </div>
  );
};
