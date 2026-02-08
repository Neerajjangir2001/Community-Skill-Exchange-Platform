import React, { useState, useEffect } from 'react';
import { useNavigate, useParams, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { userService } from '../../api/user';
import { authService } from '../../api/auth';
import { bookingService } from '../../api/bookings';
import { skillService } from '../../api/skills';
import toast from 'react-hot-toast';
import {
    User, Mail, MapPin, Camera, Save, Loader2, Check,
    LayoutDashboard, Edit3, Calendar, Bell, Star, Briefcase,
    TrendingUp, Users, Clock, Settings, ChevronRight, AlertTriangle, Lock, CheckCircle2, ChevronLeft, X
} from 'lucide-react';
import { ClayCard, ClayButton, ClayInput, ClayBadge } from '../../components/ui/ClayComponents';
import { cn } from '../../utils/cn';
import { format } from 'date-fns';

const Profile = () => {
    const { user, loading: authLoading, logout } = useAuth();
    const { userId } = useParams(); // Get user ID from URL
    const navigate = useNavigate();
    const location = useLocation();
    const [viewMode, setViewMode] = useState('dashboard');

    const [dashboardData, setDashboardData] = useState({
        stats: [],
        recentActivity: [],
        todaySessions: [],
        recruitment: []
    });
    const [profile, setProfile] = useState({
        bio: '',
        city: '',
        phoneNumber: '',
        skills: [],
        displayName: '',
        avatarUrl: ''
    });
    const [loading, setLoading] = useState(false);
    const [uploading, setUploading] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);
    const [showFullscreenImage, setShowFullscreenImage] = useState(false);
    const [passwordData, setPasswordData] = useState({ currentPassword: '', newPassword: '' });

    useEffect(() => {
        if (user) {
            fetchProfile();
            // Only fetch dashboard data if viewing own profile
            if (!userId || userId === user.id) {
                fetchDashboardData();
            }
        }
    }, [user, userId]);

    const fetchProfile = async () => {
        try {
            // Fetch specific user if ID provided, else current user
            const targetUserId = userId || user.id;
            const data = await userService.getProfile(targetUserId); // API needs to support this

            if (data) {
                setProfile(prev => ({
                    ...prev,
                    ...data,
                    displayName: data.displayName || (targetUserId === user.id ? user?.displayName : 'User')
                }));
            }
        } catch (error) {
            console.error("Fetch profile error", error);
            // navigate to 404 or show error
        }
    };

    const fetchDashboardData = async () => {
        try {
            const isTeacher = user?.roles?.includes('TEACHER');

            // Fetch common data (My Bookings as a student)
            const myBookingsPromise = bookingService.getMyBookings().catch(err => { console.error('MyBooking err:', err); return []; });

            // Conditional data fetching
            const providerBookingsPromise = isTeacher
                ? bookingService.getMyProviderBookings().catch(err => { console.error('ProviderBooking err:', err); return []; })
                : Promise.resolve([]);

            const mySkillsPromise = isTeacher
                ? skillService.getMySkills().catch(err => { console.error('MySkills err:', err); return []; })
                : Promise.resolve([]);

            const [myBookingsRes, providerBookingsRes, mySkillsRes] = await Promise.all([
                myBookingsPromise,
                providerBookingsPromise,
                mySkillsPromise
            ]);

            console.log('Bookings:', myBookingsRes, 'Provider:', providerBookingsRes, 'Skills:', mySkillsRes);

            const myBookings = Array.isArray(myBookingsRes) ? myBookingsRes : [];
            const providerBookings = Array.isArray(providerBookingsRes) ? providerBookingsRes : [];
            const mySkills = Array.isArray(mySkillsRes) ? mySkillsRes : [];

            // Combine bookings only if helpful, but for stats we might want to separate
            // For general activity feed, we can mix them, or prioritize Role
            const allBookings = [...myBookings, ...providerBookings].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));

            // Active Bookings: As Student (MyBookings CONFIRMED/PENDING), As Teacher (ProviderBookings CONFIRMED/PENDING)
            // Let's just sum them up for "Active Sessions" context
            const activeBookingsCount = myBookings.filter(b => b.status === 'CONFIRMED' || b.status === 'PENDING').length +
                providerBookings.filter(b => b.status === 'CONFIRMED' || b.status === 'PENDING').length;

            // Calculate total hours involved
            const totalHours = allBookings.reduce((acc, curr) => {
                const start = new Date(curr.startTime);
                const end = new Date(curr.endTime);
                return acc + (end - start) / (1000 * 60 * 60);
            }, 0);

            // Today's sessions
            const today = new Date().toDateString();
            const todaySessions = allBookings.filter(b =>
                new Date(b.startTime).toDateString() === today && b.status === 'CONFIRMED'
            ).sort((a, b) => new Date(a.startTime) - new Date(b.startTime));

            // Dynamic Stats based on Role
            let dynamicStats = [];
            if (isTeacher) {
                dynamicStats = [
                    { label: 'Active Bookings', value: activeBookingsCount, icon: Calendar, color: 'text-indigo-600', bg: 'bg-indigo-50' },
                    { label: 'Skills Shared', value: mySkills.length, icon: Briefcase, color: 'text-rose-600', bg: 'bg-rose-50' },
                    { label: 'Mentoring Hours', value: Math.round(totalHours), icon: Clock, color: 'text-amber-600', bg: 'bg-amber-50' },
                    { label: 'Total Students', value: new Set(providerBookings.map(b => b.userId)).size, icon: Users, color: 'text-emerald-600', bg: 'bg-emerald-50' },
                ];
            } else {
                dynamicStats = [
                    { label: 'Active Bookings', value: activeBookingsCount, icon: Calendar, color: 'text-indigo-600', bg: 'bg-indigo-50' },
                    { label: 'Skills Learned', value: new Set(myBookings.map(b => b.skillId)).size, icon: Star, color: 'text-rose-600', bg: 'bg-rose-50' },
                    { label: 'Learning Hours', value: Math.round(totalHours), icon: Clock, color: 'text-amber-600', bg: 'bg-amber-50' },
                    { label: 'Mentors met', value: new Set(myBookings.map(b => b.providerId)).size, icon: Users, color: 'text-emerald-600', bg: 'bg-emerald-50' },
                ];
            }

            setDashboardData({
                stats: dynamicStats,
                recentActivity: allBookings.slice(0, 5),
                todaySessions: todaySessions,
                recruitment: providerBookings.filter(b => b.status === 'PENDING').slice(0, 5) // Requests for me
            });
        } catch (error) {
            console.error("Failed to fetch dashboard data", error);
        }
    };

    const handleUpdate = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await userService.updateProfile(profile);
            toast.success('Profile updated successfully');
            setViewMode('dashboard'); // Switch back to dashboard after save
        } catch (error) {
            console.error(error);
            toast.error('Failed to update profile');
        } finally {
            setLoading(false);
        }
    };

    const handleAvatarUpload = async (e) => {
        const file = e.target.files[0];
        if (!file) return;

        setUploading(true);
        try {
            const data = await userService.uploadAvatar(file);
            setProfile(prev => ({ ...prev, avatarUrl: data.avatarUrl }));
            toast.success('Avatar updated!');
        } catch (error) {
            console.error(error);
            toast.error('Failed to upload avatar');
        } finally {
            setUploading(false);
        }
    };

    const handlePasswordChange = async (e) => {
        e.preventDefault();
        if (!passwordData.currentPassword || !passwordData.newPassword) return;
        setLoading(true);
        try {
            await authService.changePassword(user.id, passwordData.currentPassword, passwordData.newPassword);
            toast.success('Password changed successfully');
            setPasswordData({ currentPassword: '', newPassword: '' });
        } catch (error) {
            console.error(error);
            toast.error(error.response?.data?.message || 'Failed to change password');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteAccount = () => {
        setShowDeleteModal(true);
    };

    const confirmDelete = async () => {
        try {
            await userService.deleteUser(user.id);
            toast.success('Account deleted successfully');
            await logout();
            navigate('/login');
        } catch (error) {
            console.error("Delete account error:", error);
            toast.error('Failed to delete account');
            setShowDeleteModal(false);
        }
    };

    if (authLoading) return (
        <div className="flex items-center justify-center min-h-[50vh]">
            <Loader2 className="w-10 h-10 text-indigo-600 animate-spin" />
        </div>
    );

    const renderWelcomeBanner = () => (
        <div className="bg-gradient-to-r from-blue-600 to-indigo-600 rounded-[2.5rem] p-8 shadow-xl shadow-blue-600/20 mb-8 relative overflow-hidden text-white">
            {/* Background Pattern */}
            <div className="absolute top-0 right-0 w-96 h-96 bg-white/10 rounded-full blur-3xl -translate-y-1/2 translate-x-1/2"></div>
            <div className="absolute bottom-0 left-0 w-64 h-64 bg-indigo-500/30 rounded-full blur-2xl translate-y-1/2 -translate-x-1/4"></div>

            <div className="relative z-10 flex flex-col md:flex-row items-center justify-between gap-8">
                <div className="max-w-xl">
                    <h1 className="text-3xl font-black font-display mb-3 tracking-tight">
                        Good Morning, {profile.displayName?.split(' ')[0] || 'User'}!
                    </h1>
                    <p className="text-blue-100 font-medium text-lg leading-relaxed mb-6">
                        You have <span className="font-bold text-white bg-white/20 px-2.5 py-0.5 rounded-lg backdrop-blur-sm border border-white/10">{dashboardData.todaySessions.length} active sessions</span> today. It's a great day to share knowledge!
                    </p>
                    <div>
                        <button
                            onClick={() => setViewMode(viewMode === 'dashboard' ? 'edit' : 'dashboard')}
                            className="flex items-center gap-2 px-5 py-2.5 bg-white text-blue-600 font-bold rounded-xl hover:bg-blue-50 hover:scale-105 active:scale-95 transition-all shadow-lg shadow-blue-900/10"
                        >
                            {viewMode === 'dashboard' ? <Edit3 className="w-4 h-4" /> : <LayoutDashboard className="w-4 h-4" />}
                            {viewMode === 'dashboard' ? 'Edit Profile' : 'Back to Dashboard'}
                        </button>
                    </div>
                </div>

                {/* Avatar/Illustration Area */}
                <div className="hidden md:block relative group">
                    <div className="absolute inset-0 bg-white/20 rounded-full blur-md transform scale-110 opacity-0 group-hover:opacity-100 transition-opacity"></div>
                    <div
                        className="w-32 h-32 rounded-full border-4 border-white/30 shadow-2xl relative z-10 overflow-hidden bg-white/10 backdrop-blur-sm flex items-center justify-center cursor-pointer"
                        onClick={() => profile.avatarUrl && setShowFullscreenImage(true)}
                    >
                        {profile.avatarUrl ? (
                            <img src={profile.avatarUrl} alt="Me" className="w-full h-full object-cover" />
                        ) : (
                            <User className="w-16 h-16 text-white/80" />
                        )}
                    </div>
                </div>
            </div>
        </div>
    );

    const renderDashboardView = () => (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
            {/* Left Column - Main Content */}
            <div className="lg:col-span-2 space-y-8">
                {/* Stats Row */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    {dashboardData.stats.map((stat, idx) => (
                        <ClayCard key={idx} className="p-5 flex flex-col items-center justify-center text-center group hover:border-primary-200">
                            <div className={cn("p-2.5 rounded-xl mb-3 transition-transform group-hover:scale-110", stat.bg, stat.color)}>
                                <stat.icon className="w-6 h-6" />
                            </div>
                            <h3 className="text-2xl font-bold text-gray-900 tracking-tight">{stat.value}</h3>
                            <p className="text-[10px] font-bold text-gray-400 uppercase tracking-wide">{stat.label}</p>
                        </ClayCard>
                    ))}
                    {dashboardData.stats.length === 0 && (
                        <div className="col-span-4 text-center py-4 text-slate-400">Loading stats...</div>
                    )}
                </div>

                {/* Recent Activity / Applications */}
                <ClayCard className="p-6">
                    <div className="flex items-center justify-between mb-6">
                        <h2 className="text-lg font-serif font-bold text-gray-900">Recent Activity</h2>
                        <ClayButton variant="secondary" className="px-3 py-1 text-xs">View All</ClayButton>
                    </div>
                    <div className="space-y-3">
                        {dashboardData.recentActivity.length > 0 ? dashboardData.recentActivity.map((booking) => (
                            <div key={booking.id} className="group flex items-center gap-4 p-4 rounded-2xl bg-white border border-gray-100 hover:border-primary-100 hover:shadow-clay-card transition-all duration-300">
                                <div className={cn("w-12 h-12 rounded-2xl flex items-center justify-center shrink-0 shadow-sm transition-colors",
                                    booking.status === 'COMPLETED' ? "bg-emerald-100 text-emerald-600" : "bg-blue-50 text-blue-600"
                                )}>
                                    {booking.status === 'COMPLETED' ? <CheckCircle2 className="w-6 h-6" /> : <Clock className="w-6 h-6" />}
                                </div>
                                <div className="flex-1 min-w-0">
                                    <h4 className="font-bold text-gray-900 text-sm truncate">{booking.skillTitle}</h4>
                                    <p className="text-xs text-gray-500 font-medium flex items-center gap-1.5 mt-0.5">
                                        <span className={cn("w-1.5 h-1.5 rounded-full",
                                            booking.status === 'COMPLETED' ? "bg-emerald-500" : "bg-blue-500"
                                        )}></span>
                                        {booking.status} <span className="text-gray-300">•</span> {format(new Date(booking.startTime), 'MMM d, h:mm a')}
                                    </p>
                                </div>
                                <div className="opacity-0 group-hover:opacity-100 transition-opacity flex items-center gap-1">
                                    <ClayButton variant="icon" className="w-8 h-8 p-0"><Mail className="w-4 h-4" /></ClayButton>
                                    <ClayButton variant="icon" className="w-8 h-8 p-0"><ChevronRight className="w-4 h-4" /></ClayButton>
                                </div>
                            </div>
                        )) : (
                            <div className="text-center py-8 bg-gray-50 rounded-2xl border border-dashed border-gray-200">
                                <Clock className="w-8 h-8 text-gray-300 mx-auto mb-2" />
                                <p className="text-gray-400 text-sm font-medium">No recent activity</p>
                            </div>
                        )}
                    </div>
                </ClayCard>

                {/* Pending Requests (Recruitment) */}
                {user?.roles?.includes('TEACHER') && (
                    <div className="bg-white p-6 rounded-[2rem] shadow-clay-card border border-white/50">
                        <div className="flex items-center justify-between mb-6">
                            <h2 className="text-xl font-black text-slate-800">Pending Requests</h2>
                            <button className="text-indigo-600 text-sm font-bold hover:bg-indigo-50 px-3 py-1 rounded-lg transition-colors">View All</button>
                        </div>
                        <div className="overflow-x-auto">
                            <table className="w-full text-left border-collapse">
                                <thead>
                                    <tr className="text-xs font-black text-slate-400 uppercase tracking-wider border-b border-slate-100">
                                        <th className="pb-3 pl-2">User</th>
                                        <th className="pb-3">Skill</th>
                                        <th className="pb-3">Status</th>
                                        <th className="pb-3 text-right">Action</th>
                                    </tr>
                                </thead>
                                <tbody className="text-sm font-medium text-slate-600">
                                    {dashboardData.recruitment.length > 0 ? dashboardData.recruitment.map(booking => (
                                        <tr key={booking.id} className="group">
                                            <td className="py-4 pl-2 font-bold text-slate-800">{booking.userName || 'Unknown'}</td>
                                            <td className="py-4">{booking.skillTitle}</td>
                                            <td className="py-4"><span className="flex items-center gap-1.5 text-amber-600 font-bold text-xs"><span className="w-2 h-2 rounded-full bg-amber-500"></span> Pending</span></td>
                                            <td className="py-4 text-right"><MoreButton /></td>
                                        </tr>
                                    )) : (
                                        <tr><td colSpan="4" className="text-center py-4 text-slate-400">No pending requests</td></tr>
                                    )}
                                </tbody>
                            </table>
                        </div>
                    </div>
                )}
            </div>

            {/* Right Widget Column */}
            <div className="space-y-8">
                {/* Profile Widget */}
                <ClayCard className="p-6 text-center">
                    <div className="w-20 h-20 mx-auto mb-4 relative">
                        <div
                            className="w-full h-full rounded-full bg-gray-100 overflow-hidden border border-gray-200 cursor-pointer hover:ring-2 hover:ring-indigo-300 transition-all"
                            onClick={() => profile.avatarUrl && setShowFullscreenImage(true)}
                        >
                            {profile.avatarUrl ? <img src={profile.avatarUrl} className="w-full h-full object-cover" /> : <User className="w-full h-full p-4 text-gray-300" />}
                        </div>
                        <div className="absolute bottom-0 right-0 w-5 h-5 bg-emerald-500 border-2 border-white rounded-full"></div>
                    </div>
                    <h3 className="font-bold text-lg text-gray-900">{profile.displayName || 'User'}</h3>
                    <p className="text-sm font-medium text-gray-500 mb-6">
                        {(() => {
                            // Check for passed roles from navigation state
                            const passedRoles = location.state?.roles;

                            if (passedRoles) {
                                if (passedRoles.includes('ADMIN')) return 'Administrator';
                                if (passedRoles.includes('TEACHER')) return 'Senior Mentor';
                                return 'Student';
                            }
                            // Fallback based on isProvider (if viewing own profile, user.roles might be valid, but be careful)
                            if (profile.userId === user?.id) {
                                if (user?.roles?.includes('ADMIN')) return 'Administrator';
                                if (user?.roles?.includes('TEACHER')) return 'Senior Mentor';
                                if (user?.roles?.includes('STUDENT')) return 'Student';
                            }
                            // Default from profile data
                            return profile.isProvider ? 'Senior Mentor' : 'Student';
                        })()}
                    </p>
                    {/* <div className="flex justify-center gap-2">
                        <ClayButton variant="icon"><Settings className="w-5 h-5" /></ClayButton>
                        <ClayButton variant="icon"><Bell className="w-5 h-5" /></ClayButton>
                    </div> */}
                </ClayCard>

                {/* Calendar Widget */}
                <ClayCard className="p-6">
                    <div className="flex items-center justify-between mb-6">
                        <div className="flex items-center gap-2">
                            <h3 className="font-bold text-slate-800">Schedule Calendar</h3>
                            <div className="flex gap-1 text-slate-400">
                                <button className="hover:text-indigo-600 transition-colors"><ChevronLeft className="w-4 h-4" /></button>
                                <button className="hover:text-indigo-600 transition-colors"><ChevronRight className="w-4 h-4" /></button>
                            </div>
                        </div>
                        <span className="text-xs font-bold text-indigo-600 bg-indigo-50 px-3 py-1.5 rounded-xl">
                            {format(new Date(), 'MMM')}
                        </span>
                    </div>

                    <div className="flex justify-between items-center mb-8">
                        {['Mon', 'Tue', 'Wed', 'Thu', 'Fri'].map((day, i) => {
                            const today = new Date();
                            const currentDay = today.getDay(); // 0-6 (Sun-Sat)
                            // i: 0=Mon, 1=Tue, 2=Wed, 3=Thu, 4=Fri
                            // currentDay: 1=Mon...
                            const isToday = (i + 1) === currentDay;

                            const dateNum = new Date();
                            dateNum.setDate(today.getDate() - (currentDay - (i + 1)));

                            return (
                                <div key={i} className={cn(
                                    "w-11 h-16 flex flex-col items-center justify-center rounded-2xl transition-all duration-300 cursor-pointer",
                                    isToday
                                        ? "bg-indigo-600 text-white shadow-lg shadow-indigo-600/30 scale-110"
                                        : "hover:bg-indigo-50 text-slate-500"
                                )}>
                                    <span className={cn("text-[10px] font-bold mb-1", isToday ? "text-indigo-200" : "text-slate-400")}>{day}</span>
                                    <span className="text-lg font-black">{dateNum.getDate()}</span>
                                </div>
                            );
                        })}
                    </div>

                    {/* Event List */}
                    <div className="space-y-3">
                        {dashboardData.todaySessions.length > 0 ? dashboardData.todaySessions.map(session => (
                            <div key={session.id} className="flex items-center gap-3 p-3 rounded-xl bg-slate-50 border border-slate-100">
                                <div className="text-xs font-bold text-slate-900 flex flex-col leading-tight text-center w-10">
                                    <span>{format(new Date(session.startTime), 'h:mm')}</span><span>{format(new Date(session.startTime), 'a')}</span>
                                </div>
                                <div className="w-px h-8 bg-slate-200"></div>
                                <div>
                                    <h4 className="font-bold text-slate-800 text-xs">{session.skillTitle}</h4>
                                    <p className="text-[10px] font-semibold text-slate-500">Video Call • {((new Date(session.endTime) - new Date(session.startTime)) / (1000 * 60))}m</p>
                                </div>
                            </div>
                        )) : (
                            <div className="text-center py-6">
                                <p className="text-xs text-slate-400 font-medium">No sessions today</p>
                            </div>
                        )}
                    </div>
                </ClayCard>

            </div >
        </div >
    );

    const renderEditView = () => (
        <div className="bg-white p-8 rounded-[2.5rem] shadow-clay-card border border-white/50 max-w-2xl mx-auto">
            <div className="flex items-center justify-between mb-8">
                <h2 className="text-2xl font-black text-slate-800">Edit Profile</h2>
                <button onClick={() => setViewMode('dashboard')} className="text-slate-400 hover:text-slate-600">Cancel</button>
            </div>

            {/* Reuse the form logic from before */}
            <form onSubmit={handleUpdate} className="space-y-6">
                <div className="flex justify-center mb-6">
                    <label className="relative cursor-pointer group">
                        <div className="w-24 h-24 rounded-full bg-slate-100 overflow-hidden border-4 border-white shadow-lg">
                            {profile.avatarUrl ? <img src={profile.avatarUrl} className="w-full h-full object-cover" /> : <User className="w-full h-full p-6 text-slate-300" />}
                        </div>
                        <div className="absolute inset-0 bg-black/30 rounded-full flex items-center justify-center opacity-0 group-hover:opacity-100 transition-opacity">
                            <Camera className="w-8 h-8 text-white" />
                        </div>
                        <input type="file" className="hidden" onChange={handleAvatarUpload} disabled={uploading} />
                    </label>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div>
                        <label className="text-xs font-black text-slate-400 uppercase tracking-wider ml-1 mb-2 block">Display Name</label>
                        <ClayInput className="bg-slate-50 border-transparent focus:bg-white transition-colors" value={profile.displayName} onChange={(e) => setProfile({ ...profile, displayName: e.target.value })} icon={User} />
                    </div>
                    <div>
                        <label className="text-xs font-black text-slate-400 uppercase tracking-wider ml-1 mb-2 block">Location</label>
                        <ClayInput className="bg-slate-50 border-transparent focus:bg-white transition-colors" value={profile.city} onChange={(e) => setProfile({ ...profile, city: e.target.value })} icon={MapPin} />
                    </div>
                </div>
                <div>
                    <label className="text-xs font-black text-slate-400 uppercase tracking-wider ml-1 mb-2 block">Bio</label>
                    <textarea
                        className="w-full p-4 bg-slate-50 rounded-2xl border border-slate-100 focus:outline-none focus:ring-2 focus:ring-indigo-100 resize-none text-sm font-medium text-slate-700"
                        rows={4}
                        value={profile.bio}
                        onChange={(e) => setProfile({ ...profile, bio: e.target.value })}
                    />
                </div>
                <ClayButton type="submit" disabled={loading} className="w-full py-4 text-base">Save Changes</ClayButton>
            </form>

            <div className="mt-8 pt-8 border-t border-slate-100">
                <h3 className="text-sm font-black text-slate-400 uppercase tracking-wider mb-4">Security</h3>
                <div className="bg-slate-50 p-6 rounded-2xl border border-slate-100">
                    <h4 className="font-bold text-slate-800 mb-4 flex items-center gap-2"><Lock className="w-4 h-4 text-indigo-500" /> Change Password</h4>
                    <div className="space-y-4">
                        <div>
                            <label className="text-xs font-black text-slate-400 uppercase tracking-wider ml-1 mb-2 block">Current Password</label>
                            <ClayInput
                                type="password"
                                className="bg-slate-50 border-transparent focus:bg-white transition-colors"
                                value={passwordData.currentPassword}
                                onChange={(e) => setPasswordData({ ...passwordData, currentPassword: e.target.value })}
                                placeholder="••••••••"
                            />
                        </div>
                        <div>
                            <label className="text-xs font-black text-slate-400 uppercase tracking-wider ml-1 mb-2 block">New Password</label>
                            <ClayInput
                                type="password"
                                className="bg-slate-50 border-transparent focus:bg-white transition-colors"
                                value={passwordData.newPassword}
                                onChange={(e) => setPasswordData({ ...passwordData, newPassword: e.target.value })}
                                placeholder="••••••••"
                            />
                        </div>
                        <ClayButton
                            variant="secondary"
                            onClick={handlePasswordChange}
                            disabled={loading || !passwordData.currentPassword || !passwordData.newPassword}
                            className="w-full"
                        >
                            Update Password
                        </ClayButton>
                    </div>
                </div>
            </div>

            <div className="mt-8 pt-8 border-t border-slate-100">
                <h3 className="text-sm font-black text-rose-500 uppercase tracking-wider mb-4">Danger Zone</h3>
                <ClayButton
                    onClick={handleDeleteAccount}
                    className="w-full py-4 text-base font-bold text-rose-600 bg-rose-50 hover:bg-rose-100 border-rose-200"
                >
                    Delete Account
                </ClayButton>
            </div>
        </div>
    );

    const MoreButton = () => (
        <button className="text-slate-400 hover:text-indigo-600"><span className="text-lg leading-none">⋮</span></button>
    );

    return (
        <div className="max-w-6xl mx-auto pb-10">
            {renderWelcomeBanner()}
            {viewMode === 'dashboard' ? renderDashboardView() : renderEditView()}

            {/* Delete Confirmation Modal */}
            {showDeleteModal && (
                <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
                    <div className="bg-white rounded-[2rem] w-full max-w-md shadow-2xl animate-in fade-in zoom-in duration-200 p-8 text-center border border-white/20">
                        <div className="w-20 h-20 bg-rose-50 rounded-full flex items-center justify-center mx-auto mb-6 animate-bounce">
                            <div className="w-14 h-14 bg-rose-100 rounded-full flex items-center justify-center">
                                <AlertTriangle className="w-8 h-8 text-rose-600" />
                            </div>
                        </div>
                        <h3 className="text-2xl font-black text-slate-900 mb-3 font-display">Delete Account?</h3>
                        <p className="text-slate-500 font-medium leading-relaxed mb-8">
                            Are you sure you want to delete your account?<br />
                            <span className="text-rose-500 font-bold">This action cannot be undone.</span>
                        </p>
                        <div className="flex gap-4 justify-center">
                            <button
                                onClick={() => setShowDeleteModal(false)}
                                className="px-6 py-3 rounded-xl bg-slate-50 text-slate-600 font-bold hover:bg-slate-100 transition-colors"
                            >
                                Cancel
                            </button>
                            <button
                                onClick={confirmDelete}
                                className="px-6 py-3 rounded-xl bg-rose-600 text-white font-bold hover:bg-rose-700 shadow-lg shadow-rose-200 transition-all hover:scale-105 active:scale-95"
                            >
                                Yes, Delete It
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* Fullscreen Image Modal */}
            {showFullscreenImage && profile.avatarUrl && (
                <div
                    className="fixed inset-0 bg-black/90 backdrop-blur-md z-[100] flex items-center justify-center p-4 animate-in fade-in duration-200"
                    onClick={() => setShowFullscreenImage(false)}
                >
                    <button
                        onClick={() => setShowFullscreenImage(false)}
                        className="absolute top-6 right-6 w-12 h-12 bg-white/10 hover:bg-white/20 rounded-full flex items-center justify-center text-white transition-all hover:scale-110 active:scale-95"
                    >
                        <X className="w-6 h-6" />
                    </button>
                    <img
                        src={profile.avatarUrl}
                        alt="Profile"
                        className="max-w-full max-h-full object-contain rounded-2xl shadow-2xl animate-in zoom-in-95 duration-300"
                        onClick={(e) => e.stopPropagation()}
                    />
                </div>
            )}
        </div>
    );
};

export default Profile;
