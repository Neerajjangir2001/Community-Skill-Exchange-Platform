import api from './axios';

export const skillService = {
    // Public/Search
    getAllSkills: async (params) => {
        // params: { search, level }
        const response = await api.get('/api/search/getAllSkills', { params });
        return response.data;
    },

    searchSkills: async (query) => {
        const response = await api.get('/api/search/search', { params: { query } });
        return response.data;
    },

    advancedSearch: async (params) => {
        // params: q, tags, level, minPrice, maxPrice, page, size, sort, direction
        const response = await api.get('/api/search/skillSearch', { params });
        return response.data;
    },

    getSkillById: async (id) => {
        const response = await api.get(`/api/search/${id}`);
        return response.data;
    },

    // Teacher
    createSkill: async (skillData) => {
        const response = await api.post('/api/skills', skillData);
        return response.data;
    },

    getMySkills: async () => {
        const response = await api.get('/api/skills/my-skills');
        return response.data;
    },

    updateSkill: async (id, skillData) => {
        const response = await api.put(`/api/skills/${id}`, skillData);
        return response.data;
    },

    deleteSkill: async (id) => {
        const response = await api.delete(`/api/skills/${id}`);
        return response.data;
    },
};
