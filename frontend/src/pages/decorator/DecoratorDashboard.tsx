import { useState, useEffect } from "react";
import { useAuth } from '../../context/AuthContext';
import { EmptyState } from '../../components/ui/EmptyState';
import { AlertCircle, Calendar, CheckCircle2, Clock, Inbox, Package, XCircle } from 'lucide-react';

export default function DecoratorDashboard() {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState<'enquiries' | 'packages' | 'calendar'>('enquiries');
  
  const [enquiries, setEnquiries] = useState<any[]>([]);
  const [packages, setPackages] = useState<any[]>([]);
  const [calendarEvents, setCalendarEvents] = useState<any[]>([]);
  
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const fetchData = async () => {
      setIsLoading(true);
      setError(null);
      
      try {
        let endpoint = '';
        if (activeTab === 'enquiries') endpoint = '/api/v1/vendor/enquiries';
        else if (activeTab === 'packages') endpoint = '/api/v1/vendor/packages';
        else if (activeTab === 'calendar') endpoint = '/api/v1/vendor/calendar';
        
        // This will intentionally fail since we haven't built the backend endpoints yet.
        // It demonstrates proper empty/error state handling per the standing rule.
        const res = await fetch(endpoint, {
          headers: {
            'Authorization': `Bearer ${localStorage.getItem('token')}` // Example header if using JWT
          }
        });
        
        if (!res.ok) {
          if (res.status === 404) {
             throw new Error(`Endpoint not yet implemented (${res.status})`);
          }
          throw new Error(`Failed to fetch data (${res.status})`);
        }
        
        const data = await res.json();
        
        if (activeTab === 'enquiries') setEnquiries(data);
        else if (activeTab === 'packages') setPackages(data);
        else if (activeTab === 'calendar') setCalendarEvents(data);
        
      } catch (err: any) {
        setError(err.message || 'An unexpected error occurred');
        if (activeTab === 'enquiries') setEnquiries([]);
        if (activeTab === 'packages') setPackages([]);
        if (activeTab === 'calendar') setCalendarEvents([]);
      } finally {
        setIsLoading(false);
      }
    };
    
    fetchData();
  }, [activeTab]);

  const handleRespond = async (id: number, accept: boolean) => {
    try {
      const res = await fetch('/api/v1/vendor/enquiries/' + id + '/respond', {
        method: 'POST',
        headers: { 
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        },
        body: JSON.stringify({ accept, quotedPrice: accept ? 5000 : 0, note: accept ? 'Can do it.' : 'Busy.' })
      });
      if (!res.ok) throw new Error('Failed to respond');
      alert('Responded to enquiry');
    } catch (e: any) {
      alert(e.message);
    }
  };

  const renderEnquiries = () => {
    if (isLoading) return <div className="p-8 text-center text-muted">Loading enquiries...</div>;
    if (error) return <EmptyState title="Unable to load enquiries" message={error} type="error" />;
    if (enquiries.length === 0) return <EmptyState title="No Enquiries" message="You don't have any enquiries yet." />;
    
    return (
      <ul className="divide-y divide-line">
        {enquiries.map((e) => (
          <li key={e.id}>
            <div className="px-4 py-4 sm:px-6 flex justify-between items-center">
              <div>
                <p className="text-sm font-medium text-primary-600 truncate">{e.package}</p>
                <p className="mt-1 text-sm text-muted">Booking: {e.bookingRef} | Status: {e.status}</p>
                <p className="mt-2 text-sm text-ink">"{e.message}"</p>
              </div>
              {e.status === 'SENT' && (
                <div className="flex space-x-2">
                  <button onClick={() => handleRespond(e.id, true)} className="bg-success text-white px-3 py-1 rounded text-sm hover:bg-success/90">Accept (Quote ₹5000)</button>
                  <button onClick={() => handleRespond(e.id, false)} className="bg-danger text-white px-3 py-1 rounded text-sm hover:bg-danger/90">Decline</button>
                </div>
              )}
            </div>
          </li>
        ))}
      </ul>
    );
  };

  const renderPackages = () => {
    if (isLoading) return <div className="p-8 text-center text-muted">Loading packages...</div>;
    if (error) return <EmptyState title="Unable to load packages" message={error} type="error" />;
    if (packages.length === 0) return <EmptyState title="No Packages" message="You haven't created any decoration packages." action={{ label: "Create Package", onClick: () => alert("Not implemented") }} />;
    return null;
  };

  const renderCalendar = () => {
    if (isLoading) return <div className="p-8 text-center text-muted">Loading calendar...</div>;
    if (error) return <EmptyState title="Unable to load calendar" message={error} type="error" />;
    if (calendarEvents.length === 0) return <EmptyState title="No Events" message="Your calendar is clear." />;
    return null;
  };

  return (
    <div className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
      <div className="md:flex md:items-center md:justify-between mb-6 px-4 sm:px-0">
        <div className="min-w-0 flex-1">
          <h1 className="text-2xl font-bold leading-7 text-ink sm:truncate sm:text-3xl sm:tracking-tight">
            Decorator Console
          </h1>
        </div>
      </div>
      
      <div className="bg-white shadow sm:rounded-lg mb-6">
        <div className="border-b border-line">
          <nav className="-mb-px flex space-x-8 px-6" aria-label="Tabs">
            <button
              onClick={() => setActiveTab('enquiries')}
              className={`${
                activeTab === 'enquiries'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-muted hover:text-ink hover:border-line'
              } whitespace-nowrap flex py-4 px-1 border-b-2 font-medium text-sm`}
            >
              <Inbox className={`-ml-0.5 mr-2 h-5 w-5 ${activeTab === 'enquiries' ? 'text-primary-600' : 'text-muted'}`} />
              Enquiries
            </button>
            <button
              onClick={() => setActiveTab('packages')}
              className={`${
                activeTab === 'packages'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-muted hover:text-ink hover:border-line'
              } whitespace-nowrap flex py-4 px-1 border-b-2 font-medium text-sm`}
            >
              <Package className={`-ml-0.5 mr-2 h-5 w-5 ${activeTab === 'packages' ? 'text-primary-600' : 'text-muted'}`} />
              Packages
            </button>
            <button
              onClick={() => setActiveTab('calendar')}
              className={`${
                activeTab === 'calendar'
                  ? 'border-primary-600 text-primary-600'
                  : 'border-transparent text-muted hover:text-ink hover:border-line'
              } whitespace-nowrap flex py-4 px-1 border-b-2 font-medium text-sm`}
            >
              <Calendar className={`-ml-0.5 mr-2 h-5 w-5 ${activeTab === 'calendar' ? 'text-primary-600' : 'text-muted'}`} />
              Calendar
            </button>
          </nav>
        </div>
        
        <div className="p-0">
          {activeTab === 'enquiries' && renderEnquiries()}
          {activeTab === 'packages' && renderPackages()}
          {activeTab === 'calendar' && renderCalendar()}
        </div>
      </div>
    </div>
  );
}
