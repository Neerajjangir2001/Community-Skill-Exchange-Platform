import React, { useState, useEffect, useRef } from 'react';
import { chatService } from '../../api/chat';
import SockJS from 'sockjs-client';
import Stomp from 'stompjs';
import { useAuth } from '../../context/AuthContext';
import { useNotifications } from '../../context/NotificationContext';
import { useLocation } from 'react-router-dom';
import {
    Send, User, MoreVertical, Loader2, Check, CheckCheck,
    Search, Phone, Video, Paperclip, Mic, Image as ImageIcon,
    FileText, X, ChevronRight, ChevronDown, Bell, Star, Trash2, MessageSquare
} from 'lucide-react';
import { format } from 'date-fns';
import { cn } from '../../utils/cn';
import toast from 'react-hot-toast';

const Chat = () => {
    const { user } = useAuth();
    const location = useLocation();
    const { notifications, fetchUnreadCount } = useNotifications();

    const [isFetchingConv, setIsFetchingConv] = useState(true);

    // State
    const [conversations, setConversations] = useState([]);
    const [activeConversation, setActiveConversation] = useState(null);
    const [messages, setMessages] = useState([]);
    const [newMessage, setNewMessage] = useState('');
    const [loading, setLoading] = useState(true);
    const [sending, setSending] = useState(false);
    const [searchTerm, setSearchTerm] = useState('');
    const [rightPanelOpen, setRightPanelOpen] = useState(true);
    const [activeTab, setActiveTab] = useState('All'); // 'All', 'Work', 'Friends'
    const [onlineUsers, setOnlineUsers] = useState(new Set());

    // Refs
    const messagesEndRef = useRef(null);
    const pollInterval = useRef(null);
    const heartbeatInterval = useRef(null);
    const stompClientRef = useRef(null);

    // Initial Data Fetch
    useEffect(() => {
        if (user) {
            fetchConversations();
            fetchOnlineUsers();
            connectWebSocket();

            // Start heartbeat
            chatService.sendHeartbeat();
            heartbeatInterval.current = setInterval(() => {
                chatService.sendHeartbeat().catch(err => console.error("Heartbeat failed", err));
            }, 30000);
        }
        return () => {
            disconnectWebSocket();
            if (heartbeatInterval.current) clearInterval(heartbeatInterval.current);
        };
    }, [user]);

    const connectWebSocket = () => {
        // Connect to Chat Service WebSocket
        const socket = new SockJS('http://localhost:8888/api/chat/ws');
        const stompClient = Stomp.over(socket);
        stompClient.debug = () => { };

        stompClient.connect({
            'Authorization': `Bearer ${localStorage.getItem('token')}` // Ensure we use the token
        }, () => {
            stompClientRef.current = stompClient;

            // Subscribe to private messages
            stompClient.subscribe('/user/queue/messages', (message) => {
                const newMessage = JSON.parse(message.body);
                handleIncomingMessage(newMessage);
            });

            // Subscribe to presence updates
            stompClient.subscribe('/topic/status', (message) => {
                const statusUpdate = JSON.parse(message.body);
                handleStatusUpdate(statusUpdate);
            });
        }, (error) => {
            console.error('Chat WebSocket connection error:', error);
        });
    };

    const handleIncomingMessage = (message) => {
        // If message belongs to active conversation, append it
        // If message belongs to active conversation, append or update it
        if (activeConversationRef.current && message.conversationId === activeConversationRef.current.id) {
            setMessages(prev => {
                const index = prev.findIndex(m => m.id === message.id);
                if (index !== -1) {
                    // Update existing message (e.g. status change)
                    const updated = [...prev];
                    updated[index] = message;
                    return updated;
                }
                return [...prev, message];
            });

            // Mark as read immediately if window focused and it's not my own message
            if (message.senderId !== user.id) {
                chatService.markConversationAsRead(message.conversationId, user.id)
                    .then(() => fetchUnreadCount())
                    .catch(console.error);
            }
        } else {
            // Update conversation list with new last message and unread count
            setConversations(prev => prev.map(c => {
                if (c.id === message.conversationId) {
                    return {
                        ...c,
                        lastMessageContent: message.content,
                        lastMessageTime: message.timestamp,
                        unreadCount: (c.unreadCount || 0) + 1
                    };
                }
                return c;
            }));
            // If conversation not in list (new), we should refetch or add it. 
            // Simplest is refetch conversations.
            // But optimization: check if found.
            // setConversations(prev => { ... }) logic is hard to check if found inside map.
            // Let's just trigger a refetch if not found in current list?
            // actually, just fetchConversations() is safer for sync.
        }

        // Always update conversation list order (move to top)
        setConversations(prev => {
            const index = prev.findIndex(c => c.id === message.conversationId);
            if (index > -1) {
                const updated = { ...prev[index] };
                if (activeConversationRef.current?.id !== message.conversationId) {
                    updated.unreadCount = (updated.unreadCount || 0) + 1;
                }
                updated.lastMessageContent = message.content;
                updated.lastMessageTime = message.timestamp;

                const newInfo = [...prev];
                newInfo.splice(index, 1);
                return [updated, ...newInfo];
            }
            // If not found, fetch all
            fetchConversations();
            return prev;
        });
    };

    const disconnectWebSocket = () => {
        if (stompClientRef.current) {
            stompClientRef.current.disconnect();
            stompClientRef.current = null;
        }
    };

    const handleStatusUpdate = (update) => {
        console.log("Chat: Received status update:", update);
        setOnlineUsers(prev => {
            const newSet = new Set(prev);
            if (update.online) {
                newSet.add(String(update.userId));
            } else {
                newSet.delete(String(update.userId));
            }
            return newSet;
        });
    };

    const fetchOnlineUsers = async () => {
        try {
            const users = await chatService.getOnlineUsers();
            setOnlineUsers(new Set(users));
        } catch (error) {
            console.error("Failed to fetch online users", error);
        }
    };

    // Use specific ref for activeConversation to access in WS callback closure
    const activeConversationRef = useRef(activeConversation);
    useEffect(() => {
        activeConversationRef.current = activeConversation;
    }, [activeConversation]);

    // Handle Notifications (Real-time updates)
    // We removed message handling from here as we now listen to /queue/messages directly.
    // We can still listen for other usage if needed.

    // Handle Navigation State (Start chat from other pages)
    useEffect(() => {
        if (location.state?.initialConversation && conversations.length > 0) {
            const passedConv = location.state.initialConversation;
            const found = conversations.find(c => c.id === passedConv.id);
            setActiveConversation(found || passedConv);
        }
    }, [location.state, conversations]);

    // Fetch Messages when conversation selected
    useEffect(() => {
        if (activeConversation?.id) {
            fetchMessages(activeConversation.id);
            chatService.markConversationAsRead(activeConversation.id, user.id)
                .then(() => {
                    // Update local state to clear badge immediately
                    setConversations(prev => prev.map(c =>
                        c.id === activeConversation.id ? { ...c, unreadCount: 0 } : c
                    ));
                    // Update Global Badge
                    fetchUnreadCount();
                })
                .catch(console.error);

            pollInterval.current = setInterval(() => {
                fetchMessages(activeConversation.id, true);
            }, 5000);
        }
        return () => {
            if (pollInterval.current) clearInterval(pollInterval.current);
        };
    }, [activeConversation?.id]);

    // Scroll to bottom on new message
    useEffect(() => {
        messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }, [messages]);

    const fetchConversations = async () => {
        try {
            // setIsFetchingConv(true)
            const data = await chatService.getConversations(user?.id);
            setConversations(data);
            setLoading(false);
        } catch (error) {
            console.error(error);
        } finally {
            setIsFetchingConv(false)
        }
    };

    const fetchMessages = async (conversationId, silent = false) => {
        try {
            if (!silent) setLoading(true);
            const data = await chatService.getMessages(conversationId);
            setMessages([...data.content].reverse() || []);
        } catch (error) {
            console.error(error);
        } finally {
            if (!silent) setLoading(false);
        }
    };

    const handleSend = async (e) => {
        e.preventDefault();
        if (!newMessage.trim() || !activeConversation) return;

        setSending(true);
        try {
            const otherUserId = activeConversation.participants?.find(p => p !== user.id);
            if (!otherUserId) throw new Error('Cannot identify receiver');

            await chatService.sendMessage({
                receiverId: otherUserId,
                content: newMessage,
                type: 'TEXT'
            });

            setNewMessage('');
            fetchMessages(activeConversation.id, true);
        } catch (error) {
            toast.error('Failed to send message');
        } finally {
            setSending(false);
        }
    };

    const handleDeleteConversation = async () => {
        if (!activeConversation || !window.confirm('Are you sure you want to delete this conversation? It will be removed from your list.')) return;

        try {
            await chatService.deleteConversation(activeConversation.id, user.id);
            setConversations(prev => prev.filter(c => c.id !== activeConversation.id));
            setActiveConversation(null);
            toast.success('Conversation deleted');
        } catch (error) {
            console.error('Failed to delete conversation:', error);
            toast.error('Failed to delete conversation');
        }
    };

    // Helper to get other user's details
    const getOtherUser = (conv) => {
        const currentUserId = String(user?.id || '');
        let otherId = conv?.participants?.find(p => String(p) !== currentUserId);

        // Fallback for chat with self or single participant
        if (!otherId && conv?.participants?.length > 0) {
            otherId = conv.participants[0];
        }

        const name = conv?.participantNames?.[otherId] || 'User';
        return { id: otherId, name };
    };

    const filteredConversations = conversations.filter(conv =>
        getOtherUser(conv).name.toLowerCase().includes(searchTerm.toLowerCase())
    );

    return (
        <div className="flex h-[calc(100vh-7rem)] bg-transparent gap-6 overflow-hidden pb-2">
            {/* Left Sidebar - Chat List */}
            {/* Left Sidebar - Chat List */}
            <div className="w-80 bg-white rounded-2xl shadow-clay-card flex flex-col overflow-hidden border-white/40 border">
                {/* Header & Search */}
                <div className="p-4 pb-2">
                    <div className="flex items-center justify-between mb-4">
                        <div className="flex items-center gap-3">
                            <h1 className="text-xl font-bold text-slate-900 tracking-tight">Messages</h1>
                            <span className="bg-indigo-100 text-indigo-700 text-xs font-bold px-2 py-0.5 rounded-full">{conversations.length}</span>
                        </div>
                        <button className="text-slate-400 hover:text-indigo-600 mx-2">
                            <MoreVertical className="w-5 h-5" />
                        </button>
                    </div>

                    <div className="relative">
                        <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-slate-400" />
                        <input
                            type="text"
                            placeholder="Search chats..."
                            value={searchTerm}
                            onChange={(e) => setSearchTerm(e.target.value)}
                            className="w-full bg-slate-50 border border-gray-100 rounded-xl pl-10 pr-4 py-2.5 text-sm font-medium text-slate-700 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-100 transition-all"
                        />
                    </div>
                </div>

                {/* Conversation List */}
                <div className="flex-1 overflow-y-auto px-4 pb-4 space-y-1">
                    {
                        isFetchingConv ?
                            <div className="flex justify-center py-12">
                                <Loader2 className="w-5 h-5 animate-spin text-primary-600" />
                            </div> : filteredConversations.length < 1 ? <div className="flex justify-center py-12">
                                <h3 className="text-slate-500">No conversations found</h3>
                            </div> : filteredConversations.map(conv => {
                                const { name } = getOtherUser(conv);
                                const isActive = activeConversation?.id === conv.id;
                                return (
                                    <button
                                        key={conv.id}
                                        onClick={() => setActiveConversation(conv)}
                                        className={cn(
                                            "w-full flex items-center gap-3 p-3 rounded-xl transition-all duration-200 group text-left",
                                            isActive
                                                ? "bg-indigo-50/50 ring-1 ring-indigo-100"
                                                : "hover:bg-gray-50 border border-transparent"
                                        )}
                                    >
                                        <div className="relative shrink-0">
                                            <div className="w-12 h-12 rounded-full overflow-hidden border border-gray-100">
                                                <img
                                                    src={`https://ui-avatars.com/api/?name=${name}&background=random`}
                                                    alt={name}
                                                    className="w-full h-full object-cover"
                                                />
                                            </div>
                                            {onlineUsers.has(String(getOtherUser(conv).id)) && (
                                                <div className="absolute bottom-0 right-0 w-3 h-3 bg-emerald-500 border-2 border-white rounded-full"></div>
                                            )}
                                        </div>
                                        <div className="flex-1 min-w-0">
                                            <div className="flex items-center justify-between mb-0.5">
                                                <h3 className={cn("font-semibold text-sm truncate", isActive ? "text-indigo-900" : "text-slate-800")}>{name}</h3>
                                                <span className="text-[10px] text-slate-400 font-medium">
                                                    {conv.lastMessageTime ? format(new Date(conv.lastMessageTime), 'h:mm a') : ''}
                                                </span>
                                            </div>
                                            <p className={cn("text-xs truncate", isActive ? "text-indigo-600/80 font-medium" : "text-slate-500")}>
                                                {conv.lastMessageContent || "Start a conversation"}
                                            </p>
                                        </div>
                                        {conv.unreadCount > 0 && (
                                            <div className="w-5 h-5 bg-indigo-600 rounded-full flex items-center justify-center text-[10px] font-bold text-white shrink-0">
                                                {conv.unreadCount}
                                            </div>
                                        )}
                                    </button>
                                );
                            })
                    }
                </div>
            </div>

            {/* Main Chat Area */}
            <div className="flex-1 bg-clay-card rounded-2xl shadow-clay-card flex flex-col overflow-hidden relative border border-white/40">
                {activeConversation ? (
                    <>
                        {/* Header */}
                        <div className="h-20 px-6 flex items-center justify-between bg-white/50 backdrop-blur-md z-10 border-b border-gray-100">
                            <div className="flex items-center gap-4">
                                <div className="w-12 h-12 rounded-xl bg-clay-bg shadow-inner overflow-hidden">
                                    <img
                                        src={`https://ui-avatars.com/api/?name=${getOtherUser(activeConversation).name}&background=random`}
                                        alt="User"
                                        className="w-full h-full object-cover"
                                    />
                                </div>
                                <div>
                                    <h2 className="font-bold text-lg text-slate-900">{getOtherUser(activeConversation).name}</h2>
                                    <div className="flex items-center gap-2">
                                        <span className={cn("w-2 h-2 rounded-full", onlineUsers.has(String(getOtherUser(activeConversation).id)) ? "bg-emerald-500" : "bg-slate-300")}></span>
                                        <span className="text-xs text-slate-500 font-medium">
                                            {onlineUsers.has(String(getOtherUser(activeConversation).id)) ? 'Online' : 'Offline'}
                                        </span>
                                    </div>
                                </div>
                            </div>
                            <div className="flex items-center gap-2">
                                <button className="p-2.5 text-slate-400 hover:text-indigo-600 hover:bg-slate-50 rounded-lg transition-all">
                                    <Phone className="w-5 h-5" />
                                </button>
                                <button className="p-2.5 text-slate-400 hover:text-indigo-600 hover:bg-slate-50 rounded-lg transition-all">
                                    <Video className="w-5 h-5" />
                                </button>
                                <div className="h-6 w-px bg-slate-200 mx-1"></div>
                                <button
                                    onClick={handleDeleteConversation}
                                    className="p-2.5 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-all"
                                    title="Delete Conversation"
                                >
                                    <Trash2 className="w-5 h-5" />
                                </button>
                                <button
                                    onClick={() => setRightPanelOpen(!rightPanelOpen)}
                                    className={cn("p-2.5 rounded-lg transition-all", rightPanelOpen ? "text-indigo-600 bg-indigo-50" : "text-slate-400 hover:bg-slate-50")}
                                >
                                    <MoreVertical className="w-5 h-5" />
                                </button>
                            </div>
                        </div>

                        {/* Messages */}
                        <div className="flex-1 overflow-y-auto p-6 space-y-6">
                            {messages.map((msg, i) => {
                                const isMe = msg.senderId === user?.id;
                                return (
                                    <div key={i} className={cn("flex gap-3 max-w-[85%]", isMe ? "ml-auto flex-row-reverse" : "")}>
                                        {!isMe && (
                                            <div className="w-8 h-8 rounded-lg bg-gray-100 shrink-0 overflow-hidden mt-1">
                                                <img
                                                    src={`https://ui-avatars.com/api/?name=${getOtherUser(activeConversation).name}&background=random`}
                                                    alt="Sender"
                                                    className="w-full h-full object-cover"
                                                />
                                            </div>
                                        )}
                                        <div className="flex flex-col gap-1">
                                            <div className={cn(
                                                "px-5 py-3 text-[15px] leading-relaxed relative shadow-sm",
                                                isMe
                                                    ? "bg-indigo-600 text-white rounded-2xl rounded-tr-sm"
                                                    : "bg-white border border-gray-100 text-slate-800 rounded-2xl rounded-tl-sm"
                                            )}>
                                                {msg.deleted ? <span className="italic opacity-75">This message was deleted</span> : msg.content}
                                            </div>
                                            <div className={cn("flex items-center gap-1 text-[10px] uppercase font-bold tracking-wider opacity-60", isMe ? "justify-end" : "justify-start")}>
                                                {format(new Date(msg.timestamp), 'h:mm a')}
                                                {isMe && !msg.deleted && (
                                                    <span>
                                                        {msg.status === 'READ' ? <CheckCheck className="w-3.5 h-3.5 text-indigo-500" /> : <Check className="w-3.5 h-3.5" />}
                                                    </span>
                                                )}
                                            </div>
                                        </div>
                                    </div>
                                );
                            })}
                            <div ref={messagesEndRef} />
                        </div>

                        {/* Input Area */}
                        <div className="p-4 bg-white border-t border-gray-100 mt-auto">
                            <div className="bg-gray-50 rounded-xl p-2 flex items-end gap-2 border border-gray-200 focus-within:ring-2 focus-within:ring-indigo-100 transition-all">
                                <button className="p-2 text-slate-400 hover:text-indigo-600 transition-colors rounded-lg hover:bg-gray-200">
                                    <Paperclip className="w-5 h-5" />
                                </button>
                                <textarea
                                    value={newMessage}
                                    onChange={(e) => setNewMessage(e.target.value)}
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter' && !e.shiftKey) {
                                            e.preventDefault();
                                            handleSend(e);
                                        }
                                    }}
                                    placeholder="Type a message..."
                                    className="flex-1 bg-transparent border-none outline-none text-slate-700 placeholder:text-slate-400 font-medium text-sm py-2 resize-none max-h-32 min-h-[44px]"
                                    rows={1}
                                />
                                <button
                                    onClick={handleSend}
                                    disabled={!newMessage.trim() || sending}
                                    className="p-2 bg-indigo-600 hover:bg-indigo-700 text-white rounded-lg shadow-sm disabled:opacity-50 disabled:shadow-none transition-all"
                                >
                                    {sending ? <Loader2 className="w-5 h-5 animate-spin" /> : <Send className="w-5 h-5" />}
                                </button>
                            </div>
                        </div>
                    </>
                ) : (
                    <div className="flex-1 flex flex-col items-center justify-center text-slate-300 bg-gray-50/50">
                        <div className="w-24 h-24 bg-white shadow-sm rounded-full flex items-center justify-center mb-6">
                            <MessageSquare className="w-10 h-10 text-slate-300" />
                        </div>
                        <p className="text-lg font-bold text-slate-400">Select a conversation to start chatting</p>
                    </div>
                )}
            </div>

            {/* Right Panel - Info */}
            {activeConversation && rightPanelOpen && (
                <div className="w-80 bg-white rounded-2xl shadow-clay-card flex flex-col overflow-hidden animate-in slide-in-from-right-10 duration-500 border border-white/40">
                    <div className="p-6 border-b border-gray-100 flex items-center justify-between bg-gray-50/30">
                        <h2 className="font-bold text-base text-slate-800">Details</h2>
                        <button onClick={() => setRightPanelOpen(false)} className="text-slate-400 hover:text-slate-600 p-1.5 rounded-lg hover:bg-gray-100 transition-all">
                            <X className="w-4 h-4" />
                        </button>
                    </div>

                    <div className="flex-1 overflow-y-auto p-6 bg-white">
                        {/* Profile Section */}
                        <div className="text-center mb-8">
                            <div className="w-24 h-24 rounded-full bg-gray-100 mx-auto mb-4 overflow-hidden border-4 border-white shadow-sm">
                                <img
                                    src={`https://ui-avatars.com/api/?name=${getOtherUser(activeConversation).name}&background=random&size=256`}
                                    alt="User"
                                    className="w-full h-full object-cover"
                                />
                            </div>
                            <h3 className="font-bold text-xl text-slate-900 mb-1">{getOtherUser(activeConversation).name}</h3>
                            <p className="text-xs font-semibold text-indigo-600 bg-indigo-50 py-1 px-3 rounded-full inline-block mb-6">
                                {activeConversation?.participantRoles?.[getOtherUser(activeConversation).id] || 'User'}
                            </p>

                            <div className="flex justify-center gap-3">
                                <button className="flex flex-col items-center gap-1.5 p-3 rounded-xl hover:bg-gray-50 transition-all w-20 group">
                                    <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-slate-600 group-hover:bg-indigo-100 group-hover:text-indigo-600 transition-colors">
                                        <Phone className="w-4 h-4" />
                                    </div>
                                    <span className="text-xs font-medium text-slate-500">Audio</span>
                                </button>
                                <button className="flex flex-col items-center gap-1.5 p-3 rounded-xl hover:bg-gray-50 transition-all w-20 group">
                                    <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-slate-600 group-hover:bg-indigo-100 group-hover:text-indigo-600 transition-colors">
                                        <Video className="w-4 h-4" />
                                    </div>
                                    <span className="text-xs font-medium text-slate-500">Video</span>
                                </button>
                                <button className="flex flex-col items-center gap-1.5 p-3 rounded-xl hover:bg-gray-50 transition-all w-20 group">
                                    <div className="w-10 h-10 rounded-full bg-gray-100 flex items-center justify-center text-slate-600 group-hover:bg-amber-100 group-hover:text-amber-600 transition-colors">
                                        <Star className="w-4 h-4" />
                                    </div>
                                    <span className="text-xs font-medium text-slate-500">Rate</span>
                                </button>
                            </div>
                        </div>

                        {/* Files / Attachments Placeholder */}
                        <div className="space-y-4">
                            <h4 className="text-sm font-bold text-slate-900 uppercase tracking-wider">Shared Media</h4>
                            <div className="grid grid-cols-3 gap-2">
                                {[1, 2, 3].map(i => (
                                    <div key={i} className="aspect-square rounded-lg bg-gray-100 animate-pulse"></div>
                                ))}
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Chat;
