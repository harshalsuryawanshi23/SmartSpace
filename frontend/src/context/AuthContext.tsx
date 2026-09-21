import React, { createContext, useContext, useEffect, useState } from 'react';
import { apiClient, setAccessToken } from '../lib/apiClient';

interface User {
    id: string; // publicId
    firstName: string;
    lastName: string;
    email: string | null;
    phone: string | null;
    role: string;
    status: string;
    preferredLanguage: string;
    profilePhotoUrl: string | null;
}

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (accessToken: string) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const loadUser = async () => {
        try {
            const res = await apiClient.get('/users/me');
            setUser(res.data);
        } catch (error) {
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        // Assume we don't have access token on hard refresh, so apiClient will attempt to refresh immediately
        // upon the first 401. But actually we should just try to get /users/me, and if it fails with 401,
        // our interceptor will try to refresh.
        loadUser();

        const handleLogout = () => {
            setUser(null);
        };
        const handleRefresh = () => {
            if (!user) loadUser();
        };

        window.addEventListener('auth:logout', handleLogout);
        window.addEventListener('auth:refresh', handleRefresh);

        return () => {
            window.removeEventListener('auth:logout', handleLogout);
            window.removeEventListener('auth:refresh', handleRefresh);
        };
    }, []);

    const login = async (accessToken: string) => {
        setAccessToken(accessToken);
        await loadUser();
    };

    const logout = async () => {
        try {
            await apiClient.post('/auth/logout');
        } catch (error) {
            console.error('Logout failed', error);
        } finally {
            setAccessToken(null);
            setUser(null);
        }
    };

    return (
        <AuthContext.Provider value={{ user, isAuthenticated: !!user, isLoading, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within an AuthProvider');
    return context;
};
