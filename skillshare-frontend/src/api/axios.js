import axios from 'axios';

const api = axios.create({
    baseURL: '', // Using relative URL to leverage Vite proxy
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request interceptor to add auth token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor for error handling (optional, e.g., auto-logout on 401)
api.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle specific status codes
        if (error.response) {
            if (error.response.status === 401) {
                // Unauthorized - Clear token and redirect to login
                localStorage.removeItem('token');
                localStorage.removeItem('refreshToken');
                if (!window.location.pathname.includes('/login')) {
                    window.location.href = '/login';
                }
            } else if (error.response.status === 403) {
                // Forbidden
                console.warn('Access forbidden:', error.response.data);
            }
        } else if (error.request) {
            // Network error
            console.error('Network Error:', error.message);
        }

        return Promise.reject(error);
    }
);

export default api;
