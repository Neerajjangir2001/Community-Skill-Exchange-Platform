import api from './axios';

export const authService = {
    login: async (email, password) => {
        const response = await api.post('/auth/login', { email, password });
        return response.data;
    },

    register: async (userData) => {
        // userData: { email, password, confirmPassword (handled in UI, not sent?), ... }
        // Backend expects SignupRequest
        const response = await api.post('/auth/signup', userData);
        return response.data;
    },

    validateToken: async () => {
        const response = await api.get('/auth/validate');
        return response.data;
    },

    logout: async (refreshToken) => {
        const response = await api.post('/auth/logout', { refreshToken });
        return response.data;
    },

    refreshToken: async (refreshToken) => {
        const response = await api.post('/auth/refresh', { refreshToken });
        return response.data;
    },

    changePassword: async (userId, currentPassword, newPassword) => {
        const response = await api.post(`/auth/change-password/${userId}`, { currentPassword, newPassword });
        return response.data;
    },

    forgotPassword: async (email) => {
        const response = await api.post('/auth/forgot-password', { email });
        return response.data;
    },

    resetPassword: async (token, newPassword) => {
        const response = await api.post('/auth/reset-password', { token, newPassword });
        return response.data;
    }
};
