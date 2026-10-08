import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { apiClient } from "../../lib/apiClient";
import { toast } from "react-hot-toast";

export default function MyBookings() {
    const [bookings, setBookings] = useState<any[]>([]);
    const [loading, setLoading] = useState(true);

    const loadBookings = async () => {
        try {
            const response = await apiClient.get("/bookings");

            setBookings(
                Array.isArray(response.data)
                    ? response.data
                    : response.data?.content || []
            );
        } catch (error) {
            console.error(error);
            setBookings([]);
            toast.error("Unable to load bookings");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        loadBookings();
    }, []);

    const cancelBooking = async (id: string) => {
        if (!window.confirm("Cancel this booking?")) {
            return;
        }

        try {
            await apiClient.post(
                `/bookings/${id}/cancel`,
                "Cancelled by resident",
                {
                    params: { preview: false },
                    headers: {
                        "Content-Type": "text/plain"
                    }
                }
            );

            toast.success("Booking cancelled");
            loadBookings();
        } catch (error: any) {
            toast.error(
                error?.response?.data?.message ||
                "Unable to cancel booking"
            );
        }
    };

    return (
        <main className="max-w-6xl mx-auto px-4 py-8">
            <div className="flex flex-col sm:flex-row sm:justify-between sm:items-end gap-3 mb-6">
                <div>
                    <h1 className="text-2xl font-bold text-gray-900">
                        My Bookings
                    </h1>
                    <p className="text-sm text-gray-500 mt-1">
                        Manage your SmartSpace reservations.
                    </p>
                </div>

                <Link
                    to="/search"
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium text-center"
                >
                    + New Booking
                </Link>
            </div>

            {loading ? (
                <div className="bg-white border rounded-xl p-10 text-center">
                    Loading bookings...
                </div>
            ) : bookings.length === 0 ? (
                <div className="bg-white border rounded-xl p-10 text-center">
                    <p className="text-gray-500">
                        You do not have any bookings yet.
                    </p>
                    <Link
                        to="/search"
                        className="inline-block mt-4 text-blue-600 font-medium"
                    >
                        Find a hall →
                    </Link>
                </div>
            ) : (
                <div className="space-y-4">
                    {bookings.map((booking) => {
                        const canCancel = [
                            "PENDING_PAYMENT",
                            "CONFIRMED"
                        ].includes(booking.status);

                        return (
                            <div
                                key={booking.publicId}
                                className="bg-white border rounded-xl p-5 shadow-sm"
                            >
                                <div className="flex flex-col md:flex-row md:justify-between gap-4">
                                    <div>
                                        <div className="flex items-center gap-3">
                                            <h2 className="font-semibold text-gray-900">
                                                {booking.eventTitle ||
                                                    "SmartSpace Event"}
                                            </h2>

                                            <span className="px-2 py-1 rounded-full text-xs font-semibold bg-blue-50 text-blue-700">
                                                {booking.status}
                                            </span>
                                        </div>

                                        <p className="text-sm text-gray-500 mt-1">
                                            Ref: {booking.bookingRef}
                                        </p>
                                    </div>

                                    <div className="text-left md:text-right">
                                        <p className="text-xl font-bold">
                                            ₹{booking.priceTotal || 0}
                                        </p>
                                        <p className="text-xs text-gray-500">
                                            {booking.currency || "INR"}
                                        </p>
                                    </div>
                                </div>

                                <div className="mt-4 grid grid-cols-1 sm:grid-cols-3 gap-3 text-sm">
                                    <div className="bg-gray-50 rounded-lg p-3">
                                        <p className="text-gray-500">
                                            Date & time
                                        </p>
                                        <p className="font-medium mt-1">
                                            {new Date(
                                                booking.startAt
                                            ).toLocaleString()}
                                        </p>
                                    </div>

                                    <div className="bg-gray-50 rounded-lg p-3">
                                        <p className="text-gray-500">
                                            Guests
                                        </p>
                                        <p className="font-medium mt-1">
                                            {booking.guestCount}
                                        </p>
                                    </div>

                                    <div className="bg-gray-50 rounded-lg p-3">
                                        <p className="text-gray-500">
                                            Booking ID
                                        </p>
                                        <p className="font-medium mt-1 font-mono">
                                            {booking.publicId}
                                        </p>
                                    </div>
                                </div>

                                {canCancel && (
                                    <div className="mt-4 flex justify-end">
                                        <button
                                            onClick={() =>
                                                cancelBooking(
                                                    booking.publicId
                                                )
                                            }
                                            className="px-4 py-2 text-sm text-red-600 border border-red-200 rounded-lg hover:bg-red-50"
                                        >
                                            Cancel Booking
                                        </button>
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            )}
        </main>
    );
}
