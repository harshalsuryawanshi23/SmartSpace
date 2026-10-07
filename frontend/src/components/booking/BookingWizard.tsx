import React, { useState } from 'react';
import { useTranslation } from 'react-i18next';
import type { BookingQuoteRequest, BookingCreateRequest, PriceBreakdown, BookingResponse } from './bookingTypes';

interface BookingWizardProps {
  hallId: number;
  onSuccess?: (booking: BookingResponse) => void;
  onCancel?: () => void;
}

export const BookingWizard: React.FC<BookingWizardProps> = ({ hallId, onSuccess, onCancel }) => {
  const { t } = useTranslation();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [alternatives, setAlternatives] = useState<any[]>([]);
  const [splitCost, setSplitCost] = useState(false);
  const [bookingResult, setBookingResult] = useState<BookingResponse | null>(null);
  
  // Co-host state
  const [cohostName, setCohostName] = useState('');
  const [cohostAmount, setCohostAmount] = useState('');
  const [cohosts, setCohosts] = useState<any[]>([]);

  // Step 1: Slot Selection
  const [date, setDate] = useState('');
  const [startTime, setStartTime] = useState('');
  const [endTime, setEndTime] = useState('');
  const [guestCount, setGuestCount] = useState(10);

  // Step 2: Details
  const [eventType, setEventType] = useState('MARRIAGE');
  const [eventTitle, setEventTitle] = useState('');
  const [themeTags, setThemeTags] = useState('');

  // Step 3: Review
  const [quote, setQuote] = useState<PriceBreakdown | null>(null);

  const fetchQuote = async () => {
    try {
      setLoading(true);
      setError('');
      // Convert to UTC ISO format for backend
      // Assuming naive local time for now, in a real app use proper timezone handling
      const startAt = new Date(`${date}T${startTime}:00`).toISOString();
      const endAt = new Date(`${date}T${endTime}:00`).toISOString();

      const req: BookingQuoteRequest = {
        hallId,
        startAt,
        endAt,
        guestCount,
      };

      const res = await fetch('/api/v1/bookings/quote', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req),
      });

      if (!res.ok) {
        const data = await res.json();
        throw new Error(data.message || 'Failed to fetch quote');
      }

      const quoteData = await res.json();
      setQuote(quoteData);
      setStep(3);
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  const handleNext = () => {
    if (step === 1) {
      if (!date || !startTime || !endTime) {
        setError('Please fill in all date and time fields');
        return;
      }
      setStep(2);
    } else if (step === 2) {
      if (!eventTitle) {
        setError('Please provide an event title');
        return;
      }
      fetchQuote();
    }
  };

  const handleBook = async () => {
    try {
      setLoading(true);
      setError('');
      setAlternatives([]);
      
      const startAt = new Date(`${date}T${startTime}:00`).toISOString();
      const endAt = new Date(`${date}T${endTime}:00`).toISOString();
      
      // Basic UUID generation in browser
      const idempotencyKey = crypto.randomUUID ? crypto.randomUUID() : Math.random().toString(36).substring(2);

      const req: BookingCreateRequest = {
        idempotencyKey,
        hallId,
        eventType,
        eventTitle,
        themeTags: themeTags.split(',').map((t) => t.trim()).filter((t) => t.length > 0),
        guestCount,
        startAt,
        endAt,
      };

      const res = await fetch('/api/v1/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(req),
      });

      if (!res.ok) {
        const data = await res.json();
        if (data.errorCode === 'SLOT_UNAVAILABLE' && data.alternatives) {
           setAlternatives(data.alternatives);
        }
        throw new Error(data.message || 'Booking failed');
      }

      const booking = await res.json();
      setBookingResult(booking);
      
      if (splitCost) {
        setStep(4);
      } else {
        if (onSuccess) onSuccess(booking);
      }
    } catch (err: any) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold mb-4">{t('booking.title')}</h2>
      
      {/* Progress */}
      <div className="flex mb-6 text-sm font-medium">
        <div className={`flex-1 pb-2 border-b-2 ${step >= 1 ? 'border-primary text-primary' : 'border-gray-200 text-gray-400'}`}>{t('booking.step1')}</div>
        <div className={`flex-1 pb-2 border-b-2 ${step >= 2 ? 'border-primary text-primary' : 'border-gray-200 text-gray-400'}`}>{t('booking.step2')}</div>
        <div className={`flex-1 pb-2 border-b-2 ${step >= 3 ? 'border-primary text-primary' : 'border-gray-200 text-gray-400'}`}>{t('booking.step3')}</div>
        {splitCost && step >= 4 && (
           <div className="flex-1 pb-2 border-b-2 border-primary text-primary">Split</div>
        )}
      </div>

      {error && <div className="mb-4 p-3 bg-red-100 text-red-700 rounded text-sm">{error}</div>}

      {alternatives.length > 0 && (
        <div className="mb-4 p-4 border border-yellow-400 bg-yellow-50 rounded">
          <h4 className="font-semibold text-yellow-800 mb-2">Alternative Options</h4>
          <ul className="space-y-2 text-sm text-yellow-900">
            {alternatives.map((alt, i) => (
              <li key={i} className="flex flex-col border-b border-yellow-200 pb-2">
                <span><strong>Try this time:</strong> {new Date(alt.startAt).toLocaleString()} - {new Date(alt.endAt).toLocaleTimeString()}</span>
                {/* Note: Clicking this could theoretically update the form state and fetch a new quote */}
              </li>
            ))}
          </ul>
          <p className="mt-2 text-xs text-yellow-700">Please select one of the times above in Step 1 to proceed.</p>
        </div>
      )}

      {step === 1 && (
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">{t('booking.date')}</label>
            <input type="date" value={date} onChange={(e) => setDate(e.target.value)} className="w-full p-2 border rounded" />
          </div>
          <div className="flex space-x-4">
            <div className="flex-1">
              <label className="block text-sm font-medium mb-1">{t('booking.startTime')}</label>
              <input type="time" value={startTime} onChange={(e) => setStartTime(e.target.value)} step="1800" className="w-full p-2 border rounded" />
            </div>
            <div className="flex-1">
              <label className="block text-sm font-medium mb-1">{t('booking.endTime')}</label>
              <input type="time" value={endTime} onChange={(e) => setEndTime(e.target.value)} step="1800" className="w-full p-2 border rounded" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">{t('booking.guests')}</label>
            <input type="number" value={guestCount} onChange={(e) => setGuestCount(Number(e.target.value))} min="1" className="w-full p-2 border rounded" />
          </div>
        </div>
      )}

      {step === 2 && (
        <div className="space-y-4">
          <div>
            <label className="block text-sm font-medium mb-1">Event Type</label>
            <select value={eventType} onChange={(e) => setEventType(e.target.value)} className="w-full p-2 border rounded">
              <option value="MARRIAGE">Marriage</option>
              <option value="BIRTHDAY">Birthday</option>
              <option value="CORPORATE">Corporate</option>
              <option value="WORKSHOP">Workshop</option>
              <option value="OTHER">Other</option>
            </select>
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Event Title</label>
            <input type="text" value={eventTitle} onChange={(e) => setEventTitle(e.target.value)} placeholder="E.g. John's 30th Birthday" className="w-full p-2 border rounded" />
          </div>
          <div>
            <label className="block text-sm font-medium mb-1">Theme Tags (comma separated)</label>
            <input type="text" value={themeTags} onChange={(e) => setThemeTags(e.target.value)} placeholder="e.g. decoration, loud-music" className="w-full p-2 border rounded" />
          </div>
        </div>
      )}

      {step === 3 && quote && (
        <div className="space-y-4 bg-gray-50 p-4 rounded border">
          <h3 className="font-semibold text-lg border-b pb-2">Price Breakdown</h3>
          <div className="flex justify-between text-sm">
            <span>Base Price</span>
            <span>{quote.currency} {quote.basePrice.toFixed(2)}</span>
          </div>
          {quote.memberDiscount > 0 && (
            <div className="flex justify-between text-sm text-green-600">
              <span>Member Discount</span>
              <span>-{quote.currency} {quote.memberDiscount.toFixed(2)}</span>
            </div>
          )}
          <div className="flex justify-between text-sm">
            <span>Subtotal</span>
            <span>{quote.currency} {quote.subtotal.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span>Platform Fee</span>
            <span>{quote.currency} {quote.platformFee.toFixed(2)}</span>
          </div>
          <div className="flex justify-between text-sm">
            <span>Tax</span>
            <span>{quote.currency} {quote.tax.toFixed(2)}</span>
          </div>
          <div className="flex justify-between font-bold text-lg pt-2 border-t">
            <span>Total</span>
            <span>{quote.currency} {quote.total.toFixed(2)}</span>
          </div>
          <div className="pt-4 flex items-center">
            <input type="checkbox" id="splitCost" checked={splitCost} onChange={(e) => setSplitCost(e.target.checked)} className="mr-2" />
            <label htmlFor="splitCost" className="text-sm font-medium">Split Cost with Co-hosts (Extend lock to 30 mins)</label>
          </div>
        </div>
      )}

      {step === 4 && bookingResult && (
        <div className="space-y-4">
          <h3 className="font-semibold text-lg">Add Co-hosts to Share Payment</h3>
          <p className="text-sm text-gray-600">Your total is {quote?.currency} {quote?.total.toFixed(2)}. Add friends to pay a share.</p>
          <div className="flex space-x-2">
            <input type="text" placeholder="Name" value={cohostName} onChange={(e) => setCohostName(e.target.value)} className="w-1/2 p-2 border rounded text-sm" />
            <input type="number" placeholder="Amount" value={cohostAmount} onChange={(e) => setCohostAmount(e.target.value)} className="w-1/3 p-2 border rounded text-sm" />
            <button 
              className="bg-blue-600 text-white px-3 py-2 rounded text-sm"
              onClick={async () => {
                if (!cohostName || !cohostAmount) return;
                try {
                   setLoading(true);
                   const res = await fetch(`/api/v1/bookings/${bookingResult.publicId}/cohosts`, {
                     method: 'POST',
                     headers: { 'Content-Type': 'application/json' },
                     body: JSON.stringify({ displayName: cohostName, shareAmount: parseFloat(cohostAmount) })
                   });
                   if (res.ok) {
                     const co = await res.json();
                     setCohosts([...cohosts, co]);
                     setCohostName('');
                     setCohostAmount('');
                   }
                } finally {
                   setLoading(false);
                }
              }}
              disabled={loading}
            >
              Add
            </button>
          </div>
          
          {cohosts.length > 0 && (
            <div className="mt-4 border rounded">
              {cohosts.map((c, i) => (
                <div key={i} className="p-2 border-b flex justify-between text-sm">
                  <div>
                    <span className="font-semibold">{c.displayName}</span> - {c.shareAmount} ({c.status})
                  </div>
                  <button className="text-xs text-blue-500 underline" onClick={async () => {
                    await fetch(`/api/v1/bookings/${bookingResult.publicId}/cohosts/mock-pay/${c.payToken}`, { method: 'POST' });
                    // optimistically update status
                    const newCohosts = [...cohosts];
                    newCohosts[i].status = 'PAID';
                    setCohosts(newCohosts);
                  }}>
                    Mock Pay
                  </button>
                </div>
              ))}
            </div>
          )}

          <div className="pt-4 border-t mt-4 flex justify-end">
             <button onClick={() => { if (onSuccess) onSuccess(bookingResult); }} className="px-4 py-2 bg-green-600 text-white font-bold rounded">
               Finish
             </button>
          </div>
        </div>
      )}

      {step < 4 && (
        <div className="mt-6 flex justify-between">
          {step > 1 ? (
            <button onClick={() => setStep(step - 1)} className="px-4 py-2 border rounded hover:bg-gray-100" disabled={loading}>
              {t('booking.back')}
            </button>
          ) : (
            <button onClick={onCancel} className="px-4 py-2 border rounded text-gray-600 hover:bg-gray-100">
              {t('booking.cancel')}
            </button>
          )}
          
          {step < 3 ? (
            <button onClick={handleNext} className="px-4 py-2 bg-primary text-white rounded hover:bg-primary-dark" disabled={loading}>
              {t('booking.next')}
            </button>
          ) : (
            <button onClick={handleBook} className="px-4 py-2 bg-green-600 text-white font-bold rounded hover:bg-green-700" disabled={loading}>
              {loading ? 'Processing...' : (splitCost ? 'Start Split Cost' : t('booking.confirm'))}
            </button>
          )}
        </div>
      )}
    </div>
  );
};
