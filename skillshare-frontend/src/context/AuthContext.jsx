import React, { createContext, useContext, useState, useEffect } from 'react';
import { authService } from '../api/auth';
import { userService } from '../api/user';

const AuthContext = createContext(null);

export const AuthProvider = ({ children }) => {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('token'));
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        if (token) {
            validateToken();
        } else {
            setLoading(false);
        }
    }, [token]);

    const validateToken = async () => {
        try {
            const data = await authService.validateToken();
            if (data.valid) {
                // Initial user state from token validation (if available)
                setUser(prev => ({
                    ...prev,
                    id: data.userId, // CAREFUL: Backend returns 'userId' or 'id'?
                    email: data.email,
                    roles: data.roles
                }));

                // Fetch full profile
                if (data.userId) {
                    fetchUserProfile(data.userId); // pass ID if needed, or service uses token context
                }
            } else {
                logout();
            }
        } catch (error) {
            console.error('Token validation failed:', error);
            logout();
        } finally {
            setLoading(false);
        }
    };

    const fetchUserProfile = async (userId) => {
        try {
            const profile = await userService.getProfile();
            setUser(prev => ({
                ...prev,
                ...profile,
                id: profile.userId || prev.id // Ensure 'id' is always the User ID, not Profile ID
            }));
        } catch (error) {
            console.error('Failed to fetch profile:', error);
        }
    };

    const login = async (email, password) => {
        const data = await authService.login(email, password);
        // Backend returns: accessToken, refreshToken, userId, email, roles
        setToken(data.accessToken);
        localStorage.setItem('token', data.accessToken);
        localStorage.setItem('refreshToken', data.refreshToken);

        const nextUser = {
            id: data.userId,
            email: data.email,
            roles: data.roles || []
        };
        setUser(nextUser);

        return data;
    };

    const register = async (userData) => {
        await authService.register(userData);
        return await login(userData.email, userData.password);
    };

    const logout = async () => {
        const refreshToken = localStorage.getItem('refreshToken');
        try {
            if (refreshToken) {
                await authService.logout(refreshToken);
            }
        } catch (error) {
            console.error("Logout error", error);
        } finally {
            setToken(null);
            setUser(null);
            localStorage.removeItem('token');
            localStorage.removeItem('refreshToken');
            setLoading(false);
        }
    };

    return (
        <AuthContext.Provider value={{ user, token, login, register, logout, loading, isAuthenticated: !!token }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => {
    const context = useContext(AuthContext);
    if (!context) throw new Error('useAuth must be used within AuthProvider');
    return context;
};
