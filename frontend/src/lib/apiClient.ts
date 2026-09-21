import axios from 'axios';

const API_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1';

export const apiClient = axios.create({
    baseURL: API_URL,
    withCredentials: true, // Important for refresh token cookie
});

// We can intercept requests to attach access token later if we store it in memory/zustand
// For now, let's assume we handle it in the AuthProvider or keep the token in apiClient instance

let currentAccessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
    currentAccessToken = token;
};

apiClient.interceptors.request.use((config) => {
    if (currentAccessToken) {
        config.headers.Authorization = `Bearer ${currentAccessToken}`;
    }
    return config;
});

apiClient.interceptors.response.use(
    (response) => response,
    async (error) => {
        const originalRequest = error.config;
        if (error.response?.status === 401 && !originalRequest._retry) {
            originalRequest._retry = true;
            try {
                // Call refresh endpoint to get new access token
                const res = await axios.post(`${API_URL}/auth/refresh`, {}, { withCredentials: true });
                const { accessToken } = res.data;
                setAccessToken(accessToken);
                // Dispatch event or callback to update React state if needed
                window.dispatchEvent(new CustomEvent('auth:refresh', { detail: accessToken }));
                
                originalRequest.headers.Authorization = `Bearer ${accessToken}`;
                return apiClient(originalRequest);
            } catch (refreshError) {
                // Refresh failed (cookie expired or missing)
                setAccessToken(null);
                window.dispatchEvent(new Event('auth:logout'));
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);
