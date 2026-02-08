import api from './axios';

export const bookingService = {
    // Student
    createBooking: async (bookingData) => {
        // bookingData: { skillId, startTime, endTime }
        const response = await api.post('/api/bookings', bookingData);
        return response.data;
    },

    getMyBookings: async () => { // As student
        const response = await api.get('/api/bookings/my-bookings');
        return response.data;
    },

    cancelBookingStudent: async (id) => {
        const response = await api.put(`/api/bookings/${id}/cancel-student`);
        return response.data;
    },

    // Teacher
    getMyProviderBookings: async () => {
        const response = await api.get('/api/bookings/my-provider-bookings');
        return response.data;
    },

    acceptBooking: async (id) => {
        const response = await api.put(`/api/bookings/${id}/accept`);
        return response.data;
    },

    rejectBooking: async (id, reason) => {
        const response = await api.put(`/api/bookings/${id}/reject`, { reason });
        return response.data;
    },

    completeBooking: async (id) => {
        const response = await api.put(`/api/bookings/${id}/complete`);
        return response.data;
    },

    cancelBookingTeacher: async (id, reason) => {
        const response = await api.put(`/api/bookings/${id}/cancel-teacher`, { reason });
        return response.data;
    }
};
