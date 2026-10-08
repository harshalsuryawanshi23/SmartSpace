import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import { AuthProvider } from "./context/AuthContext";
import { AuthGuard } from "./components/AuthGuard";
import { Navbar } from "./components/common/Navbar";

import { Login } from "./pages/Login";
import { Register } from "./pages/Register";
import { VerifyOtp } from "./pages/VerifyOtp";
import { ForgotPassword } from "./pages/ForgotPassword";
import { Dashboard } from "./pages/Dashboard";
import { ForceChangePassword } from "./pages/ForceChangePassword";

import Home from "./pages/discovery/Home";
import Search from "./pages/discovery/Search";
import HallDetail from "./pages/discovery/HallDetail";
import BookingPage from "./pages/booking/BookingPage";
import MyBookings from "./pages/booking/MyBookings";

import { KycIntro } from "./pages/kyc/KycIntro";
import { KycMockProvider } from "./pages/kyc/KycMockProvider";
import { KycStatus } from "./pages/kyc/KycStatus";

import WatchmanLayout from "./layouts/WatchmanLayout";
import WatchHome from "./pages/watchman/WatchHome";
import Scanner from "./pages/watchman/Scanner";
import Verdict from "./pages/watchman/Verdict";
import CheckoutForm from "./pages/watchman/CheckoutForm";

import DecoratorDashboard from "./pages/decorator/DecoratorDashboard";

function App() {
    return (
        <AuthProvider>
            <BrowserRouter>
                <div className="min-h-screen flex flex-col bg-gray-50">
                    <Navbar />

                    <div className="flex-grow">
                        <Routes>
                            <Route path="/login" element={<Login />} />
                            <Route path="/register" element={<Register />} />
                            <Route path="/verify-otp" element={<VerifyOtp />} />
                            <Route path="/forgot-password" element={<ForgotPassword />} />

                            <Route path="/" element={<Navigate to="/login" replace />} />
                            <Route path="/home" element={<Home />} />
                            <Route path="/search" element={<Search />} />
                            <Route path="/halls/:id" element={<HallDetail />} />

                            <Route element={<AuthGuard />}>
                                <Route path="/dashboard" element={<Dashboard />} />

                                <Route
                                    path="/force-change-password"
                                    element={<ForceChangePassword />}
                                />

                                <Route path="/booking" element={<BookingPage />} />
                                <Route path="/bookings" element={<MyBookings />} />

                                <Route path="/kyc" element={<KycIntro />} />
                                <Route path="/kyc/mock" element={<KycMockProvider />} />
                                <Route path="/kyc/status" element={<KycStatus />} />

                                <Route path="/watchman" element={<WatchmanLayout />}>
                                    <Route index element={<WatchHome />} />
                                    <Route path="scan" element={<Scanner />} />
                                    <Route path="verdict" element={<Verdict />} />
                                    <Route
                                        path="checkout/:id"
                                        element={<CheckoutForm />}
                                    />
                                </Route>

                                <Route
                                    path="/decorator/dashboard"
                                    element={<DecoratorDashboard />}
                                />
                            </Route>

                            <Route
                                path="*"
                                element={<Navigate to="/login" replace />}
                            />
                        </Routes>
                    </div>
                </div>
            </BrowserRouter>
        </AuthProvider>
    );
}

export default App;
