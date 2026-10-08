import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/AuthContext";
import { apiClient as api } from "../lib/apiClient";
import { toast } from "react-hot-toast";
import LiveStatusWidget from "../components/owner/LiveStatusWidget";
import { PrivacyDashboard } from "../components/trust/PrivacyDashboard";
import { OwnerDashboard } from "./owner/OwnerDashboard";
import { AdminConsole } from "./admin/AdminConsole";

const ActionCard = ({
    title,
    description,
    button,
    onClick,
    icon
}: {
    title: string;
    description: string;
    button: string;
    onClick: () => void;
    icon: string;
}) => (
    <button
        onClick={onClick}
        className="text-left bg-white border border-gray-200 rounded-xl p-5 shadow-sm hover:shadow-md hover:border-blue-300 transition"
    >
        <div className="text-2xl mb-3">{icon}</div>
        <h3 className="font-semibold text-gray-900">{title}</h3>
        <p className="text-sm text-gray-500 mt-1">{description}</p>
        <span className="inline-block mt-4 text-sm font-semibold text-blue-600">
            {button} →
        </span>
    </button>
);

export const Dashboard = () => {
    const { user } = useAuth();
    const navigate = useNavigate();

    const [disputes, setDisputes] = useState<any[]>([]);
    const [showRaiseDispute, setShowRaiseDispute] = useState(false);
    const [newDispute, setNewDispute] = useState({
        bookingRef: "",
        type: "DAMAGE_CLAIM",
        description: ""
    });

    const role = user?.role || "RESIDENT";

    useEffect(() => {
        if (role === "RESIDENT") {
            api.get("/disputes/my")
                .then((res) =>
                    setDisputes(
                        Array.isArray(res.data)
                            ? res.data
                            : res.data?.content || []
                    )
                )
                .catch(() => setDisputes([]));
        }
    }, [role]);

    const handleRaiseDispute = async (e: React.FormEvent) => {
        e.preventDefault();

        try {
            await api.post("/disputes", newDispute);
            toast.success("Dispute raised successfully");
            setShowRaiseDispute(false);
            setNewDispute({
                bookingRef: "",
                type: "DAMAGE_CLAIM",
                description: ""
            });

            const response = await api.get("/disputes/my");
            setDisputes(
                Array.isArray(response.data)
                    ? response.data
                    : response.data?.content || []
            );
        } catch {
            toast.error("Failed to raise dispute");
        }
    };

    if (role === "HALL_OWNER") {
        return <OwnerDashboard />;
    }

    if (role === "ADMIN") {
        return <AdminConsole />;
    }

    if (role === "WATCHMAN") {
        return (
            <div className="max-w-7xl mx-auto px-4 py-8">
                <div className="bg-gradient-to-r from-indigo-600 to-blue-600 rounded-2xl p-8 text-white">
                    <p className="text-sm opacity-80 uppercase tracking-wide">
                        Watchman Console
                    </p>
                    <h1 className="text-3xl font-bold mt-2">
                        Welcome, {user?.firstName || "Watchman"}
                    </h1>
                    <p className="mt-2 opacity-90">
                        Manage QR entry, live headcount and checkout.
                    </p>
                    <button
                        onClick={() => navigate("/watchman")}
                        className="mt-6 bg-white text-blue-700 px-5 py-2.5 rounded-lg font-semibold"
                    >
                        Open Watchman Console →
                    </button>
                </div>
            </div>
        );
    }

    return (
        <main className="max-w-7xl mx-auto px-4 py-8">
            <section className="bg-gradient-to-r from-blue-700 to-indigo-700 rounded-2xl p-7 md:p-9 text-white shadow-lg">
                <p className="text-sm uppercase tracking-wider opacity-80">
                    Resident Dashboard
                </p>

                <div className="flex flex-col md:flex-row md:items-end md:justify-between gap-5">
                    <div>
                        <h1 className="text-3xl md:text-4xl font-bold mt-2">
                            Welcome, {user?.firstName || user?.fullName || "Resident"}!
                        </h1>
                        <p className="mt-2 opacity-90">
                            Find a hall, manage bookings and keep your event secure.
                        </p>
                    </div>

                    <button
                        onClick={() => navigate("/search")}
                        className="bg-white text-blue-700 px-5 py-3 rounded-lg font-semibold hover:bg-blue-50"
                    >
                        Find a Hall →
                    </button>
                </div>
            </section>

            <section className="mt-7 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
                <ActionCard
                    icon="🏛️"
                    title="Find Halls"
                    description="Search available event spaces by capacity and location."
                    button="Browse halls"
                    onClick={() => navigate("/search")}
                />

                <ActionCard
                    icon="📅"
                    title="My Bookings"
                    description="View confirmed, pending and completed bookings."
                    button="Open bookings"
                    onClick={() => navigate("/bookings")}
                />

                <ActionCard
                    icon="🪪"
                    title="KYC"
                    description="Check or complete your identity verification."
                    button="Open KYC"
                    onClick={() => navigate("/kyc")}
                />

                <ActionCard
                    icon="🛡️"
                    title="Privacy"
                    description="Review your SmartSpace privacy controls."
                    button="Privacy dashboard"
                    onClick={() =>
                        document
                            .getElementById("privacy-section")
                            ?.scrollIntoView({ behavior: "smooth" })
                    }
                />
            </section>

            <section className="mt-8 grid grid-cols-1 lg:grid-cols-3 gap-6">
                <div className="lg:col-span-2 bg-white rounded-xl border shadow-sm p-6">
                    <div className="flex justify-between items-center mb-5">
                        <div>
                            <h2 className="text-xl font-semibold text-gray-800">
                                My Disputes
                            </h2>
                            <p className="text-sm text-gray-500">
                                Track issues related to your bookings.
                            </p>
                        </div>

                        <button
                            onClick={() => setShowRaiseDispute(true)}
                            className="bg-red-600 text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-red-700"
                        >
                            Raise Dispute
                        </button>
                    </div>

                    {disputes.length === 0 ? (
                        <div className="text-center py-10 text-gray-500 border-2 border-dashed border-gray-100 rounded-lg">
                            No active disputes.
                        </div>
                    ) : (
                        <div className="space-y-3">
                            {disputes.map((d) => (
                                <div
                                    key={d.id}
                                    className="p-4 bg-gray-50 border rounded-lg flex justify-between gap-4"
                                >
                                    <div>
                                        <p className="font-medium">
                                            {d.type} · {d.bookingRef}
                                        </p>
                                        <p className="text-sm text-gray-500 mt-1">
                                            {d.description}
                                        </p>
                                    </div>
                                    <span className="h-fit text-xs font-semibold px-2 py-1 bg-yellow-100 text-yellow-800 rounded-full">
                                        {d.status}
                                    </span>
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                <div>
                    <h2 className="text-lg font-bold text-gray-800 mb-3">
                        Live Halls
                    </h2>
                    <LiveStatusWidget />
                </div>
            </section>

            <section
                id="privacy-section"
                className="mt-8"
            >
                <PrivacyDashboard />
            </section>

            {showRaiseDispute && (
                <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
                    <div className="bg-white p-6 rounded-xl w-full max-w-md shadow-xl">
                        <h3 className="text-lg font-bold mb-4">
                            Raise a Dispute
                        </h3>

                        <form onSubmit={handleRaiseDispute}>
                            <input
                                required
                                value={newDispute.bookingRef}
                                onChange={(e) =>
                                    setNewDispute({
                                        ...newDispute,
                                        bookingRef: e.target.value
                                    })
                                }
                                className="w-full border rounded-lg p-2.5 mb-3"
                                placeholder="Booking reference"
                            />

                            <select
                                value={newDispute.type}
                                onChange={(e) =>
                                    setNewDispute({
                                        ...newDispute,
                                        type: e.target.value
                                    })
                                }
                                className="w-full border rounded-lg p-2.5 mb-3"
                            >
                                <option value="DAMAGE_CLAIM">Damage Claim</option>
                                <option value="MISREPRESENTATION">Misrepresentation</option>
                                <option value="CANCELLATION_FEE">Cancellation Fee</option>
                            </select>

                            <textarea
                                required
                                value={newDispute.description}
                                onChange={(e) =>
                                    setNewDispute({
                                        ...newDispute,
                                        description: e.target.value
                                    })
                                }
                                className="w-full border rounded-lg p-2.5"
                                rows={4}
                                placeholder="Describe the issue"
                            />

                            <div className="flex justify-end gap-3 mt-5">
                                <button
                                    type="button"
                                    onClick={() => setShowRaiseDispute(false)}
                                    className="px-4 py-2 rounded-lg border"
                                >
                                    Cancel
                                </button>

                                <button
                                    type="submit"
                                    className="px-4 py-2 rounded-lg bg-red-600 text-white"
                                >
                                    Submit
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </main>
    );
};
