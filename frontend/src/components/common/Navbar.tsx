import { useState, useEffect } from "react";
import { Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { apiClient as api } from '../../lib/apiClient';

export const Navbar: React.FC = () => {
  const { isAuthenticated, user, logout } = useAuth();
  const [unreadCount, setUnreadCount] = useState(0);
  const [showNotifications, setShowNotifications] = useState(false);
  const [notifications, setNotifications] = useState<any[]>([]);

  useEffect(() => {
    if (isAuthenticated) {
      const fetchNotifications = async () => {
        try {
          const [countRes, notifRes] = await Promise.all([
            api.get('/notifications/unread-count'),
            api.get('/notifications?size=5')
          ]);
          setUnreadCount(countRes.data || 0);
          setNotifications(notifRes.data.content || []);
        } catch (error) {
          console.error("Failed to fetch notifications", error);
        }
      };
      fetchNotifications();
    }
  }, [isAuthenticated]);

  const handleMarkAsRead = async (id: number) => {
    try {
      await api.post(`/notifications/${id}/read`);
      setUnreadCount(prev => Math.max(0, prev - 1));
      setNotifications(notifications.map(n => n.id === id ? { ...n, readAt: new Date().toISOString() } : n));
    } catch (e) {
      console.error(e);
    }
  };

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
            {isAuthenticated && (
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
                      </div>
                      {notifications.length === 0 ? (
                        <div className="p-4 text-center text-gray-500 text-sm">No notifications</div>
                      ) : (
                        notifications.map(notif => (
                          <div 
                            key={notif.id} 
                            onClick={() => !notif.readAt && handleMarkAsRead(notif.id)}
                            className={`p-2 border-b border-gray-100 cursor-pointer ${notif.readAt ? 'bg-white' : 'bg-blue-50'}`}
                          >
                            <p className="text-sm font-medium text-gray-800">{notif.title}</p>
                            <p className="text-xs text-gray-600 mt-1">{notif.body}</p>
                            <p className="text-xs text-gray-400 mt-1">{new Date(notif.createdAt).toLocaleString()}</p>
                          </div>
                        ))
                      )}
                    </div>
                  </div>
                )}
              </div>
            )}
            
            <div className="ml-4 flex items-center">
              {isAuthenticated ? (
                <div className="flex items-center space-x-4">
                  <span className="text-sm font-medium text-gray-700">Hello, {user?.firstName || user?.id}</span>
                  <button onClick={logout} className="text-sm font-medium text-red-600 hover:text-red-800">
                    Logout
                  </button>
                </div>
              ) : (
                <Link to="/login" className="text-sm font-medium text-gray-700 hover:text-gray-900">
                  Log in
                </Link>
              )}
            </div>
          </div>
        </div>
      </div>
    </nav>
  );
};
