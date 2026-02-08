import api from './axios';

export const userService = {
    getProfile: async (userId) => {
        if (userId) {
            const response = await api.get(`/api/users/${userId}`);
            return response.data;
        }
        const response = await api.get('/api/users/userComeByUserId');
        return response.data;
    },

    createProfile: async (profileData) => {
        const response = await api.post('/api/users', profileData);
        return response.data;
    },

    updateProfile: async (profileData) => {
        const response = await api.put('/api/users/update', profileData);
        return response.data;
    },

    uploadAvatar: async (file) => {
        const formData = new FormData();
        formData.append('file', file);
        const response = await api.post('/api/users/users/me/avatar', formData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    },

    searchProfiles: async (params) => {
        const response = await api.get('/api/users/search', { params });
        return response.data;
    },

    getProfileById: async (userId) => {
        const response = await api.get(`/api/users/${userId}`);
        return response.data;
    },

    deleteUser: async (userId) => {
        const response = await api.delete(`/api/users/${userId}`);
        return response.data;
    },

    getAllUsers: async () => {
        // Direct call to auth service for now, proxy ideally
        const token = localStorage.getItem('token');
        const response = await fetch('http://localhost:8081/auth/users', {
            headers: {
                'Authorization': `Bearer ${token}`
            }
        });
        if (!response.ok) throw new Error('Failed to fetch users');
        return await response.json();
    }
};
