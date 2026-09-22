import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import LiveStatusWidget from '../components/owner/LiveStatusWidget';

export const Dashboard = () => {
    const { user, logout } = useAuth();
    const navigate = useNavigate();

    const handleLogout = async () => {
        await logout();
        navigate('/login');
    };

    return (
        <div className="min-h-screen bg-gray-100">
            <nav className="bg-white shadow-sm">
                <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                    <div className="flex justify-between h-16">
                        <div className="flex">
                            <div className="flex-shrink-0 flex items-center">
                                <h1 className="text-xl font-bold text-indigo-600">SmartSpace</h1>
                            </div>
                        </div>
                        <div className="flex items-center">
                            <span className="text-gray-700 mr-4">Welcome, {user?.firstName}</span>
                            <button
                                onClick={handleLogout}
                                className="bg-indigo-600 text-white px-3 py-1 rounded-md text-sm font-medium hover:bg-indigo-700"
                            >
                                Logout
                            </button>
                        </div>
                    </div>
                </div>
            </nav>

            <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
                <div className="px-4 py-6 sm:px-0 grid grid-cols-1 md:grid-cols-3 gap-6">
                    <div className="md:col-span-2">
                        <div className="border-4 border-dashed border-gray-200 rounded-lg h-96 flex items-center justify-center bg-white">
                            <div className="text-center">
                                <h2 className="text-2xl font-semibold text-gray-700">Dashboard</h2>
                                <p className="mt-2 text-gray-500">More features coming soon...</p>
                            </div>
                        </div>
                    </div>
                    
                    {/* Owner Widgets */}
                    <div>
                        <h2 className="text-lg font-bold text-gray-700 mb-4">Live Halls</h2>
                        <LiveStatusWidget />
                    </div>
                </div>
            </main>
        </div>
    );
};
