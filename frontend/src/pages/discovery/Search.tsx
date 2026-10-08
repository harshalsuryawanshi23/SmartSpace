import React, { useEffect, useMemo, useState } from "react";
import { useSearchParams, Link } from "react-router-dom";
import { TrustBadge } from "../../components/trust/TrustBadge";
import { NLSearchBar } from "../../components/discovery/NLSearchBar";
import { apiClient } from "../../lib/apiClient";

export default function Search() {
    const [searchParams, setSearchParams] = useSearchParams();

    const [halls, setHalls] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");
    const [nlFilters, setNlFilters] = useState<any>({});

    const [maxPrice, setMaxPrice] = useState(10000);
    const [amenities, setAmenities] = useState<string[]>([]);

    useEffect(() => {
        const fetchHalls = async () => {
            setLoading(true);
            setError("");

            try {
                const params = new URLSearchParams();

                if (searchParams.get("guests")) {
                    params.append("guests", searchParams.get("guests")!);
                }

                if (searchParams.get("date")) {
                    params.append("date", searchParams.get("date")!);
                }

                const response = await apiClient.get(
                    `/halls/search?${params.toString()}`
                );

                const data = Array.isArray(response.data)
                    ? response.data
                    : response.data?.content || [];

                setHalls(data);
            } catch (err) {
                console.error(err);
                setError(
                    "Unable to load halls. Please make sure the backend is running."
                );
                setHalls([]);
            } finally {
                setLoading(false);
            }
        };

        fetchHalls();
    }, [searchParams]);

    const filteredHalls = useMemo(() => {
        return halls.filter((hall) => {
            const price = Number(hall.basePricePerHour || 0);

            if (price > maxPrice) {
                return false;
            }

            return amenities.every((amenity) => {
                if (amenity === "AC") return hall.hasAc;
                if (amenity === "Parking") return hall.hasParking;
                if (amenity === "Kitchen") return hall.hasKitchen;
                return true;
            });
        });
    }, [halls, maxPrice, amenities]);

    const toggleAmenity = (amenity: string) => {
        setAmenities((current) =>
            current.includes(amenity)
                ? current.filter((item) => item !== amenity)
                : [...current, amenity]
        );
    };

    return (
        <main className="max-w-7xl mx-auto py-6 px-4 sm:px-6 lg:px-8">
            <div className="mb-6">
                <h1 className="text-2xl font-bold text-gray-900">
                    Find Event Halls
                </h1>
                <p className="text-sm text-gray-500 mt-1">
                    Search available spaces for your event.
                </p>
            </div>

            <NLSearchBar
                onFiltersParsed={(filters) => {
                    setNlFilters(filters);

                    const params = new URLSearchParams(
                        searchParams.toString()
                    );

                    if (filters.guests) {
                        params.set(
                            "guests",
                            String(filters.guests)
                        );
                    }

                    if (filters.date) {
                        params.set("date", filters.date);
                    }

                    setSearchParams(params);
                }}
            />

            {Object.keys(nlFilters).length > 0 && (
                <div className="mt-4 flex flex-wrap gap-2">
                    {Object.entries(nlFilters).map(([key, value]) => {
                        if (!value) return null;
                        if (Array.isArray(value) && value.length === 0) {
                            return null;
                        }

                        return (
                            <span
                                key={key}
                                className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-blue-100 text-blue-800"
                            >
                                {key}:{" "}
                                {Array.isArray(value)
                                    ? value.join(", ")
                                    : String(value)}

                                <button
                                    type="button"
                                    onClick={() => {
                                        const next = {
                                            ...nlFilters
                                        };

                                        delete next[key];
                                        setNlFilters(next);
                                    }}
                                    className="ml-2 font-bold"
                                >
                                    ×
                                </button>
                            </span>
                        );
                    })}
                </div>
            )}

            <div className="mt-6 grid grid-cols-1 lg:grid-cols-4 gap-6">
                <aside className="bg-white rounded-xl border p-5 h-fit shadow-sm">
                    <h2 className="font-semibold text-gray-900">
                        Filters
                    </h2>

                    <div className="mt-5">
                        <div className="flex justify-between text-sm mb-2">
                            <label>Max hourly price</label>
                            <span className="font-medium">
                                ₹{maxPrice}
                            </span>
                        </div>

                        <input
                            type="range"
                            min="500"
                            max="10000"
                            step="500"
                            value={maxPrice}
                            onChange={(e) =>
                                setMaxPrice(Number(e.target.value))
                            }
                            className="w-full"
                        />
                    </div>

                    <div className="mt-6">
                        <label className="text-sm font-medium">
                            Amenities
                        </label>

                        <div className="mt-3 space-y-3">
                            {["AC", "Parking", "Kitchen"].map(
                                (amenity) => (
                                    <label
                                        key={amenity}
                                        className="flex items-center gap-2 text-sm"
                                    >
                                        <input
                                            type="checkbox"
                                            checked={amenities.includes(amenity)}
                                            onChange={() =>
                                                toggleAmenity(amenity)
                                            }
                                        />
                                        {amenity}
                                    </label>
                                )
                            )}
                        </div>
                    </div>

                    <button
                        onClick={() => {
                            setMaxPrice(10000);
                            setAmenities([]);
                        }}
                        className="mt-6 text-sm text-blue-600 font-medium"
                    >
                        Clear filters
                    </button>
                </aside>

                <section className="lg:col-span-3">
                    <div className="flex justify-between items-center mb-4">
                        <h2 className="font-semibold text-gray-900">
                            Results ({filteredHalls.length})
                        </h2>
                    </div>

                    {loading && (
                        <div className="bg-white rounded-xl border p-10 text-center">
                            Loading halls...
                        </div>
                    )}

                    {!loading && error && (
                        <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl p-5">
                            {error}
                        </div>
                    )}

                    {!loading && !error && filteredHalls.length === 0 && (
                        <div className="bg-white rounded-xl border p-10 text-center text-gray-500">
                            No halls match the selected filters.
                        </div>
                    )}

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
                        {filteredHalls.map((hall) => (
                            <div
                                key={hall.id}
                                className="bg-white rounded-xl border shadow-sm overflow-hidden"
                            >
                                <div className="p-5">
                                    <div className="flex justify-between gap-3">
                                        <div>
                                            <h3 className="text-lg font-semibold text-gray-900">
                                                {hall.name}
                                            </h3>
                                            <p className="text-sm text-gray-500 mt-1">
                                                {hall.locality}, {hall.city}
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
                                            showLabel={false}
                                        />
                                    </div>

                                    <div className="mt-4 grid grid-cols-2 gap-3 text-sm">
                                        <div className="bg-gray-50 rounded-lg p-3">
                                            <span className="text-gray-500">
                                                Price
                                            </span>
                                            <p className="font-semibold">
                                                ₹{hall.basePricePerHour}/hr
                                            </p>
                                        </div>

                                        <div className="bg-gray-50 rounded-lg p-3">
                                            <span className="text-gray-500">
                                                Capacity
                                            </span>
                                            <p className="font-semibold">
                                                {hall.capacityStanding} guests
                                            </p>
                                        </div>
                                    </div>

                                    <div className="mt-4 flex flex-wrap gap-2 text-xs">
                                        {hall.hasAc && (
                                            <span className="px-2 py-1 bg-blue-50 text-blue-700 rounded">
                                                AC
                                            </span>
                                        )}
                                        {hall.hasParking && (
                                            <span className="px-2 py-1 bg-green-50 text-green-700 rounded">
                                                Parking
                                            </span>
                                        )}
                                        {hall.hasKitchen && (
                                            <span className="px-2 py-1 bg-amber-50 text-amber-700 rounded">
                                                Kitchen
                                            </span>
                                        )}
                                    </div>

                                    <Link
                                        to={`/halls/${hall.id}`}
                                        className="block mt-5 text-center bg-blue-600 text-white py-2.5 rounded-lg font-medium hover:bg-blue-700"
                                    >
                                        View Details →
                                    </Link>
                                </div>
                            </div>
                        ))}
                    </div>
                </section>
            </div>
        </main>
    );
}
