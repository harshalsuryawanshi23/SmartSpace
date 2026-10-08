import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";
import { apiClient as api } from "../../lib/apiClient";

export const Navbar: React.FC = () => {
    const { isAuthenticated, user, logout } = useAuth();
    const navigate = useNavigate();

    const [unreadCount, setUnreadCount] = useState(0);
    const [showNotifications, setShowNotifications] = useState(false);
    const [notifications, setNotifications] = useState<any[]>([]);

    useEffect(() => {
        if (!isAuthenticated) {
            setUnreadCount(0);
            setNotifications([]);
            return;
        }

        const fetchNotifications = async () => {
            try {
                const [countRes, notifRes] = await Promise.all([
                    api.get("/notifications/unread-count"),
                    api.get("/notifications?size=5")
                ]);

                setUnreadCount(
                    typeof countRes.data === "number"
                        ? countRes.data
                        : countRes.data?.count || 0
                );

                setNotifications(
                    notifRes.data?.content ||
                    (Array.isArray(notifRes.data) ? notifRes.data : [])
                );
            } catch {
                setUnreadCount(0);
                setNotifications([]);
            }
        };

        fetchNotifications();
    }, [isAuthenticated]);

    const handleLogout = async () => {
        await logout();
        setShowNotifications(false);
        navigate("/login", { replace: true });
    };

    const handleMarkAsRead = async (id: number) => {
        try {
            await api.post(`/notifications/${id}/read`);

            setUnreadCount((prev) => Math.max(0, prev - 1));

            setNotifications((prev) =>
                prev.map((n) =>
                    n.id === id
                        ? { ...n, readAt: new Date().toISOString() }
                        : n
                )
            );
        } catch {
            // Keep the notification unchanged if the request fails.
        }
    };

    const role = user?.role || "RESIDENT";

    return (
        <nav className="bg-white shadow-sm border-b sticky top-0 z-40">
            <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
                <div className="flex justify-between h-16">
                    <div className="flex items-center">
                        <Link
                            to={isAuthenticated ? "/dashboard" : "/home"}
                            className="text-xl font-bold text-blue-600"
                        >
                            SmartSpace
                        </Link>

                        <div className="hidden md:flex ml-8 space-x-6">
                            <Link
                                to="/search"
                                className="text-sm font-medium text-gray-700 hover:text-blue-600"
                            >
                                Find Halls
                            </Link>

                            {isAuthenticated && (
                                <>
                                    <Link
                                        to="/dashboard"
                                        className="text-sm font-medium text-gray-700 hover:text-blue-600"
                                    >
                                        Dashboard
                                    </Link>

                                    {role === "RESIDENT" && (
                                        <Link
                                            to="/bookings"
                                            className="text-sm font-medium text-gray-700 hover:text-blue-600"
                                        >
                                            My Bookings
                                        </Link>
                                    )}

                                    {role === "WATCHMAN" && (
                                        <Link
                                            to="/watchman"
                                            className="text-sm font-medium text-gray-700 hover:text-blue-600"
                                        >
                                            Watchman
                                        </Link>
                                    )}

                                    {role === "HALL_OWNER" && (
                                        <Link
                                            to="/decorator/dashboard"
                                            className="text-sm font-medium text-gray-700 hover:text-blue-600"
                                        >
                                            Operations
                                        </Link>
                                    )}
                                </>
                            )}
                        </div>
                    </div>

                    <div className="flex items-center">
                        {isAuthenticated && (
                            <div className="relative mr-4">
                                <button
                                    onClick={() =>
                                        setShowNotifications((value) => !value)
                                    }
                                    className="relative p-2 rounded-full text-gray-500 hover:bg-gray-100"
                                    aria-label="Notifications"
                                >
                                    <svg
                                        className="h-6 w-6"
                                        fill="none"
                                        viewBox="0 0 24 24"
                                        stroke="currentColor"
                                    >
                                        <path
                                            strokeLinecap="round"
                                            strokeLinejoin="round"
                                            strokeWidth="2"
                                            d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"
                                        />
                                    </svg>

                                    {unreadCount > 0 && (
                                        <span className="absolute top-0 right-0 min-w-4 h-4 px-1 rounded-full bg-red-500 text-white text-[10px] flex items-center justify-center">
                                            {unreadCount > 9 ? "9+" : unreadCount}
                                        </span>
                                    )}
                                </button>

                                {showNotifications && (
                                    <div className="absolute right-0 mt-2 w-80 rounded-lg shadow-xl bg-white border z-50">
                                        <div className="px-4 py-3 border-b">
                                            <h3 className="font-semibold text-gray-800">
                                                Notifications
                                            </h3>
                                        </div>

                                        {notifications.length === 0 ? (
                                            <div className="p-5 text-center text-sm text-gray-500">
                                                No notifications
                                            </div>
                                        ) : (
                                            notifications.map((notif) => (
                                                <button
                                                    key={notif.id}
                                                    onClick={() =>
                                                        !notif.readAt &&
                                                        handleMarkAsRead(notif.id)
                                                    }
                                                    className={`w-full text-left p-3 border-b hover:bg-gray-50 ${
                                                        notif.readAt
                                                            ? "bg-white"
                                                            : "bg-blue-50"
                                                    }`}
                                                >
                                                    <p className="text-sm font-medium text-gray-800">
                                                        {notif.title}
                                                    </p>
                                                    <p className="text-xs text-gray-600 mt-1">
                                                        {notif.body}
                                                    </p>
                                                </button>
                                            ))
                                        )}
                                    </div>
                                )}
                            </div>
                        )}

                        {isAuthenticated ? (
                            <div className="flex items-center gap-3">
                                <div className="hidden sm:block text-right">
                                    <p className="text-sm font-medium text-gray-800">
                                        {user?.fullName || user?.firstName || "User"}
                                    </p>
                                    <p className="text-xs text-gray-500">
                                        {role.replace("_", " ")}
                                    </p>
                                </div>

                                <button
                                    onClick={handleLogout}
                                    className="text-sm font-medium text-red-600 hover:text-red-800"
                                >
                                    Logout
                                </button>
                            </div>
                        ) : (
                            <Link
                                to="/login"
                                className="text-sm font-medium text-gray-700 hover:text-blue-600"
                            >
                                Log in
                            </Link>
                        )}
                    </div>
                </div>
            </div>
        </nav>
    );
};
