import React from 'react';

interface OtpPadProps {
  onComplete: (otp: string) => void;
  isProcessing: boolean;
  error?: string | null;
}

export default function OtpPad({ onComplete, isProcessing, error }: OtpPadProps) {
  const [pin, setPin] = React.useState('');

  const handlePress = (num: string) => {
    if (isProcessing) return;
    if (pin.length < 6) {
      const newPin = pin + num;
      setPin(newPin);
      if (newPin.length === 6) {
        onComplete(newPin);
      }
    }
  };

  const handleBackspace = () => {
    if (isProcessing) return;
    setPin(pin.slice(0, -1));
  };

  return (
    <div className="w-full max-w-sm mx-auto flex flex-col items-center">
      {/* PIN Display */}
      <div className="flex gap-2 mb-8">
        {[0, 1, 2, 3, 4, 5].map((i) => (
          <div 
            key={i} 
            className={`w-10 h-14 rounded-lg flex items-center justify-center text-2xl font-bold border-b-4 
              ${pin.length > i ? 'border-indigo-600 bg-indigo-50 text-indigo-900' : 'border-gray-300 bg-gray-50'}`}
          >
            {pin[i] || ''}
          </div>
        ))}
      </div>

      {error && <div className="text-red-600 mb-4 font-medium animate-pulse">{error}</div>}

      {/* Number Pad */}
      <div className="grid grid-cols-3 gap-4 w-full">
        {[1, 2, 3, 4, 5, 6, 7, 8, 9].map((num) => (
          <button
            key={num}
            onClick={() => handlePress(num.toString())}
            disabled={isProcessing}
            className="h-16 rounded-2xl bg-white shadow-sm border border-gray-100 text-2xl font-semibold active:bg-gray-100 transition-colors"
          >
            {num}
          </button>
        ))}
        <div className="h-16"></div> {/* Empty slot */}
        <button
          onClick={() => handlePress('0')}
          disabled={isProcessing}
          className="h-16 rounded-2xl bg-white shadow-sm border border-gray-100 text-2xl font-semibold active:bg-gray-100 transition-colors"
        >
          0
        </button>
        <button
          onClick={handleBackspace}
          disabled={isProcessing || pin.length === 0}
          className="h-16 rounded-2xl bg-gray-100 shadow-sm text-xl font-semibold flex items-center justify-center active:bg-gray-200 transition-colors"
        >
          <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2M3 12l6.414 6.414a2 2 0 001.414.586H19a2 2 0 002-2V7a2 2 0 00-2-2h-8.172a2 2 0 00-1.414.586L3 12z"></path></svg>
        </button>
      </div>
    </div>
  );
}
