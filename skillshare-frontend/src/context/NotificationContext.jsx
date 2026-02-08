import React, { createContext, useContext, useEffect, useState, useRef } from 'react';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { useAuth } from './AuthContext';
import { notificationService } from '../api/notification';
import toast from 'react-hot-toast';


import { chatService } from '../api/chat';

const NotificationContext = createContext();

export const useNotifications = () => useContext(NotificationContext);

export const NotificationProvider = ({ children }) => {
    const { user, token } = useAuth();
    const [notifications, setNotifications] = useState([]);
    const [unreadCount, setUnreadCount] = useState(0);
    const stompClientRef = useRef(null);
    const connectedRef = useRef(false);
    const connectingRef = useRef(false);
    const lastCheckedRef = useRef(Date.now());
    const pollingIntervalRef = useRef(null);

    useEffect(() => {
        if (user && token) {
            connect();
            startPolling();
        } else {
            disconnect();
            stopPolling();
        }

        return () => {
            disconnect();
            stopPolling();
        };
    }, [user, token]);

    const startPolling = () => {
        // Initial fetch
        fetchNotifications(true);
        fetchUnreadCount();

        // Poll every 15 seconds
        pollingIntervalRef.current = setInterval(() => {
            fetchNotifications(false);
            fetchUnreadCount();
        }, 15000);
    };

    const stopPolling = () => {
        if (pollingIntervalRef.current) {
            clearInterval(pollingIntervalRef.current);
            pollingIntervalRef.current = null;
        }
    };

    const fetchUnreadCount = async () => {
        if (!user?.id) return;
        try {
            const count = await chatService.getUnreadCount();
            setUnreadCount(count);
        } catch (error) {
            console.error('Failed to fetch unread count:', error);
        }
    };

    const fetchNotifications = async (isInitial = false) => {
        if (!user?.id) return;
        try {
            const data = await notificationService.getUserNotifications(user.id);

            if (!Array.isArray(data)) {
                console.warn("Notification data is not an array:", data);
                setNotifications([]);
                return;
            }

            setNotifications(data.reverse());

            // For polling check: find if any created after lastChecked
            if (!isInitial) {
                const newItems = data.filter(n => new Date(n.createdAt).getTime() > lastCheckedRef.current);
                newItems.forEach(item => {
                    handleNotification({
                        title: item.subject,
                        message: item.content,
                        type: item.type,
                        ...item
                    });
                });
            }
            lastCheckedRef.current = Date.now();
        } catch (error) {
            console.error("Failed to fetch notifications", error);
        }
    };

    const connect = () => {
        if (connectedRef.current || connectingRef.current) return;

        connectingRef.current = true;
        const socket = new SockJS('http://localhost:8888/api/notifications/ws');
        const stompClient = Stomp.over(socket);

        stompClient.debug = () => { };

        stompClient.connect({
            'Authorization': `Bearer ${token}`
        }, (frame) => {
            connectedRef.current = true;
            connectingRef.current = false;
            stompClientRef.current = stompClient;
            console.log('Connected to WebSocket');

            stompClient.subscribe('/user/topic/notifications', (message) => {
                const notification = JSON.parse(message.body);
                handleNotification(notification);
            });
        }, (error) => {
            console.error('WebSocket connection error:', error);
            connectedRef.current = false;
            connectingRef.current = false;
        });
    };

    const disconnect = () => {
        if (stompClientRef.current) {
            stompClientRef.current.disconnect();
            stompClientRef.current = null;
        }
        connectedRef.current = false;
    };

    const handleNotification = (notification) => {
        // Refresh unread count if it's a message
        if (notification.type === 'MESSAGE' ||
            (notification.title && notification.title.toLowerCase().includes('message')) ||
            (notification.subject && notification.subject.toLowerCase().includes('message'))) {
            fetchUnreadCount();
        }

        toast.success(
            <div className="flex flex-col gap-1">
                <span className="font-bold text-slate-800">{notification.title || 'New Notification'}</span>
                <span className="text-xs text-slate-500 font-medium">{notification.message || notification.content}</span>
            </div>,
            { duration: 5000 }
        );
    };

    return (
        <NotificationContext.Provider value={{ notifications, unreadCount, fetchUnreadCount }}>
            {children}
        </NotificationContext.Provider>
    );
};
