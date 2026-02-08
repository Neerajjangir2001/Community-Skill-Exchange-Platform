import React from 'react';
import { NavLink } from 'react-router-dom';
import { Home, BookOpen, Calendar, MessageSquare, User, LogOut, GraduationCap, Users, Star } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { useNotifications } from '../context/NotificationContext';
import { cn } from '../utils/cn';

const Sidebar = ({ isOpen, onClose }) => {
    const { logout, user } = useAuth();
    const { unreadCount } = useNotifications();

    const generalItems = [
        { icon: Home, label: 'Explore Skills', path: '/' },
        { icon: Calendar, label: 'My Bookings', path: '/bookings' },
        { icon: MessageSquare, label: 'Messages', path: '/chat', badge: true },
        { icon: User, label: 'Profile', path: '/profile' },
    ];






    const teachingItems = [
        { icon: BookOpen, label: 'My Skills', path: '/my-skills' },
    ];

    const adminItems = [
        { icon: Users, label: 'User Management', path: '/admin/users' },
        { icon: Star, label: 'Reviews', path: '/admin/reviews' },
    ];


    const NavLinkItem = ({ item }) => (
        <NavLink
            to={item.path}
            onClick={() => onClose && onClose()}
            className={({ isActive }) => cn(
                "flex items-center gap-3 px-4 py-3.5 rounded-2xl transition-all duration-300 font-medium group text-sm relative overflow-hidden",
                isActive
                    ? "bg-clay-bg text-indigo-600 shadow-clay-pressed"
                    : "text-slate-500 hover:text-indigo-600 hover:shadow-clay-btn hover:-translate-y-0.5"
            )}
        >
            <item.icon className={cn("w-5 h-5 transition-colors duration-300",
                ({ isActive }) => isActive ? "text-indigo-600" : "text-slate-400 group-hover:text-indigo-600"
            )} />
            <span className="flex-1 z-10">{item.label}</span>
            {item.badge && unreadCount > 0 && (
                <span className="bg-rose-500 text-white text-[10px] font-bold px-2 py-0.5 rounded-full shadow-sm z-10 min-w-[1.25rem] text-center">
                    {unreadCount > 99 ? '99+' : unreadCount}
                </span>
            )}
        </NavLink>
    );

    return (
        <>
            {/* Mobile Overlay */}
            <div
                className={cn(
                    "fixed inset-0 bg-slate-900/40 backdrop-blur-sm z-40 lg:hidden transition-all duration-300",
                    isOpen ? "opacity-100" : "opacity-0 pointer-events-none"
                )}
                onClick={onClose}
            />

            {/* Sidebar Container */}
            <div className={cn(
                "fixed inset-y-0 left-0 z-50 w-[19rem] h-[100dvh] transition-transform duration-300 ease-in-out lg:sticky lg:top-0 lg:h-screen lg:z-40 lg:py-4 lg:pl-4",
                isOpen ? "translate-x-0" : "-translate-x-full lg:translate-x-0"
            )}>
                <aside className="h-full w-full bg-clay-card lg:rounded-[2rem] shadow-clay-card flex flex-col overflow-hidden">
                    <div className="p-8 flex items-center gap-4">
                        <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-indigo-500 to-violet-600 flex items-center justify-center shadow-lg shadow-indigo-500/30 text-white transform hover:animate-wiggle transition-all cursor-pointer">
                            <GraduationCap className="w-7 h-7" />
                        </div>
                        <h1 className="text-2xl font-bold bg-gradient-to-r from-slate-700 to-slate-900 bg-clip-text text-transparent font-display tracking-tight">
                            SkillShare
                        </h1>
                    </div>

                    <nav className="flex-1 px-6 space-y-8 overflow-y-auto scrollbar-hide py-2">
                        <div>
                            <div className="px-4 mb-4 text-xs font-bold text-slate-400 uppercase tracking-widest">
                                Menu
                            </div>
                            <div className="space-y-3">
                                {generalItems.map((item) => (
                                    <NavLinkItem key={item.path} item={item} />
                                ))}
                            </div>
                        </div>

                        {user?.roles?.includes('TEACHER') && (
                            <div>
                                <div className="px-4 mb-4 text-xs font-bold text-slate-400 uppercase tracking-widest">
                                    Teaching
                                </div>
                                <div className="space-y-3">
                                    {teachingItems.map((item) => (
                                        <NavLinkItem key={item.path} item={item} />
                                    ))}
                                </div>
                            </div>
                        )}

                        {user?.roles?.includes('ADMIN') && (
                            <div>
                                <div className="px-4 mb-4 text-xs font-bold text-slate-400 uppercase tracking-widest">
                                    Admin
                                </div>
                                <div className="space-y-3">
                                    {adminItems.map((item) => (
                                        <NavLinkItem key={item.path} item={item} />
                                    ))}
                                </div>
                            </div>
                        )}
                    </nav>

                    <div className="p-6 mt-auto">
                        <div className="p-4 rounded-3xl bg-clay-bg shadow-clay-inner backdrop-blur-sm">
                            <div className="flex items-center gap-3 mb-4">
                                <div className="w-12 h-12 rounded-2xl bg-white shadow-clay-btn flex items-center justify-center text-indigo-600 font-bold text-lg shrink-0">
                                    {user?.displayName?.[0] || user?.email?.[0] || 'U'}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <p className="text-sm font-bold text-slate-700 truncate">
                                        {user?.displayName || 'User'}
                                    </p>
                                    <p className="text-xs text-slate-500 truncate font-medium">
                                        {user?.email}
                                    </p>
                                </div>
                            </div>
                            <button
                                onClick={logout}
                                className="w-full flex items-center justify-center gap-2 px-4 py-3 text-slate-500 hover:text-rose-600 hover:shadow-clay-btn hover:bg-white rounded-xl transition-all duration-300 text-sm font-semibold group"
                            >
                                <LogOut className="w-4 h-4 group-hover:scale-110 transition-transform" />
                                Sign Out
                            </button>
                        </div>
                    </div>
                </aside>
            </div>
        </>
    );
};

export default Sidebar;
