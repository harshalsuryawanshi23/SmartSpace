import { useEffect, useMemo, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { apiClient } from "../../lib/apiClient";
import { toast } from "react-hot-toast";

const EVENT_TYPES = [
    "BIRTHDAY",
    "KITTY_PARTY",
    "MEETING",
    "TUITION_BATCH",
    "FESTIVAL",
    "GET_TOGETHER",
    "BABY_SHOWER",
    "WORKSHOP",
    "OTHER"
];

export default function BookingPage() {
    const [params] = useSearchParams();
    const navigate = useNavigate();

    const hallId = params.get("hallId") || "";
    const slot = params.get("slot") || "10:00";

    const [hall, setHall] = useState<any>(null);
    const [date, setDate] = useState(
        new Date().toISOString().slice(0, 10)
    );
    const [startTime, setStartTime] = useState(slot);
    const [duration, setDuration] = useState(1);
    const [guestCount, setGuestCount] = useState(10);
    const [eventType, setEventType] = useState("OTHER");
    const [eventTitle, setEventTitle] = useState("");
    const [loading, setLoading] = useState(false);
    const [loadingHall, setLoadingHall] = useState(true);
    const [quote, setQuote] = useState<any>(null);

    useEffect(() => {
        const loadHall = async () => {
            if (!hallId) {
                setLoadingHall(false);
                return;
            }

            try {
                const response = await apiClient.get(
                    `/halls/${hallId}`
                );
                setHall(response.data);
            } catch (error) {
                console.error(error);
            } finally {
                setLoadingHall(false);
            }
        };

        loadHall();
    }, [hallId]);

    const startAt = useMemo(
        () => new Date(`${date}T${startTime}:00`),
        [date, startTime]
    );

    const endAt = useMemo(
        () =>
            new Date(
                startAt.getTime() + duration * 60 * 60 * 1000
            ),
        [startAt, duration]
    );

    const getQuote = async () => {
        if (!hallId) {
            toast.error("No hall selected");
            return;
        }

        if (guestCount < 1) {
            toast.error("Guest count must be at least 1");
            return;
        }

        setLoading(true);

        try {
            const response = await apiClient.post(
                "/bookings/quote",
                {
                    hallId,
                    startAt: startAt.toISOString(),
                    endAt: endAt.toISOString(),
                    guestCount
                }
            );

            setQuote(response.data);
            toast.success("Price calculated");
        } catch (error: any) {
            toast.error(
                error?.response?.data?.message ||
                "Unable to calculate booking price"
            );
        } finally {
            setLoading(false);
        }
    };

    const confirmBooking = async () => {
        if (!quote) {
            await getQuote();
            return;
        }

        setLoading(true);

        try {
            const bookingResponse = await apiClient.post(
                "/bookings",
                {
                    idempotencyKey:
                        typeof crypto !== "undefined" &&
                        crypto.randomUUID
                            ? crypto.randomUUID()
                            : `booking-${Date.now()}`,
                    hallId,
                    eventType,
                    eventTitle:
                        eventTitle.trim() ||
                        `${eventType.replace("_", " ")} at ${hall?.name || "SmartSpace"}`,
                    themeTags: [],
                    guestCount,
                    startAt: startAt.toISOString(),
                    endAt: endAt.toISOString()
                }
            );

            const booking = bookingResponse.data;

            if (booking?.publicId) {
                await apiClient.post(
                    `/bookings/${booking.publicId}/mock-pay`
                );
            }

            toast.success("Booking confirmed successfully!");
            navigate("/bookings", { replace: true });
        } catch (error: any) {
            toast.error(
                error?.response?.data?.message ||
                "Booking could not be confirmed"
            );
        } finally {
            setLoading(false);
        }
    };

    if (loadingHall) {
        return (
            <main className="max-w-4xl mx-auto p-8 text-center">
                Loading booking...
            </main>
        );
    }

    if (!hallId) {
        return (
            <main className="max-w-4xl mx-auto p-8 text-center">
                <h1 className="text-xl font-bold">
                    No hall selected
                </h1>
                <button
                    onClick={() => navigate("/search")}
                    className="mt-4 px-5 py-2 bg-blue-600 text-white rounded-lg"
                >
                    Find a Hall
                </button>
            </main>
        );
    }

    return (
        <main className="max-w-4xl mx-auto px-4 py-8">
            <button
                onClick={() => navigate(-1)}
                className="text-blue-600 text-sm mb-5"
            >
                ← Back
            </button>

            <div className="bg-white border rounded-2xl shadow-sm overflow-hidden">
                <div className="bg-gradient-to-r from-blue-700 to-indigo-700 text-white p-6">
                    <p className="text-sm opacity-80">
                        Confirm your booking
                    </p>
                    <h1 className="text-2xl font-bold mt-1">
                        {hall?.name || `Hall #${hallId}`}
                    </h1>
                    <p className="text-sm opacity-90 mt-1">
                        {hall?.locality}, {hall?.city}
                    </p>
                </div>

                <div className="p-6 space-y-6">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Event date
                            </label>
                            <input
                                type="date"
                                value={date}
                                min={new Date()
                                    .toISOString()
                                    .slice(0, 10)}
                                onChange={(e) =>
                                    setDate(e.target.value)
                                }
                                className="w-full border rounded-lg p-2.5"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Start time
                            </label>
                            <input
                                type="time"
                                value={startTime}
                                onChange={(e) =>
                                    setStartTime(e.target.value)
                                }
                                className="w-full border rounded-lg p-2.5"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Duration
                            </label>
                            <select
                                value={duration}
                                onChange={(e) =>
                                    setDuration(Number(e.target.value))
                                }
                                className="w-full border rounded-lg p-2.5"
                            >
                                {[1, 2, 3, 4, 5, 6, 8].map(
                                    (hours) => (
                                        <option
                                            key={hours}
                                            value={hours}
                                        >
                                            {hours} hour
                                            {hours > 1 ? "s" : ""}
                                        </option>
                                    )
                                )}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Guest count
                            </label>
                            <input
                                type="number"
                                min="1"
                                max={hall?.capacityStanding || undefined}
                                value={guestCount}
                                onChange={(e) =>
                                    setGuestCount(
                                        Number(e.target.value)
                                    )
                                }
                                className="w-full border rounded-lg p-2.5"
                            />
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Event type
                            </label>
                            <select
                                value={eventType}
                                onChange={(e) =>
                                    setEventType(e.target.value)
                                }
                                className="w-full border rounded-lg p-2.5"
                            >
                                {EVENT_TYPES.map((type) => (
                                    <option key={type} value={type}>
                                        {type.replaceAll("_", " ")}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div>
                            <label className="block text-sm font-medium mb-1">
                                Event title
                            </label>
                            <input
                                value={eventTitle}
                                onChange={(e) =>
                                    setEventTitle(e.target.value)
                                }
                                placeholder="e.g. Harshal's Birthday"
                                className="w-full border rounded-lg p-2.5"
                            />
                        </div>
                    </div>

                    <div className="bg-gray-50 rounded-xl p-5">
                        <h2 className="font-semibold text-gray-800">
                            Booking summary
                        </h2>

                        <div className="mt-3 text-sm text-gray-600 space-y-1">
                            <p>
                                {startAt.toLocaleString()} →{" "}
                                {endAt.toLocaleTimeString()}
                            </p>
                            <p>
                                {guestCount} guests · {duration} hour
                                {duration > 1 ? "s" : ""}
                            </p>
                        </div>
                    </div>

                    {quote && (
                        <div className="border rounded-xl p-5">
                            <h2 className="font-semibold text-gray-800 mb-3">
                                Price breakdown
                            </h2>

                            <div className="space-y-2 text-sm">
                                <div className="flex justify-between">
                                    <span>Base price</span>
                                    <span>₹{quote.basePrice}</span>
                                </div>

                                <div className="flex justify-between">
                                    <span>Member discount</span>
                                    <span>-₹{quote.memberDiscount}</span>
                                </div>

                                <div className="flex justify-between">
                                    <span>Platform fee</span>
                                    <span>₹{quote.platformFee}</span>
                                </div>

                                <div className="flex justify-between">
                                    <span>Tax</span>
                                    <span>₹{quote.tax}</span>
                                </div>

                                <div className="border-t pt-3 flex justify-between font-bold text-lg">
                                    <span>Total</span>
                                    <span>
                                        ₹{quote.total}{" "}
                                        {quote.currency || "INR"}
                                    </span>
                                </div>
                            </div>
                        </div>
                    )}

                    <div className="flex flex-col sm:flex-row gap-3 justify-end">
                        <button
                            onClick={getQuote}
                            disabled={loading}
                            className="px-5 py-3 border border-blue-600 text-blue-600 rounded-lg font-semibold disabled:opacity-50"
                        >
                            {loading
                                ? "Please wait..."
                                : "Calculate Price"}
                        </button>

                        <button
                            onClick={confirmBooking}
                            disabled={loading}
                            className="px-5 py-3 bg-blue-600 text-white rounded-lg font-semibold hover:bg-blue-700 disabled:opacity-50"
                        >
                            {quote
                                ? "Confirm & Pay"
                                : "Get Quote First"}
                        </button>
                    </div>

                    <p className="text-xs text-gray-400 text-center">
                        Demo payment uses SmartSpace mock payment
                        confirmation.
                    </p>
                </div>
            </div>
        </main>
    );
}
