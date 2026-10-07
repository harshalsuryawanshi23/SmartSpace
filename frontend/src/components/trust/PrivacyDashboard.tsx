import { useState } from "react";
import { useTranslation } from 'react-i18next';
import { Shield, Download, Trash2, Bell } from 'lucide-react';
import { useWebPush } from '../../hooks/useWebPush';

export const PrivacyDashboard: React.FC = () => {
  // In a real app we'd get the actual user ID from an AuthContext
  const mockUserId = "1";
  const { isSupported, isSubscribed, loading: pushLoading, subscribe, unsubscribe } = useWebPush(mockUserId);

  const { t } = useTranslation();
  const [isExporting, setIsExporting] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [message, setMessage] = useState('');

  const handleExport = async () => {
    setIsExporting(true);
    setMessage('');
    try {
      // Stub for export API
      const res = await fetch('/api/v1/privacy/export', {
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (res.ok) {
        const blob = await res.blob();
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = 'my-data-export.json';
        a.click();
        setMessage('Export successful');
      } else {
        setMessage('Export failed');
      }
    } catch (e) {
      setMessage('Network error during export');
    } finally {
      setIsExporting(false);
    }
  };

  const handleDeleteRequest = async () => {
    if (!window.confirm('Are you sure you want to request account deletion? This action cannot be undone.')) {
      return;
    }
    
    setIsDeleting(true);
    setMessage('');
    try {
      // Stub for delete API
      const res = await fetch('/api/v1/privacy/account', {
        method: 'DELETE',
        headers: {
          'Authorization': `Bearer ${localStorage.getItem('token')}`
        }
      });
      if (res.ok) {
        setMessage('Account deletion requested successfully');
      } else {
        setMessage('Failed to request account deletion');
      }
    } catch (e) {
      setMessage('Network error during deletion request');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="max-w-4xl mx-auto p-6">
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="p-6 border-b border-gray-100 bg-gray-50 flex items-center space-x-3">
          <div className="p-2 bg-indigo-100 rounded-lg text-indigo-600">
            <Shield className="w-6 h-6" />
          </div>
          <h2 className="text-xl font-bold text-gray-900">{t('privacy.dashboard')}</h2>
        </div>
        
        <div className="p-6 space-y-8">
          {message && (
            <div className="p-4 bg-blue-50 text-blue-700 rounded-lg">
              {message}
            </div>
          )}

          {/* Export Section */}
          <div className="flex items-start space-x-4">
            <div className="p-3 bg-green-50 text-green-600 rounded-full">
              <Download className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900">Export Your Data</h3>
              <p className="text-gray-500 mt-1 mb-4">
                Download a copy of all the data associated with your account, including profile information and booking history.
              </p>
              <button 
                onClick={handleExport}
                disabled={isExporting}
                className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
              >
                {isExporting ? 'Exporting...' : t('privacy.export')}
              </button>
            </div>
          </div>

          <hr className="border-gray-100" />

          {/* Delete Section */}
          <div className="flex items-start space-x-4">
            <div className="p-3 bg-red-50 text-red-600 rounded-full">
              <Trash2 className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900">Delete Account</h3>
              <p className="text-gray-500 mt-1 mb-4">
                Request permanent deletion of your account. Your personal information will be removed, and financial records will be anonymized.
              </p>
              <button 
                onClick={handleDeleteRequest}
                disabled={isDeleting}
                className="px-4 py-2 bg-red-50 text-red-600 rounded-lg text-sm font-medium hover:bg-red-100 disabled:opacity-50"
              >
                {isDeleting ? 'Processing...' : t('privacy.delete')}
              </button>
            </div>
          </div>

          <hr className="border-gray-100" />

          {/* Web Push Section */}
          <div className="flex items-start space-x-4">
            <div className="p-3 bg-yellow-50 text-yellow-600 rounded-full">
              <Bell className="w-6 h-6" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-semibold text-gray-900">Web Push Notifications</h3>
              <p className="text-gray-500 mt-1 mb-4">
                Receive instant alerts about your bookings on this device.
              </p>
              {isSupported ? (
                <button 
                  onClick={() => isSubscribed ? unsubscribe() : subscribe()}
                  disabled={pushLoading}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50"
                >
                  {pushLoading ? 'Processing...' : (isSubscribed ? 'Disable Notifications' : 'Enable Notifications')}
                </button>
              ) : (
                <p className="text-sm text-red-500">Push notifications are not supported in this browser.</p>
              )}
            </div>
          </div>
          
        </div>
      </div>
    </div>
  );
};
