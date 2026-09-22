import React from 'react';
import { Outlet } from 'react-router-dom';

export default function WatchmanLayout() {
  return (
    <div className="min-h-screen bg-gray-100 flex flex-col">
      {/* Top Bar without menus */}
      <header className="bg-indigo-600 text-white p-4 flex justify-between items-center shadow-md">
        <div>
          <h1 className="text-xl font-bold">SmartSpace Entry</h1>
          <p className="text-sm opacity-80">Green Meadows Hall</p>
        </div>
        <div className="flex items-center space-x-2">
          {/* Online/Offline Pill */}
          <span className="flex items-center bg-green-500 text-white text-xs font-semibold px-2 py-1 rounded-full shadow-inner">
            <span className="w-2 h-2 bg-white rounded-full mr-1 animate-pulse"></span>
            ONLINE
          </span>
        </div>
      </header>
      
      {/* Main Content Area */}
      <main className="flex-grow p-4 overflow-y-auto">
        <Outlet />
      </main>
    </div>
  );
}
