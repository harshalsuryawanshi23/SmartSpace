import React from 'react';

interface TrustDrawerProps {
    score: number;
    badge: string;
    componentsJson: string;
    onClose: () => void;
}

export const TrustDrawer: React.FC<TrustDrawerProps> = ({ score, badge, componentsJson, onClose }) => {
    let components: any = {};
    try {
        components = JSON.parse(componentsJson);
    } catch (e) {
        console.error("Failed to parse trust components", e);
    }

    return (
        <div className="fixed inset-0 z-50 flex justify-end">
            {/* Backdrop */}
            <div 
                className="fixed inset-0 bg-black bg-opacity-50 transition-opacity" 
                onClick={onClose}
            ></div>
            
            {/* Drawer panel */}
            <div className="relative w-96 max-w-full bg-white h-full shadow-2xl flex flex-col transform transition-transform duration-300 ease-in-out">
                <div className="px-6 py-4 border-b flex items-center justify-between">
                    <h2 className="text-xl font-bold">Why this score?</h2>
                    <button onClick={onClose} className="text-gray-500 hover:text-gray-700">
                        <svg className="w-6 h-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                    </button>
                </div>
                
                <div className="p-6 flex-1 overflow-y-auto">
                    <div className="mb-6 flex items-center justify-between bg-gray-50 p-4 rounded-lg">
                        <div>
                            <p className="text-sm text-gray-500">Current Score</p>
                            <p className="text-3xl font-bold">{score.toFixed(1)}</p>
                        </div>
                        <div className="text-right">
                            <p className="text-sm text-gray-500">Status</p>
                            <p className="text-lg font-bold uppercase">{badge}</p>
                        </div>
                    </div>

                    <h3 className="font-semibold text-lg mb-3">Breakdown</h3>
                    
                    <ul className="space-y-4">
                        <li className="flex justify-between items-start border-b pb-3">
                            <div>
                                <p className="font-medium">Prior Average</p>
                                <p className="text-xs text-gray-500">Baseline score for new members</p>
                            </div>
                            <span className="text-gray-700">{components.priorMean ? (components.priorMean * 100).toFixed(1) : '-'}</span>
                        </li>
                        
                        <li className="flex justify-between items-start border-b pb-3">
                            <div>
                                <p className="font-medium">Recent Performance</p>
                                <p className="text-xs text-gray-500">Ratings from verified stays, weighted by recency</p>
                            </div>
                            <span className="text-green-600">
                                {components.sumWeight ? `+${(components.sumWeightedScore / components.sumWeight * 100).toFixed(1)} avg` : '-'}
                            </span>
                        </li>

                        {(components.penalty > 0) && (
                            <li className="flex justify-between items-start border-b pb-3 text-red-600">
                                <div>
                                    <p className="font-medium">Penalties</p>
                                    <p className="text-xs opacity-80">
                                        {components.lateCancellations > 0 ? `${components.lateCancellations} late cancellations ` : ''}
                                        {components.noShows > 0 ? `${components.noShows} no shows ` : ''}
                                        {components.disputes > 0 ? `${components.disputes} disputes` : ''}
                                    </p>
                                </div>
                                <span>-{(components.penalty * 100).toFixed(1)}%</span>
                            </li>
                        )}
                    </ul>

                    <div className="mt-8 bg-blue-50 p-4 rounded text-sm text-blue-800">
                        <p><strong>Did you know?</strong> Our trust scores update every night. Recent ratings hold more weight, and suspected fake ratings are automatically reduced in value.</p>
                    </div>
                </div>
            </div>
        </div>
    );
};
