import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { bookingService } from '../../api/bookings';
import { chatService } from '../../api/chat';
import { format } from 'date-fns';
import toast from 'react-hot-toast';
import { Check, X, Calendar, Clock, User, MessageSquare, AlertTriangle, Loader2 } from 'lucide-react';
import { cn } from '../../utils/cn';
import { useAuth } from '../../context/AuthContext';
import { ClayCard, ClayButton, ClayBadge, ClayModal } from '../../components/ui/ClayComponents';
import ReviewModal from '../../components/ReviewModal';

const ActionModal = ({ isOpen, onClose, title, message, onConfirm, confirmText = "Confirm", type = "primary" }) => {
    const [reason, setReason] = useState('');
    const [loading, setLoading] = useState(false);

    if (!isOpen) return null;

    const handleConfirm = async () => {
        setLoading(true);
        try {
            await onConfirm(reason);
            onClose();
        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    };

    return (
        <ClayModal isOpen={isOpen} onClose={onClose} title={title}>
            <p className="text-slate-600 mb-6 leading-relaxed">{message}</p>

            {type === 'danger' && (
                <div className="mb-6">
                    <label className="block text-sm font-bold text-slate-700 mb-2">Reason (Required)</label>
                    <textarea
                        value={reason}
                        onChange={(e) => setReason(e.target.value)}
                        className="w-full p-4 bg-clay-bg shadow-clay-inner rounded-2xl border-none outline-none focus:ring-2 focus:ring-rose-200 transition-all resize-none text-sm font-medium text-slate-700"
                        rows="3"
                        placeholder="Please provide a reason..."
                    />
                </div>
            )}

            <div className="flex gap-3 justify-end">
                <ClayButton
                    variant="secondary"
                    onClick={onClose}
                    className="px-6"
                >
                    Cancel
                </ClayButton>
                <ClayButton
                    onClick={handleConfirm}
                    disabled={loading || (type === 'danger' && !reason.trim())}
                    className={cn(
                        "px-6",
                        type === 'danger' ? "bg-rose-500 hover:bg-rose-600 shadow-rose-200" : ""
                    )}
                >
                    {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                    {confirmText}
                </ClayButton>
            </div>
        </ClayModal>
    );
};

const MyBookings = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const activeTabState = useState(user?.roles?.includes('TEACHER') ? 'teacher' : 'student');
    const [activeTab, setActiveTab] = activeTabState;
    const [bookings, setBookings] = useState([]);
    const [loading, setLoading] = useState(true);

    // Modal State
    const [modalConfig, setModalConfig] = useState(null);
    const [reviewModalConfig, setReviewModalConfig] = useState(null);

    useEffect(() => {
        fetchBookings();
    }, [activeTab]);

    const fetchBookings = async () => {
        setLoading(true);
        try {
            let data = [];
            if (activeTab === 'student') {
                data = await bookingService.getMyBookings();
            } else {
                data = await bookingService.getMyProviderBookings();
            }
            // Sort by Created At desc (Newest first)
            setBookings(data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
        } catch (error) {
            console.error('Failed to fetch bookings', error);
            toast.error('Failed to load bookings');
        } finally {
            setLoading(false);
        }
    };

    const handleRejectClick = (id) => {
        setModalConfig({
            isOpen: true,
            title: "Reject Booking Request",
            message: "Are you sure you want to reject this booking? This action cannot be undone.",
            confirmText: "Reject Booking",
            type: "danger",
            onConfirm: async (reason) => {
                await bookingService.rejectBooking(id, reason);
                toast.success('Booking rejected');
                fetchBookings();
            }
        });
    };

    const handleCancelTeacherClick = (id) => {
        setModalConfig({
            isOpen: true,
            title: "Cancel Booking",
            message: "As a mentor, cancelling a confirmed booking should only be done in emergencies. Please provide a reason.",
            confirmText: "Cancel Booking",
            type: "danger",
            onConfirm: async (reason) => {
                await bookingService.cancelBookingTeacher(id, reason);
                toast.success('Booking cancelled');
                fetchBookings();
            }
        });
    };

    const handleCancelStudentClick = (id) => {
        // Simple confirm for student cancellation
        if (window.confirm("Are you sure you want to cancel this booking?")) {
            bookingService.cancelBookingStudent(id)
                .then(() => {
                    toast.success('Booking cancelled');
                    fetchBookings();
                })
                .catch(() => toast.error('Failed to cancel booking'));
        }
    };

    const handleAcceptClick = async (id) => {
        try {
            await bookingService.acceptBooking(id);
            toast.success('Booking confirmed!');
            fetchBookings();
        } catch (error) {
            toast.error('Failed to accept booking');
        }
    };

    const handleCompleteClick = async (id) => {
        if (window.confirm("Mark this session as completed?")) {
            try {
                await bookingService.completeBooking(id);
                toast.success('Session completed!');
                fetchBookings();
            } catch (error) {
                toast.error('Failed to complete booking');
            }
        }
    };

    const handleMessageClick = async (booking) => {
        try {
            const otherUserId = activeTab === 'student' ? booking.providerId : booking.userId;

            if (!otherUserId) {
                toast.error('Cannot identify user to message');
                return;
            }

            const conversation = await chatService.getConversation(user.id, otherUserId);
            navigate('/chat', { state: { initialConversation: conversation } });
        } catch (error) {
            console.error('Failed to start chat', error);
            toast.error('Could not open chat');
        }
    };

    const getStatusVariant = (status) => {
        switch (status) {
            case 'CONFIRMED': return 'success';
            case 'PENDING': return 'warning';
            case 'COMPLETED': return 'primary'; // Or neutral/blue
            case 'CANCELLED':
            case 'REJECTED': return 'error';
            default: return 'neutral';
        }
    };

    return (
        <div className="max-w-5xl mx-auto pb-10">
            <h1 className="text-3xl font-bold text-gray-900 mb-8 font-serif tracking-tight">
                {activeTab === 'student' ? 'My Learning Journey' : 'Mentorship Dashboard'}
            </h1>

            {/* Tabs */}
            {user?.roles?.includes('TEACHER') && (
                <div className="flex p-1 bg-gray-100/50 rounded-xl w-fit mb-8 border border-gray-200">
                    {/* <button
                        onClick={() => setActiveTab('student')}
                        className={cn(
                            "px-6 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200",
                            activeTab === 'student'
                                ? "bg-white text-primary-900 shadow-sm border border-gray-200"
                                : "text-gray-500 hover:text-primary-900"
                        )}
                    >
                        Bookings I Made
                    </button> */}
                    <button
                        onClick={() => setActiveTab('teacher')}
                        className={cn(
                            "px-6 py-2.5 rounded-lg text-sm font-semibold transition-all duration-200 flex items-center gap-2",
                            activeTab === 'teacher'
                                ? "bg-white text-primary-900 shadow-sm border border-gray-200"
                                : "text-gray-500 hover:text-primary-900"
                        )}
                    >
                        Requests for Me
                    </button>
                </div>
            )}

            {loading ? (
                <div className="flex flex-col items-center justify-center py-20 pb-32">
                    <Loader2 className="w-12 h-12 text-indigo-600 animate-spin mb-6" />
                    <p className="text-slate-500 font-bold text-lg">Loading your schedule...</p>
                </div>
            ) : bookings.length === 0 ? (
                <ClayCard className="text-center py-20 border-dashed border-2 border-gray-200 shadow-none bg-gray-50/50">
                    <div className="w-16 h-16 bg-white rounded-xl flex items-center justify-center mx-auto mb-4 shadow-sm border border-gray-200">
                        <Calendar className="w-8 h-8 text-primary-500" />
                    </div>
                    <h3 className="text-2xl font-bold text-slate-800 mb-3">No bookings found</h3>
                    <p className="text-slate-500 max-w-md mx-auto mb-8 font-medium">
                        {activeTab === 'student'
                            ? "You haven't booked any sessions yet. Explore skills to find a mentor!"
                            : "You don't have any booking requests yet. Share your profile to get more visibility."}
                    </p>
                    {activeTab === 'student' && (
                        <ClayButton onClick={() => navigate('/dashboard/explore')} className="px-8 py-3 mx-auto">
                            Explore Skills
                        </ClayButton>
                    )}
                </ClayCard>
            ) : (
                <div className="space-y-6">
                    {bookings.map((booking) => (
                        <div key={booking.id} className="relative bg-white rounded-[2.5rem] p-8 shadow-[8px_8px_24px_rgba(0,0,0,0.04),-8px_-8px_24px_rgba(255,255,255,1)] border border-white/60 group transition-all duration-300 hover:shadow-[12px_12px_32px_rgba(0,0,0,0.06),-12px_-12px_32px_rgba(255,255,255,1)] hover:-translate-y-1">
                            <div className="flex flex-col md:flex-row gap-8">
                                {/* Date Box - Clay Pressed Look */}
                                <div className="flex-shrink-0 flex md:flex-col items-center justify-center gap-1 w-full md:w-24 h-24 bg-indigo-50/50 rounded-[1.5rem] text-indigo-900 shadow-inner">
                                    <span className="text-xs font-black uppercase tracking-wider opacity-60">{format(new Date(booking.startTime), 'MMM')}</span>
                                    <span className="text-3xl font-black text-indigo-600">{format(new Date(booking.startTime), 'd')}</span>
                                </div>

                                {/* Main Content */}
                                <div className="flex-grow flex flex-col justify-center">
                                    <div className="flex justify-between items-start mb-2">
                                        <h3 className="text-xl font-bold text-slate-800 group-hover:text-indigo-600 transition-colors">
                                            {booking.skillTitle || 'Mentorship Session'}
                                        </h3>
                                        <ClayBadge variant={getStatusVariant(booking.status)} className="px-3 py-1">
                                            {booking.status}
                                        </ClayBadge>
                                    </div>

                                    <div className="flex flex-wrap items-center gap-4 text-sm font-medium text-slate-500 mb-6">
                                        <div className="flex items-center gap-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100">
                                            <Clock className="w-4 h-4 text-slate-400" />
                                            <span>{format(new Date(booking.startTime), 'h:mm a')} - {format(new Date(booking.endTime), 'h:mm a')}</span>
                                        </div>
                                        <div className="flex items-center gap-2 bg-slate-50 px-3 py-1.5 rounded-lg border border-slate-100">
                                            <User className="w-4 h-4 text-slate-400" />
                                            <span>{activeTab === 'student' ? (booking.providerName || 'Mentor') : (booking.userName || 'Student')}</span>
                                        </div>
                                        {(booking.totalPrice !== undefined && booking.totalPrice !== null) && (
                                            <div className="flex items-center gap-1 text-slate-700 bg-emerald-50/50 px-3 py-1.5 rounded-lg border border-emerald-100/50">
                                                <span className="font-bold">₹{booking.totalPrice}</span>
                                            </div>
                                        )}
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="flex items-center gap-3 pt-6 border-t border-slate-50">
                                        {/* TEACHER ACTIONS */}
                                        {activeTab === 'teacher' && booking.status === 'PENDING' && (
                                            <>
                                                <button onClick={() => handleAcceptClick(booking.id)} className="px-6 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white font-bold rounded-2xl transition-all shadow-lg shadow-emerald-200">
                                                    Accept Request
                                                </button>
                                                <button onClick={() => handleRejectClick(booking.id)} className="px-6 py-2.5 bg-white border-2 border-slate-100 hover:border-rose-100 hover:bg-rose-50 text-slate-500 hover:text-rose-600 font-bold rounded-2xl transition-all">
                                                    Reject
                                                </button>
                                            </>
                                        )}

                                        {activeTab === 'teacher' && booking.status === 'CONFIRMED' && (
                                            <>
                                                <button onClick={() => handleCompleteClick(booking.id)} className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-indigo-200">
                                                    Mark Complete
                                                </button>
                                                <button onClick={() => handleCancelTeacherClick(booking.id)} className="px-4 py-2.5 text-rose-500 hover:bg-rose-50 font-bold rounded-2xl transition-all">
                                                    Cancel
                                                </button>
                                            </>
                                        )}

                                        {/* STUDENT ACTIONS */}
                                        {activeTab === 'student' && (booking.status === 'PENDING' || booking.status === 'CONFIRMED') && (
                                            <button onClick={() => handleCancelStudentClick(booking.id)} className="px-6 py-2.5 bg-white border-2 border-slate-100 hover:border-slate-200 text-slate-500 hover:text-slate-800 font-bold rounded-2xl transition-all">
                                                Cancel Booking
                                            </button>
                                        )}

                                        {activeTab === 'student' && booking.status === 'COMPLETED' && (
                                            <button
                                                onClick={() => setReviewModalConfig({ isOpen: true, teacherId: booking.providerId, studentId: user.id, targetName: booking.providerName })}
                                                className="px-6 py-2.5 bg-indigo-600 hover:bg-indigo-700 text-white font-bold rounded-2xl transition-all shadow-lg shadow-indigo-200"
                                            >
                                                Rate Mentor
                                            </button>
                                        )}

                                        <div className="flex-grow"></div>
                                        <button onClick={() => handleMessageClick(booking)} className="p-2.5 text-slate-400 hover:text-indigo-600 hover:bg-indigo-50 rounded-xl transition-all">
                                            <MessageSquare className="w-5 h-5" />
                                        </button>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {/* Render Modal if active */}
            {modalConfig && (
                <ActionModal
                    isOpen={modalConfig.isOpen}
                    onClose={() => setModalConfig(null)}
                    title={modalConfig.title}
                    message={modalConfig.message}
                    onConfirm={modalConfig.onConfirm}
                    confirmText={modalConfig.confirmText}
                    type={modalConfig.type}
                />
            )}

            {/* Review Modal */}
            {reviewModalConfig && (
                <ReviewModal
                    isOpen={reviewModalConfig.isOpen}
                    onClose={() => setReviewModalConfig(null)}
                    mode="create"
                    teacherId={reviewModalConfig.teacherId}
                    studentId={reviewModalConfig.studentId}
                    targetName={reviewModalConfig.targetName}
                    onSuccess={() => {
                        // Success handled by modal
                    }}
                />
            )}
        </div>
    );
};

export default MyBookings;
