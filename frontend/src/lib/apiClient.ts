import axios from 'axios';

const API_URL =
    import.meta.env.VITE_API_URL ||
    'http://localhost:8080/api/v1';

export const apiClient = axios.create({
    baseURL: API_URL,
    withCredentials: true,
    headers: {
        'Content-Type': 'application/json'
    }
});

let currentAccessToken: string | null = null;

export const setAccessToken = (token: string | null) => {
    currentAccessToken = token;
};

export const getAccessToken = () => currentAccessToken;

let refreshPromise: Promise<string> | null = null;

apiClient.interceptors.request.use(
    (config) => {
        if (currentAccessToken) {
            config.headers = config.headers || {};
            config.headers.Authorization =
                `Bearer ${currentAccessToken}`;
        }

        return config;
    },
    (error) => Promise.reject(error)
);

apiClient.interceptors.response.use(
    (response) => response,

    async (error) => {
        const originalRequest = error.config;

        if (!originalRequest) {
            return Promise.reject(error);
        }

        if (error.response?.status !== 401) {
            return Promise.reject(error);
        }

        if (originalRequest._retry) {
            return Promise.reject(error);
        }

        originalRequest._retry = true;

        try {
            if (!refreshPromise) {
                refreshPromise = axios
                    .post(
                        `${API_URL}/auth/refresh`,
                        {},
                        { withCredentials: true }
                    )
                    .then((response) => {
                        const newToken =
                            response.data?.accessToken;

                        if (!newToken) {
                            throw new Error(
                                'Refresh response did not contain an access token'
                            );
                        }

                        setAccessToken(newToken);
                        return newToken;
                    })
                    .finally(() => {
                        refreshPromise = null;
                    });
            }

            const newAccessToken = await refreshPromise;

            originalRequest.headers =
                originalRequest.headers || {};

            originalRequest.headers.Authorization =
                `Bearer ${newAccessToken}`;

            window.dispatchEvent(
                new CustomEvent('auth:refresh', {
                    detail: newAccessToken
                })
            );

            return apiClient(originalRequest);
        } catch (refreshError) {
            setAccessToken(null);

            window.dispatchEvent(
                new Event('auth:logout')
            );

            return Promise.reject(refreshError);
        }
    }
);
