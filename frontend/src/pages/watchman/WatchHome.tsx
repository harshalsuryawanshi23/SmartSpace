import React, { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { apiClient } from "../../lib/apiClient";

interface Booking {
    id: number;
    bookingRef: string;
    status: string;
    startAt: string;
    endAt: string;
    displayName: string;
    guestCount?: number;
    currentHeadcount?: number;
    capacity?: number;
    alertLevel?: string;
}

export default function WatchHome() {
    const [bookings, setBookings] = useState<Booking[]>([]);
    const [loading, setLoading] = useState(true);
    const navigate = useNavigate();

    useEffect(() => {
        const fetchToday = async () => {
            try {
                const response = await apiClient.get("/entry/today");

                setBookings(
                    Array.isArray(response.data)
                        ? response.data
                        : response.data?.content || []
                );
            } catch (error) {
                console.error(error);
                setBookings([]);
            } finally {
                setLoading(false);
            }
        };

        fetchToday();
    }, []);

    const updateHeadcount = async (
        bookingId: number,
        newCount: number
    ) => {
        try {
            const response = await apiClient.post(
                "/entry/headcount",
                {
                    bookingId,
                    count: newCount,
                    deviceId: "browser-test"
                }
            );

            const data = response.data;

            setBookings((prev) =>
                prev.map((booking) =>
                    booking.id === bookingId
                        ? {
                              ...booking,
                              currentHeadcount: data.count,
                              alertLevel: data.level
                          }
                        : booking
                )
            );
        } catch (error) {
            console.error(error);
        }
    };

    return (
        <div className="flex flex-col h-full">
            <div className="flex justify-between items-center mb-5">
                <div>
                    <h2 className="text-xl font-semibold text-gray-800">
                        Today's Bookings
                    </h2>
                    <p className="text-sm text-gray-500">
                        Manage live entry and headcount.
                    </p>
                </div>

                <button
                    onClick={() => navigate("/watchman/scan")}
                    className="hidden sm:block px-4 py-2 bg-indigo-600 text-white rounded-lg font-medium"
                >
                    Scan QR
                </button>
            </div>

            <div className="flex-grow overflow-auto pb-24">
                {loading ? (
                    <div className="animate-pulse space-y-4">
                        <div className="h-24 bg-gray-200 rounded-xl" />
                        <div className="h-24 bg-gray-200 rounded-xl" />
                    </div>
                ) : bookings.length === 0 ? (
                    <div className="bg-white border rounded-xl p-10 text-center text-gray-500">
                        No bookings expected today.
                    </div>
                ) : (
                    <div className="space-y-3">
                        {bookings.map((booking) => (
                            <div
                                key={booking.id}
                                className={`p-4 rounded-xl shadow-sm bg-white border-l-4 ${
                                    booking.status === "CHECKED_IN"
                                        ? "border-green-500"
                                        : "border-blue-500"
                                }`}
                            >
                                <div className="flex justify-between items-start">
                                    <div>
                                        <h3 className="font-bold text-gray-900">
                                            {booking.displayName}
                                        </h3>
                                        <p className="text-sm text-gray-500 font-mono">
                                            {booking.bookingRef}
                                        </p>
                                    </div>

                                    <span className="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">
                                        {booking.status}
                                    </span>
                                </div>

                                <div className="mt-2 text-sm text-gray-600">
                                    {new Date(
                                        booking.startAt
                                    ).toLocaleTimeString([], {
                                        hour: "2-digit",
                                        minute: "2-digit"
                                    })}
                                    {" - "}
                                    {new Date(
                                        booking.endAt
                                    ).toLocaleTimeString([], {
                                        hour: "2-digit",
                                        minute: "2-digit"
                                    })}
                                </div>

                                {booking.status === "CHECKED_IN" && (
                                    <div className="mt-4 pt-3 border-t">
                                        <div className="flex justify-between items-center mb-3">
                                            <div>
                                                <p className="text-xs text-gray-500 uppercase font-semibold">
                                                    Live Headcount
                                                </p>
                                                <p className="text-sm text-gray-700">
                                                    {booking.currentHeadcount || 0}
                                                    {" / "}
                                                    {booking.capacity || "—"}
                                                    {" · Declared "}
                                                    {booking.guestCount || 0}
                                                </p>
                                            </div>

                                            <div className="flex items-center gap-2">
                                                <button
                                                    onClick={() =>
                                                        updateHeadcount(
                                                            booking.id,
                                                            Math.max(
                                                                0,
                                                                (booking.currentHeadcount || 0) - 1
                                                            )
                                                        )
                                                    }
                                                    className="w-8 h-8 rounded-full bg-gray-200 font-bold"
                                                >
                                                    −
                                                </button>

                                                <span className="px-3 py-1 font-mono font-bold rounded bg-gray-100">
                                                    {booking.currentHeadcount || 0}
                                                </span>

                                                <button
                                                    onClick={() =>
                                                        updateHeadcount(
                                                            booking.id,
                                                            (booking.currentHeadcount || 0) + 1
                                                        )
                                                    }
                                                    className="w-8 h-8 rounded-full bg-gray-200 font-bold"
                                                >
                                                    +
                                                </button>
                                            </div>
                                        </div>

                                        {booking.alertLevel === "CRITICAL" && (
                                            <div className="mb-3 p-2 bg-red-100 border border-red-300 rounded text-red-800 text-sm font-bold text-center">
                                                HOLD ENTRY! CALL OWNER!
                                            </div>
                                        )}

                                        <button
                                            onClick={() =>
                                                navigate(
                                                    `/watchman/checkout/${booking.id}`
                                                )
                                            }
                                            className="w-full px-4 py-2 bg-red-100 text-red-700 font-medium rounded-lg text-sm hover:bg-red-200"
                                        >
                                            Checkout & Handover
                                        </button>
                                    </div>
                                )}
                            </div>
                        ))}
                    </div>
                )}
            </div>

            <button
                onClick={() => navigate("/watchman/scan")}
                className="fixed bottom-6 right-6 left-6 bg-indigo-600 text-white py-4 rounded-xl shadow-xl font-bold text-lg flex items-center justify-center hover:bg-indigo-700"
            >
                SCAN QR CODE
            </button>
        </div>
    );
}
