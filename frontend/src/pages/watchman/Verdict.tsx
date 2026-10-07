import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { Volume2, VolumeX } from 'lucide-react';
import OtpPad from '../../components/watchman/OtpPad';

export default function Verdict() {
  const location = useLocation();
  const navigate = useNavigate();
  const { t, i18n } = useTranslation();
  const data = location.state?.verdictData;

  const [isProcessingOtp, setIsProcessingOtp] = useState(false);
  const [otpError, setOtpError] = useState<string | null>(null);
  const [isMuted, setIsMuted] = useState(false);
  
  // If we just verified OTP, we transition the local state to GO
  const [localVerdict, setLocalVerdict] = useState<any>(null);

  const currentData = localVerdict || data;
  
  useEffect(() => {
    if (!currentData || isMuted) return;
    
    const { verdict } = currentData;
    let textToSpeak = '';
    
    if (verdict === 'GO') {
      textToSpeak = t('watchman.approved');
    } else if (verdict === 'STOP') {
      textToSpeak = t('watchman.denied');
    }
    
    if (textToSpeak && 'speechSynthesis' in window) {
      window.speechSynthesis.cancel();
      const utterance = new SpeechSynthesisUtterance(textToSpeak);
      utterance.lang = i18n.language === 'en' ? 'en-US' : (i18n.language === 'hi' ? 'hi-IN' : 'mr-IN');
      window.speechSynthesis.speak(utterance);
    }
  }, [currentData, isMuted, t, i18n.language]);

  if (!data) {
    return (
      <div className="flex flex-col items-center justify-center h-full">
        <p className="text-gray-500 mb-4">No verdict data found.</p>
        <button onClick={() => navigate('/watchman')} className="bg-indigo-600 text-white px-6 py-2 rounded-lg">Go Home</button>
      </div>
    );
  }

  const { verdict, reasonCode, displayName, maskedPhone, challengeId, endsAt } = currentData;

  const handleOtpComplete = async (otp: string) => {
    setIsProcessingOtp(true);
    setOtpError(null);
    try {
      const res = await fetch('/api/v1/entry/verify-otp', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ 
          challengeId, 
          otp, 
          deviceId: 'browser-test',
          arrivedCount: 1 // default for now
        })
      });
      const result = await res.json();
      
      if (result.verdict === 'GO') {
        setLocalVerdict(result);
      } else {
        setOtpError(result.reasonCode || 'Invalid OTP');
      }
    } catch (err) {
      setOtpError('Network error');
    } finally {
      setIsProcessingOtp(false);
    }
  };

  const toggleMute = () => setIsMuted(!isMuted);

  if (verdict === 'GO') {
    // Vibrate/beep can be simulated with browser APIs if supported
    if (navigator.vibrate) navigator.vibrate(200);

    return (
      <div className="flex flex-col h-full bg-green-500 text-white p-6 justify-center items-center text-center animate-in fade-in zoom-in duration-300 relative">
        <button onClick={toggleMute} className="absolute top-6 right-6 p-2 bg-white/20 rounded-full" aria-label={isMuted ? "Unmute" : "Mute"}>
          {isMuted ? <VolumeX className="w-6 h-6" /> : <Volume2 className="w-6 h-6" />}
        </button>
        <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center mb-8 shadow-2xl">
          <svg className="w-20 h-20 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" d="M5 13l4 4L19 7"></path></svg>
        </div>
        <h1 className="text-5xl font-extrabold mb-4 uppercase tracking-wider">{t('watchman.approved')}</h1>
        <p className="text-2xl font-semibold mb-2">{displayName}</p>
        <p className="text-green-100 text-lg mb-12">Ends at {new Date(endsAt).toLocaleTimeString([], {hour: '2-digit', minute:'2-digit'})}</p>
        
        <button onClick={() => navigate('/watchman')} className="mt-auto w-full bg-white text-green-700 py-4 rounded-xl font-bold text-xl shadow-lg active:scale-95 transition-transform">
          DONE
        </button>
      </div>
    );
  }

  if (verdict === 'STOP') {
    if (navigator.vibrate) navigator.vibrate([100, 50, 100, 50, 100]);

    return (
      <div className="flex flex-col h-full bg-red-600 text-white p-6 justify-center items-center text-center animate-in fade-in zoom-in duration-300 relative">
        <button onClick={toggleMute} className="absolute top-6 right-6 p-2 bg-white/20 rounded-full" aria-label={isMuted ? "Unmute" : "Mute"}>
          {isMuted ? <VolumeX className="w-6 h-6" /> : <Volume2 className="w-6 h-6" />}
        </button>
        <div className="w-32 h-32 bg-white rounded-full flex items-center justify-center mb-8 shadow-2xl">
          <svg className="w-20 h-20 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="4" d="M6 18L18 6M6 6l12 12"></path></svg>
        </div>
        <h1 className="text-5xl font-extrabold mb-4 uppercase tracking-wider">{t('watchman.denied')}</h1>
        <p className="text-xl font-medium bg-red-800 px-4 py-2 rounded-lg inline-block mb-12 border border-red-500">{reasonCode}</p>
        
        <button onClick={() => navigate('/watchman')} className="mt-auto w-full bg-white text-red-700 py-4 rounded-xl font-bold text-xl shadow-lg active:scale-95 transition-transform">
          GO BACK
        </button>
      </div>
    );
  }

  // HOLD (OTP Required)
  return (
    <div className="flex flex-col h-full bg-amber-400 p-6 pt-12 animate-in fade-in duration-300">
      <div className="bg-white rounded-3xl shadow-xl p-6 flex-grow flex flex-col">
        <div className="text-center mb-8">
          <div className="w-16 h-16 bg-amber-100 text-amber-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path></svg>
          </div>
          <h2 className="text-2xl font-bold text-gray-900 mb-1">Verify Identity</h2>
          <p className="text-gray-600">{displayName}</p>
          {maskedPhone && <p className="text-sm text-gray-500 mt-1">OTP sent to {maskedPhone}</p>}
        </div>

        <div className="flex-grow flex items-center">
          <OtpPad onComplete={handleOtpComplete} isProcessing={isProcessingOtp} error={otpError} />
        </div>
        
        <button 
          onClick={() => navigate('/watchman')}
          className="mt-6 text-gray-500 font-medium py-3"
        >
          Cancel
        </button>
      </div>
    </div>
  );
}
