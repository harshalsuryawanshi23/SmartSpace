import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';

export const Navbar: React.FC = () => {
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);

  useEffect(() => {
    // Mock fetching unread count
    setUnreadCount(3);
  }, []);

  return (
    <nav className="bg-white shadow">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between h-16">
          <div className="flex">
            <div className="flex-shrink-0 flex items-center">
              <Link to="/" className="text-xl font-bold text-blue-600">SmartSpace</Link>
            </div>
            <div className="hidden sm:ml-6 sm:flex sm:space-x-8">
              <Link to="/search" className="text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 border-transparent hover:border-gray-300">
                Find Halls
              </Link>
              <Link to="/owner" className="text-gray-900 inline-flex items-center px-1 pt-1 border-b-2 border-transparent hover:border-gray-300">
                For Owners
              </Link>
            </div>
          </div>
          <div className="flex items-center">
            {/* Notification Bell */}
            <div className="relative ml-3">
              <button 
                onClick={() => setShowNotifications(!showNotifications)}
                className="bg-white p-1 rounded-full text-gray-400 hover:text-gray-500 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              >
                <span className="sr-only">View notifications</span>
                <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor" aria-hidden="true">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" />
                </svg>
                {unreadCount > 0 && (
                  <span className="absolute top-0 right-0 block h-2.5 w-2.5 rounded-full bg-red-500 ring-2 ring-white"></span>
                )}
              </button>

              {showNotifications && (
                <div className="origin-top-right absolute right-0 mt-2 w-80 rounded-md shadow-lg bg-white ring-1 ring-black ring-opacity-5 focus:outline-none z-50">
                  <div className="py-2 p-2" role="menu" aria-orientation="vertical" aria-labelledby="options-menu">
                    <div className="flex justify-between items-center mb-2 px-2">
                      <h3 className="text-sm font-semibold">Notifications</h3>
                      <button className="text-xs text-blue-600 hover:underline">Mark all read</button>
                    </div>
                    {/* Mock notification item */}
                    <div className="p-2 border-b border-gray-100 bg-blue-50">
                      <p className="text-sm font-medium text-gray-800">Booking Confirmed</p>
                      <p className="text-xs text-gray-600 mt-1">Your booking for Grand Hall is confirmed.</p>
                      <p className="text-xs text-gray-400 mt-1">2 mins ago</p>
                    </div>
                    <div className="p-2 border-b border-gray-100">
                      <p className="text-sm font-medium text-gray-800">Payment Receipt</p>
                      <p className="text-xs text-gray-600 mt-1">Receipt for ₹12,000 sent.</p>
                      <p className="text-xs text-gray-400 mt-1">1 hour ago</p>
                    </div>
                  </div>
                </div>
              )}
            </div>
            
            <div className="ml-4 flex items-center">
              <Link to="/login" className="text-sm font-medium text-gray-700 hover:text-gray-900">
                Log in
              </Link>
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};
