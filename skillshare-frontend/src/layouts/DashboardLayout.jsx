import React, { useState } from 'react';
import { Outlet, useNavigate } from 'react-router-dom';
import Sidebar from '../components/Sidebar';
import { Menu, Bell, Search } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import { cn } from '../utils/cn';

const DashboardLayout = () => {
    const [sidebarOpen, setSidebarOpen] = useState(false);
    const [showNotifications, setShowNotifications] = useState(false);
    const { user } = useAuth();
    const { notifications } = useNotifications();
    const navigate = useNavigate();

    const handleNotificationClick = (notif) => {
        setShowNotifications(false);
        const title = (notif.title || notif.subject || '').toLowerCase();

        if (title.includes('request') || title.includes('confirm') || title.includes('accepted')) {
            navigate('/bookings');
        } else if (title.includes('message')) {
            navigate('/chat');
        } else {
            navigate('/profile');
        }
    };

    return (
        <div className="flex min-h-screen bg-slate-50 font-sans text-slate-900">

            <Sidebar isOpen={sidebarOpen} onClose={() => setSidebarOpen(false)} />

            <div className="flex-1 flex flex-col min-w-0 transition-all duration-300">
                {/* Header - Professional Clean Style */}
                <header className=" sticky top-4 z-30 bg-white border border-slate-200 rounded-2xl px-4 py-3 mx-4  ">
                    <div className="flex items-center justify-between gap-1 h-10">
                        <div className="flex items-center gap-4 flex-1">
                            <button
                                onClick={() => setSidebarOpen(true)}
                                className="p-2 text-slate-500 hover:bg-slate-100 rounded-lg lg:hidden transition-colors"
                            >
                                <Menu className="w-6 h-6" />
                            </button>

                            {/* Global Search Bar - Pill Style */}
                            <div className="hidden md:flex items-center max-w-lg w-full relative group">
                                <Search className="w-4 h-4 text-slate-400 absolute left-4 group-focus-within:text-indigo-600 transition-colors pointer-events-none" />
                                <input
                                    type="text"
                                    placeholder="Search for skills, mentors..."
                                    className="w-full pl-11 pr-4 py-2.5 bg-slate-100 border-transparent focus:bg-white border focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 rounded-xl text-sm font-medium text-slate-900 placeholder:text-slate-500 transition-all outline-none"
                                    onKeyDown={(e) => {
                                        if (e.key === 'Enter') {
                                            navigate(`/?q=${encodeURIComponent(e.target.value)}`);
                                        }
                                    }}
                                />
                            </div>
                        </div>

                        <div className="flex items-center gap-3 sm:gap-4 relative">
                            {user?.roles?.includes('ADMIN') && (
                                <div className="hidden sm:flex items-center gap-2 px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full border border-indigo-100">
                                    <div className="w-1.5 h-1.5 rounded-full bg-indigo-600"></div>
                                    <span className="text-[11px] font-bold uppercase tracking-wider">Admin</span>
                                </div>
                            )}

                            <button
                                onClick={() => setShowNotifications(!showNotifications)}
                                className="p-2.5 text-slate-500 hover:text-indigo-600 hover:bg-slate-50 rounded-full transition-all relative"
                            >
                                <Bell className="w-5 h-5" />
                                {notifications.length > 0 && (
                                    <span className="absolute top-2.5 right-2.5 w-2 h-2 bg-rose-500 rounded-full border-2 border-white"></span>
                                )}
                            </button>

                            {/* Notifications Dropdown */}
                            {showNotifications && (
                                <div className="absolute top-full right-0 mt-2 w-80 md:w-96 bg-white rounded-2xl shadow-xl border border-slate-200 overflow-hidden z-50 animate-in fade-in slide-in-from-top-1 duration-200">
                                    <div className="p-4 border-b border-slate-100 flex items-center justify-between bg-slate-50/50">
                                        <h3 className="font-bold text-slate-800">Notifications</h3>
                                        <button className="text-xs text-indigo-600 font-semibold hover:text-indigo-700">Mark all read</button>
                                    </div>
                                    <div className="max-h-[400px] overflow-y-auto custom-scrollbar">
                                        {notifications.length > 0 ? (
                                            notifications.map((notif, index) => (
                                                <div
                                                    key={index}
                                                    onClick={() => handleNotificationClick(notif)}
                                                    className="p-4 border-b border-slate-50 hover:bg-slate-50 transition-colors cursor-pointer flex gap-3 last:border-0"
                                                >
                                                    <div className="flex-shrink-0">
                                                        <div className="w-8 h-8 rounded-full bg-indigo-50 text-indigo-600 flex items-center justify-center font-bold text-xs ring-1 ring-indigo-100">
                                                            {(notif.title || 'N')[0]}
                                                        </div>
                                                    </div>
                                                    <div>
                                                        <h4 className="text-sm font-semibold text-slate-900">{notif.title || notif.subject}</h4>
                                                        <p className="text-xs text-slate-600 mt-0.5 line-clamp-2">{notif.message || notif.content}</p>
                                                        <span className="text-[10px] text-slate-400 mt-1.5 block font-medium">
                                                            {new Date(notif.createdAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                                                        </span>
                                                    </div>
                                                </div>
                                            ))
                                        ) : (
                                            <div className="p-8 text-center text-slate-400">
                                                <Bell className="w-8 h-8 mx-auto mb-2 opacity-20" />
                                                <p className="text-sm font-medium">No new notifications</p>
                                            </div>
                                        )}
                                    </div>
                                    <div className="p-3 bg-slate-50 border-t border-slate-100 text-center">
                                        <button className="text-xs font-bold text-slate-600 hover:text-indigo-600 transition-colors uppercase tracking-wide">View All Activity</button>
                                    </div>
                                </div>
                            )}

                            {/* User Profile Section */}
                            <div className="hidden md:flex items-center pl-2 ml-2 border-l border-slate-200 h-8 ">
                                <div className="flex items-center gap-3 pl-4 cursor-pointer group">
                                    <div className="text-right">
                                        <div className="text-sm font-bold text-slate-800 group-hover:text-indigo-700 transition-colors">{user?.displayName || user?.name || 'User'}</div>
                                    </div>
                                    <div className="w-9 h-9 rounded-full bg-slate-100 border border-slate-200 flex items-center justify-center text-slate-600 font-bold group-hover:bg-indigo-50 group-hover:text-indigo-600 group-hover:border-indigo-100 transition-all">
                                        {(user?.displayName || user?.name || user?.email || 'U').charAt(0).toUpperCase()}
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </header>

                <main className="flex-1 overflow-y-auto">
                    <div className="max-w-[1600px] mx-auto p-6 lg:py-8 lg:px-4 animate-in fade-in duration-500">
                        <Outlet />
                    </div>
                </main>
            </div>
        </div>
    );
};

export default DashboardLayout;
