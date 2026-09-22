import React, { useState } from 'react';
import { TrustDrawer } from './TrustDrawer';

interface TrustBadgeProps {
    score: number;
    badge: 'NEW' | 'STANDARD' | 'TRUSTED' | 'WATCH';
    componentsJson?: string;
    showLabel?: boolean;
}

export const TrustBadge: React.FC<TrustBadgeProps> = ({ score, badge, componentsJson, showLabel = true }) => {
    const [isDrawerOpen, setIsDrawerOpen] = useState(false);

    let colorClass = 'bg-gray-100 text-gray-800';
    if (badge === 'TRUSTED') colorClass = 'bg-green-100 text-green-800 border border-green-200';
    if (badge === 'WATCH') colorClass = 'bg-red-100 text-red-800 border border-red-200';
    if (badge === 'NEW') colorClass = 'bg-blue-100 text-blue-800';

    return (
        <>
            <button 
                onClick={() => setIsDrawerOpen(true)}
                className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium cursor-pointer hover:opacity-80 transition-opacity ${colorClass}`}
                title="Click to see why this score was given"
            >
                <span className="mr-1">⭐ {score.toFixed(1)}</span>
                {showLabel && <span className="uppercase tracking-wider ml-1 text-[10px] font-bold">{badge}</span>}
            </button>

            {isDrawerOpen && (
                <TrustDrawer 
                    score={score} 
                    badge={badge} 
                    componentsJson={componentsJson || '{}'} 
                    onClose={() => setIsDrawerOpen(false)} 
                />
            )}
        </>
    );
};
