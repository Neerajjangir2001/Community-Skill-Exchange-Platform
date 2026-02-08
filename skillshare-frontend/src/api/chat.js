import api from './axios';

export const chatService = {
    getConversations: async (userId) => {
        const response = await api.get('/api/chat/conversations', { params: { userId } });
        return response.data;
    },

    getConversation: async (user1, user2) => {
        const response = await api.get('/api/chat/conversation', { params: { user1, user2 } });
        return response.data;
    },

    getMessages: async (conversationId, page = 0, size = 50) => {
        const response = await api.get(`/api/messages/conversation/${conversationId}`, {
            params: { page, size }
        });
        return response.data;
    },

    sendMessage: async (messageData) => {
        // messageData: { receiverId, content, type }
        const response = await api.post('/api/messages/send', messageData);
        return response.data;
    },

    getUnreadCount: async () => {
        const response = await api.get('/api/messages/unread/count');
        return response.data;
    },

    markAsRead: async (messageId) => {
        const response = await api.put(`/api/messages/${messageId}/read`);
        return response.data;
    },

    markConversationAsRead: async (conversationId, userId) => {
        const response = await api.put(`/api/messages/conversation/${conversationId}/read`, null, {
            params: { userId }
        });
        return response.data;
    },

    getOnlineUsers: async () => {
        const response = await api.get('/api/chat/presence');
        return response.data;
    },

    deleteConversation: async (conversationId, userId) => {
        const response = await api.delete(`/api/chat/conversation/${conversationId}`, {
            params: { userId }
        });
        return response.data;
    },

    sendHeartbeat: async () => {
        try {
            await api.post('/api/chat/presence/heartbeat');
        } catch (error) {
            // Ignore heartbeat errors to not clutter console
        }
    }
};
