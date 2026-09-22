import React, { useState } from 'react';

export interface EnquiryModalProps {
  isOpen: boolean;
  onClose: () => void;
  decoratorName: string;
  onSubmit: (message: string) => void;
}

export const EnquiryModal: React.FC<EnquiryModalProps> = ({ isOpen, onClose, decoratorName, onSubmit }) => {
  const [message, setMessage] = useState('');

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black bg-opacity-50">
      <div className="bg-white rounded-lg shadow-xl w-full max-w-md overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 flex justify-between items-center">
          <h2 className="text-lg font-semibold text-gray-800">Enquire with {decoratorName}</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">&times;</button>
        </div>
        
        <div className="p-6">
          <label className="block text-sm font-medium text-gray-700 mb-2">
            Message to Decorator
          </label>
          <textarea
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            className="w-full border border-gray-300 rounded-md shadow-sm p-3 focus:ring-blue-500 focus:border-blue-500 min-h-[120px]"
            placeholder="Tell them about your event, specific requirements, or ask any questions..."
          />
        </div>
        
        <div className="px-6 py-4 bg-gray-50 flex justify-end gap-3 rounded-b-lg">
          <button 
            onClick={onClose}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50"
          >
            Cancel
          </button>
          <button 
            onClick={() => onSubmit(message)}
            disabled={!message.trim()}
            className="px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Send Enquiry
          </button>
        </div>
      </div>
    </div>
  );
};
