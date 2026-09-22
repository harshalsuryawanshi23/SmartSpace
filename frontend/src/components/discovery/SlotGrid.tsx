import React from 'react';

interface Slot {
  time: string;
  state: 'FREE' | 'BOOKED' | 'LOCKED' | 'CLOSED' | 'PAST' | 'BUFFER';
}

interface SlotGridProps {
  slots: Slot[];
  onSelect?: (time: string) => void;
}

export default function SlotGrid({ slots, onSelect }: SlotGridProps) {
  return (
    <div className="mt-4">
      <h3 className="text-lg font-medium text-gray-900 mb-2">Availability</h3>
      <div className="grid grid-cols-4 sm:grid-cols-6 lg:grid-cols-8 gap-2">
        {slots.map((slot, idx) => {
          let bgColor = 'bg-gray-100 text-gray-800';
          let cursor = 'cursor-pointer hover:bg-teal-100';
          
          if (slot.state === 'FREE') bgColor = 'bg-white border border-teal-500 text-teal-700';
          if (slot.state === 'BOOKED' || slot.state === 'LOCKED') {
            bgColor = 'bg-red-100 text-red-800';
            cursor = 'cursor-not-allowed opacity-50';
          }
          if (slot.state === 'CLOSED' || slot.state === 'PAST') {
            bgColor = 'bg-gray-200 text-gray-400';
            cursor = 'cursor-not-allowed';
          }
          if (slot.state === 'BUFFER') {
            bgColor = 'bg-yellow-100 text-yellow-800';
            cursor = 'cursor-not-allowed';
          }

          return (
            <div
              key={idx}
              role="button"
              tabIndex={0}
              aria-label={slot.time + ' ' + slot.state}
              className={"text-center py-2 text-sm rounded " + bgColor + " " + cursor}
              onClick={() => {
                if (slot.state === 'FREE' && onSelect) onSelect(slot.time);
              }}
              onKeyDown={(e) => {
                if (e.key === 'Enter' && slot.state === 'FREE' && onSelect) onSelect(slot.time);
              }}
            >
              {slot.time}
            </div>
          );
        })}
      </div>
    </div>
  );
}
