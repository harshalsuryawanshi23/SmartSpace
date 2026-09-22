import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';

export default function Home() {
  const [locality, setLocality] = useState('');
  const [guests, setGuests] = useState('');
  const navigate = useNavigate();

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    // Typically we would look up locality lat/lng, but for MVP we might just route
    navigate(/search?locality= + encodeURIComponent(locality) + &guests= + guests);
  };

  return (
    <div className="max-w-4xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <div className="text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 sm:text-5xl sm:tracking-tight lg:text-6xl">
          Discover the Perfect Hall
        </h1>
        <p className="mt-5 max-w-xl mx-auto text-xl text-gray-500">
          Find spaces for your community events, celebrations, and gatherings.
        </p>
      </div>

      <div className="mt-10 sm:flex sm:justify-center">
        <form onSubmit={handleSearch} className="flex space-x-2">
          <input
            type="text"
            placeholder="Locality (e.g. Pune)"
            className="w-full sm:max-w-xs border-gray-300 rounded-md shadow-sm focus:ring-teal-500 focus:border-teal-500 sm:text-sm"
            value={locality}
            onChange={(e) => setLocality(e.target.value)}
          />
          <input
            type="number"
            placeholder="Guests"
            className="w-full sm:max-w-xs border-gray-300 rounded-md shadow-sm focus:ring-teal-500 focus:border-teal-500 sm:text-sm"
            value={guests}
            onChange={(e) => setGuests(e.target.value)}
          />
          <button
            type="submit"
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-teal-600 hover:bg-teal-700"
          >
            Search
          </button>
        </form>
      </div>
    </div>
  );
}
