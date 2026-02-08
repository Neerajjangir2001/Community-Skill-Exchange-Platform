import React, { useState, useEffect } from 'react';
import { reviewService } from '../../api/reviews';
import toast from 'react-hot-toast';
import { Check, X, Star, User, Clock, AlertTriangle, Filter } from 'lucide-react';
import { format } from 'date-fns';
import { cn } from '../../utils/cn';

const AdminReviews = () => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [filterStatus, setFilterStatus] = useState('PENDING');

    useEffect(() => {
        fetchReviews();
    }, [filterStatus]);

    const fetchReviews = async () => {
        setLoading(true);
        try {
            const data = await reviewService.getAllReviews(filterStatus === 'ALL' ? null : filterStatus);
            setReviews(data);
        } catch (error) {
            console.error(error);
            toast.error('Failed to load reviews');
        } finally {
            setLoading(false);
        }
    };

    const handleModerate = async (id, status) => {
        try {
            await reviewService.moderateReview(id, status);
            toast.success(`Review ${status.toLowerCase()}`);
            fetchReviews();
        } catch (error) {
            toast.error('Action failed');
        }
    };

    return (
        <div className="p-4 md:p-8 max-w-7xl mx-auto mb-10">
            {/* Header Section */}
            <div className="flex flex-col md:flex-row justify-between items-start md:items-center mb-10 gap-4">
                <div>
                    <h1 className="text-2xl md:text-3xl font-bold text-primary-800 tracking-tight font-sans">Review Moderation</h1>
                    <p className="text-gray-400 font-medium mt-1">Manage and moderate student feedback effectively.</p>
                </div>

                {/* Filter Pill */}
                <div className="bg-white p-1 rounded-full shadow-[0px_18px_40px_rgba(112,144,176,0.12)] flex items-center">
                    {['PENDING', 'APPROVED', 'REJECTED', 'ALL'].map((status) => (
                        <button
                            key={status}
                            onClick={() => setFilterStatus(status)}
                            className={cn(
                                "px-4 py-2 rounded-full text-xs font-bold transition-all duration-300",
                                filterStatus === status
                                    ? "bg-primary-600 text-white shadow-md transform scale-105"
                                    : "text-gray-400 hover:text-primary-600 hover:bg-gray-50"
                            )}
                        >
                            {status === 'ALL' ? 'All Reviews' : status.charAt(0) + status.slice(1).toLowerCase()}
                        </button>
                    ))}
                </div>
            </div>

            {loading ? (
                <div className="flex items-center justify-center min-h-[400px]">
                    <div className="w-12 h-12 border-4 border-primary-100 border-t-primary-600 rounded-full animate-spin"></div>
                </div>
            ) : reviews.length === 0 ? (
                <div className="bg-white rounded-[20px] p-12 text-center shadow-[0px_18px_40px_rgba(112,144,176,0.12)] border border-white/50">
                    <div className="w-20 h-20 bg-emerald-50 rounded-full flex items-center justify-center mx-auto mb-6">
                        <Check className="w-10 h-10 text-emerald-500" />
                    </div>
                    <h3 className="text-xl font-bold text-primary-800 mb-2">All Caught Up!</h3>
                    <p className="text-gray-400 max-w-md mx-auto">There are no reviews matching the selected filter. Great job keeping specific clean!</p>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
                    {reviews.map((review) => (
                        <div key={review.id} className="bg-white rounded-[20px] p-6 shadow-[0px_18px_40px_rgba(112,144,176,0.12)] border border-transparent hover:border-primary-100 transition-all duration-300 flex flex-col group">

                            {/* Card Header: User & Status */}
                            <div className="flex justify-between items-start mb-6">
                                <div className="flex items-center gap-4">
                                    <div className="w-12 h-12 rounded-full bg-gradient-to-br from-primary-100 to-primary-50 flex items-center justify-center text-primary-600 font-bold text-lg shadow-sm">
                                        {/* Ideally User Avatar if available, else Initials */}
                                        <User className="w-6 h-6" />
                                    </div>
                                    <div>
                                        <h4 className="font-bold text-primary-800 text-sm">Student ID: {review.studentId?.substring(0, 6)}...</h4>
                                        <div className="flex items-center gap-1 text-xs font-bold text-gray-400 mt-0.5">
                                            <Clock className="w-3 h-3" />
                                            {format(new Date(review.createdAt), 'MMM d, yyyy')}
                                        </div>
                                    </div>
                                </div>

                                <span className={cn(
                                    "px-3 py-1 rounded-lg text-[10px] font-black uppercase tracking-wider",
                                    review.status === 'APPROVED' ? "bg-emerald-50 text-emerald-600" :
                                        review.status === 'REJECTED' ? "bg-rose-50 text-rose-600" :
                                            "bg-amber-50 text-amber-600"
                                )}>
                                    {review.status}
                                </span>
                            </div>

                            {/* Rating Stars */}
                            <div className="flex gap-1 mb-3">
                                {[...Array(5)].map((_, i) => (
                                    <Star key={i} className={cn("w-4 h-4", i < review.rating ? "fill-amber-400 text-amber-400" : "text-gray-200")} />
                                ))}
                            </div>

                            {/* Review Content */}
                            <div className="flex-1 mb-6">
                                <p className="text-gray-600 text-sm leading-relaxed font-medium">"{review.comment}"</p>
                            </div>

                            {/* Action Buttons */}
                            <div className="grid grid-cols-2 gap-3 mt-auto pt-6 border-t border-gray-50">
                                {review.status !== 'APPROVED' ? (
                                    <button
                                        onClick={() => handleModerate(review.id, 'APPROVED')}
                                        className="py-2.5 rounded-[12px] bg-emerald-50 text-emerald-600 hover:bg-emerald-500 hover:text-white transition-all font-bold text-xs flex items-center justify-center gap-2 group/btn"
                                    >
                                        <Check className="w-4 h-4" /> Approve
                                    </button>
                                ) : (
                                    <button disabled className="py-2.5 rounded-[12px] bg-gray-50 text-gray-300 font-bold text-xs flex items-center justify-center gap-2 cursor-not-allowed">
                                        <Check className="w-4 h-4" /> Approved
                                    </button>
                                )}

                                {review.status !== 'REJECTED' ? (
                                    <button
                                        onClick={() => handleModerate(review.id, 'REJECTED')}
                                        className="py-2.5 rounded-[12px] bg-rose-50 text-rose-600 hover:bg-rose-500 hover:text-white transition-all font-bold text-xs flex items-center justify-center gap-2"
                                    >
                                        <X className="w-4 h-4" /> Reject
                                    </button>
                                ) : (
                                    <button disabled className="py-2.5 rounded-[12px] bg-gray-50 text-gray-300 font-bold text-xs flex items-center justify-center gap-2 cursor-not-allowed">
                                        <X className="w-4 h-4" /> Rejected
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            )}
        </div>
    );
};

export default AdminReviews;
