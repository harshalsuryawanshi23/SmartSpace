import React, {
    createContext,
    useContext,
    useEffect,
    useState
} from 'react';

import {
    apiClient,
    setAccessToken
} from '../lib/apiClient';

interface User {
    id: string;
    firstName: string;
    lastName: string;
    fullName: string;
    email: string | null;
    phone: string | null;
    role: string;
    roles: string[];
}

interface AuthContextType {
    user: User | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (accessToken: string) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | null>(null);

export const AuthProvider = ({
    children
}: {
    children: React.ReactNode;
}) => {
    const [user, setUser] = useState<User | null>(null);
    const [isLoading, setIsLoading] = useState(true);

    const normalizeUser = (backendUser: any): User => {
        const fullName = backendUser?.fullName || '';
        const nameParts = fullName.trim().split(/\s+/).filter(Boolean);

        const roles: string[] = Array.isArray(backendUser?.roles)
            ? backendUser.roles.map((role: any) =>
                typeof role === 'string'
                    ? role
                    : role?.name || String(role)
            )
            : [];

        return {
            id: backendUser?.publicId || '',
            fullName,
            firstName: nameParts[0] || '',
            lastName: nameParts.slice(1).join(' '),
            email: backendUser?.email || null,
            phone: backendUser?.phone || null,
            roles,
            role: roles[0] || 'RESIDENT'
        };
    };

    const loadUser = async () => {
        try {
            const response = await apiClient.get('/users/me');
            setUser(normalizeUser(response.data));
        } catch (error) {
            console.error('Failed to load current user:', error);
            setUser(null);
        } finally {
            setIsLoading(false);
        }
    };

    useEffect(() => {
        loadUser();

        const handleLogout = () => {
            setAccessToken(null);
            setUser(null);
            setIsLoading(false);
        };

        const handleRefresh = async (event: Event) => {
            const customEvent = event as CustomEvent<string>;
            const refreshedToken = customEvent.detail;

            if (refreshedToken) {
                setAccessToken(refreshedToken);
                await loadUser();
            }
        };

        window.addEventListener('auth:logout', handleLogout);
        window.addEventListener('auth:refresh', handleRefresh);

        return () => {
            window.removeEventListener('auth:logout', handleLogout);
            window.removeEventListener('auth:refresh', handleRefresh);
        };
    }, []);

    const login = async (accessToken: string) => {
        if (!accessToken) {
            throw new Error('No access token received from login');
        }

        setAccessToken(accessToken);
        setIsLoading(true);
        await loadUser();
    };

    const logout = async () => {
        try {
            await apiClient.post('/auth/logout');
        } catch (error) {
            console.error('Logout request failed:', error);
        } finally {
            setAccessToken(null);
            setUser(null);
            setIsLoading(false);
        }
    };

    return (
        <AuthContext.Provider
            value={{
                user,
                isAuthenticated: !!user,
                isLoading,
                login,
                logout
            }}
        >
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);

    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }

    return context;
};
