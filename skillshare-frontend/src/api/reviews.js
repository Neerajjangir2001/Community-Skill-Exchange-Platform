import api from './axios';

export const reviewService = {
    createReview: async (reviewData) => {
        const response = await api.post('/api/reviews', reviewData);
        return response.data;
    },

    getTeacherReviews: async (teacherId) => {
        const response = await api.get(`/api/reviews/teacher/${teacherId}`);
        return response.data;
    },

    getAverageRating: async (teacherId) => {
        const response = await api.get(`/api/reviews/teacher/${teacherId}/average`);
        return response.data;
    },

    // Admin Endpoints
    moderateReview: async (reviewId, status) => {
        const response = await api.put(`/api/reviews/${reviewId}/status?status=${status}`);
        return response.data;
    },

    getAllReviews: async (status) => {
        const params = status ? { status } : {};
        const response = await api.get('/api/reviews', { params });
        return response.data;
    }

};
