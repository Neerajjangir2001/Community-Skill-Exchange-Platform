import api from './axios';

export const notificationService = {
    /**
     * Get user notifications log
     * @param {string} userId
     * @returns {Promise<Array>}
     */
    getUserNotifications: async (userId) => {
        const response = await api.get(`/api/notifications/user/${userId}`);
        return response.data;
    },

    /**
     * Get notification counts
     * @param {string} userId
     * @returns {Promise<Object>}
     */
    getNotificationCounts: async (userId) => {
        const response = await api.get(`/api/notifications/user/${userId}/count`);
        return response.data;
    },

    /**
     * Register device token
     * @param {Object} data { userId, deviceToken, deviceType, ... }
     */
    registerDeviceToken: async (data) => {
        const response = await api.post('/api/notifications/device/register', data);
        return response.data;
    }
};
