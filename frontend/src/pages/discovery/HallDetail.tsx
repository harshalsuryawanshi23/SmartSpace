import React, { useEffect, useState } from "react";
import { useNavigate, useParams, Link } from "react-router-dom";
import SlotGrid from "../../components/discovery/SlotGrid";
import { TrustBadge } from "../../components/trust/TrustBadge";
import DecoratorMatches from "../../components/discovery/DecoratorMatches";
import { apiClient } from "../../lib/apiClient";

export default function HallDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [hall, setHall] = useState<any>(null);
    const [loading, setLoading] = useState(true);
    const [selectedSlots, setSelectedSlots] = useState<string[]>([]);

    const mockSlots = [
        { time: "09:00", state: "PAST" as const },
        { time: "09:30", state: "PAST" as const },
        { time: "10:00", state: "FREE" as const },
        { time: "10:30", state: "FREE" as const },
        { time: "11:00", state: "FREE" as const },
        { time: "11:30", state: "FREE" as const },
        { time: "12:00", state: "BOOKED" as const },
        { time: "12:30", state: "BOOKED" as const },
        { time: "13:00", state: "BUFFER" as const },
        { time: "13:30", state: "FREE" as const },
        { time: "14:00", state: "FREE" as const },
        { time: "14:30", state: "CLOSED" as const }
    ];

    useEffect(() => {
        const fetchHall = async () => {
            setLoading(true);

            try {
                const response = await apiClient.get(`/halls/${id}`);
                setHall(response.data);
            } catch (error) {
                console.error(error);
                setHall(null);
            } finally {
                setLoading(false);
            }
        };

        if (id) {
            fetchHall();
        }
    }, [id]);

    const handleBooking = () => {
        if (!id || selectedSlots.length === 0) {
            return;
        }

        navigate(
            `/booking?hallId=${encodeURIComponent(id)}&slot=${encodeURIComponent(
                selectedSlots[0]
            )}`
        );
    };

    if (loading) {
        return (
            <main className="max-w-5xl mx-auto p-8 text-center">
                Loading hall...
            </main>
        );
    }

    if (!hall) {
        return (
            <main className="max-w-5xl mx-auto p-8 text-center">
                <h1 className="text-xl font-semibold">
                    Hall not found
                </h1>
                <Link
                    to="/search"
                    className="inline-block mt-4 text-blue-600"
                >
                    Back to search
                </Link>
            </main>
        );
    }

    return (
        <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
            <Link
                to="/search"
                className="text-blue-600 hover:text-blue-800"
            >
                ← Back to Search
            </Link>

            <div className="mt-4 bg-white shadow-sm border rounded-xl overflow-hidden">
                <div className="px-5 py-6 md:px-7 flex flex-col md:flex-row justify-between gap-5">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-900">
                            {hall.name}
                        </h1>

                        <p className="mt-2 text-sm text-gray-500">
                            {hall.locality}, {hall.city}
                            {" · "}
                            ₹{hall.basePricePerHour}/hr
                            {" · "}
                            {hall.capacityStanding} guests max
                        </p>
                    </div>

                    <TrustBadge
                        score={73.9}
                        badge="TRUSTED"
                        componentsJson={JSON.stringify({
                            priorMean: 0.7,
                            sumWeightedScore: 6.8,
                            sumWeight: 8,
                            penalty: 0.0667
                        })}
                    />
                </div>

                <div className="border-t px-5 py-6 md:px-7">
                    <div className="grid md:grid-cols-3 gap-6">
                        <div className="md:col-span-2">
                            <h2 className="font-semibold text-gray-800">
                                About this hall
                            </h2>

                            <p className="mt-2 text-sm text-gray-600">
                                {hall.description ||
                                    "A convenient event space managed through SmartSpace."}
                            </p>

                            <h3 className="font-semibold text-gray-800 mt-6">
                                Amenities
                            </h3>

                            <div className="mt-3 flex flex-wrap gap-2">
                                {hall.hasAc && (
                                    <span className="px-3 py-1 bg-blue-50 text-blue-700 rounded-full text-sm">
                                        Air Conditioning
                                    </span>
                                )}
                                {hall.hasParking && (
                                    <span className="px-3 py-1 bg-green-50 text-green-700 rounded-full text-sm">
                                        Parking
                                    </span>
                                )}
                                {hall.hasKitchen && (
                                    <span className="px-3 py-1 bg-amber-50 text-amber-700 rounded-full text-sm">
                                        Kitchen
                                    </span>
                                )}
                                {hall.hasStage && (
                                    <span className="px-3 py-1 bg-purple-50 text-purple-700 rounded-full text-sm">
                                        Stage
                                    </span>
                                )}
                                {hall.hasPowerBackup && (
                                    <span className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm">
                                        Power Backup
                                    </span>
                                )}
                            </div>
                        </div>

                        <div className="bg-gray-50 rounded-xl p-5">
                            <p className="text-sm text-gray-500">
                                Starting price
                            </p>
                            <p className="text-3xl font-bold text-gray-900 mt-1">
                                ₹{hall.basePricePerHour}
                            </p>
                            <p className="text-sm text-gray-500">
                                per hour
                            </p>

                            <div className="mt-5">
                                <p className="text-sm font-medium text-gray-700">
                                    Selected slots
                                </p>
                                <p className="text-2xl font-bold text-blue-600 mt-1">
                                    {selectedSlots.length}
                                </p>
                            </div>
                        </div>
                    </div>

                    <div className="mt-8">
                        <h2 className="font-semibold text-gray-800 mb-3">
                            Select an available slot
                        </h2>

                        <SlotGrid
                            slots={mockSlots}
                            onSelect={(time) => {
                                setSelectedSlots((prev) =>
                                    prev.includes(time)
                                        ? prev.filter((t) => t !== time)
                                        : [...prev, time]
                                );
                            }}
                        />
                    </div>
                </div>
            </div>

            {selectedSlots.some((time) => {
                const [hour] = time.split(":").map(Number);
                return hour >= 22 || hour < 6;
            }) && (
                <div className="mt-4 bg-indigo-50 border border-indigo-200 p-4 rounded-xl">
                    <p className="text-sm text-indigo-800">
                        <strong>Quiet Hours:</strong> No loud music or
                        excessive noise is permitted after 10 PM.
                    </p>
                </div>
            )}

            {id && (
                <div className="mt-6">
                    <DecoratorMatches hallId={id} />
                </div>
            )}

            <div className="mt-6 flex justify-end">
                <button
                    onClick={handleBooking}
                    disabled={selectedSlots.length === 0}
                    className="bg-blue-600 text-white px-7 py-3 rounded-lg font-semibold shadow hover:bg-blue-700 disabled:opacity-50 disabled:cursor-not-allowed"
                >
                    Continue to Booking →
                </button>
            </div>
        </main>
    );
}
