import React, { useState } from 'react';

export interface DecoratorMatchCardProps {
  decoratorId: string;
  businessName: string;
  matchScore: number;
  positiveReasons: string[];
  warnings: string[];
  basePrice: number;
  packageDescription: string;
  onEnquire: () => void;
}

export const DecoratorMatchCard: React.FC<DecoratorMatchCardProps> = ({
  businessName,
  matchScore,
  positiveReasons,
  warnings,
  basePrice,
  packageDescription,
  onEnquire
}) => {
  return (
    <div className="bg-white rounded-lg shadow-md p-5 flex flex-col md:flex-row gap-6 border border-gray-100 hover:shadow-lg transition-shadow">
      <div className="flex-1">
        <div className="flex justify-between items-start mb-2">
          <h3 className="text-xl font-semibold text-gray-800">{businessName}</h3>
          <div className="bg-green-100 text-green-800 text-xs px-2 py-1 rounded-full font-bold">
            {matchScore}% Match
          </div>
        </div>
        <p className="text-gray-600 text-sm mb-4">{packageDescription}</p>
        
        <div className="mb-4">
          <h4 className="text-sm font-semibold text-gray-700 mb-1">Why it's a good fit:</h4>
          <ul className="text-sm text-green-700 space-y-1">
            {positiveReasons.map((r, i) => (
              <li key={i} className="flex items-center">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
                {r}
              </li>
            ))}
          </ul>
          
          {warnings.length > 0 && (
            <div className="mt-3">
              <h4 className="text-sm font-semibold text-gray-700 mb-1">Keep in mind:</h4>
              <ul className="text-sm text-orange-600 space-y-1">
                {warnings.map((w, i) => (
                  <li key={i} className="flex items-center">
                    <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path></svg>
                    {w}
                  </li>
                ))}
              </ul>
            </div>
          )}
        </div>
      </div>
      
      <div className="flex flex-col justify-center items-center md:items-end border-t md:border-t-0 md:border-l border-gray-100 pt-4 md:pt-0 md:pl-6 min-w-[150px]">
        <div className="text-center md:text-right mb-4">
          <p className="text-sm text-gray-500">Starting from</p>
          <p className="text-2xl font-bold text-gray-900">₹{basePrice}</p>
        </div>
        <button 
          onClick={onEnquire}
          className="w-full bg-blue-600 hover:bg-blue-700 text-white font-medium py-2 px-4 rounded-md transition-colors"
        >
          Enquire Now
        </button>
      </div>
    </div>
  );
};
