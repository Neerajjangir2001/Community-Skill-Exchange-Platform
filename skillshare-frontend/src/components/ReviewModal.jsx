import React, { useState, useEffect } from 'react';
import { ClayModal } from './ui/ClayComponents';
import { MessageSquare, Loader2, Star, Quote, X } from 'lucide-react';
import { reviewService } from '../api/reviews';
import toast from 'react-hot-toast';
import { cn } from '../utils/cn';
import { format } from 'date-fns';

const ReviewModal = ({ isOpen, onClose, mode = 'view', teacherId, studentId, targetName, onSuccess }) => {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(false);

    // Create Mode State
    const [rating, setRating] = useState(5);
    const [comment, setComment] = useState('');
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        if (isOpen && mode === 'view' && teacherId) {
            fetchReviews();
        }
    }, [isOpen, teacherId, mode]);

    const fetchReviews = async () => {
        setLoading(true);
        try {
            const data = await reviewService.getTeacherReviews(teacherId);
            // Sort by Created At desc
            setReviews(data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)));
        } catch (error) {
            console.error('Failed to fetch reviews', error);
        } finally {
            setLoading(false);
        }
    };

    const handleSubmit = async () => {
        if (!comment.trim()) {
            toast.error('Please write a comment');
            return;
        }
        setSubmitting(true);
        try {
            await reviewService.createReview({
                teacherId,
                studentId,
                rating,
                comment
            });
            toast.success('Review submitted successfully!');
            onSuccess?.();
            onClose();
            setComment('');
            setRating(5);
        } catch (error) {
            console.error(error);
            toast.error('Failed to submit review');
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <ClayModal
            isOpen={isOpen}
            onClose={onClose}
            title={null}
            className="sm:max-w-xl w-full !bg-white border border-slate-100 !rounded-[2.5rem] !shadow-2xl overflow-hidden"
        >
            {mode === 'create' ? (
                <div className="space-y-8 py-8 relative px-6 sm:px-8">
                    {/* Custom Close Button */}
                    <button
                        onClick={onClose}
                        className="absolute top-4 right-4 p-2 text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-all"
                    >
                        <X className="w-5 h-5" />
                    </button>

                    {/* Question & Emojis Section */}
                    <div className="text-center space-y-8 mt-2">
                        <div className="space-y-2">
                            <h3 className="text-2xl sm:text-3xl font-bold text-slate-900 tracking-tight">How was your session?</h3>
                            <p className="text-slate-500 font-medium text-base sm:text-lg">Rate your experience with <span className="text-indigo-600 font-bold">{targetName || 'Mentor'}</span></p>
                        </div>

                        <div className="flex justify-center gap-3 sm:gap-6">
                            {[1, 2, 3, 4, 5].map((value) => (
                                <button
                                    key={value}
                                    onClick={() => setRating(value)}
                                    className="group focus:outline-none transition-all duration-300"
                                >
                                    <div className={cn(
                                        "w-12 h-12 sm:w-14 sm:h-14 rounded-full flex items-center justify-center text-3xl sm:text-4xl transition-all duration-300 transform",
                                        rating === value
                                            ? "scale-125 grayscale-0 drop-shadow-md"
                                            : "grayscale opacity-50 hover:grayscale-0 hover:opacity-100 hover:scale-110"
                                    )}>
                                        {value === 1 && "😠"}
                                        {value === 2 && "🙁"}
                                        {value === 3 && "😐"}
                                        {value === 4 && "🙂"}
                                        {value === 5 && "😍"}
                                    </div>
                                    {rating === value && (
                                        <div className="h-1.5 w-1.5 bg-indigo-600 rounded-full mx-auto mt-2 animate-in fade-in zoom-in"></div>
                                    )}
                                </button>
                            ))}
                        </div>
                    </div>

                    {/* Feedback Section */}
                    <div className="space-y-4">
                        <div className="relative">
                            <textarea
                                value={comment}
                                onChange={(e) => setComment(e.target.value)}
                                className="w-full p-4 sm:p-5 bg-slate-50 border border-slate-200 rounded-2xl outline-none focus:bg-white focus:border-indigo-500 focus:ring-4 focus:ring-indigo-500/10 transition-all resize-none text-slate-800 placeholder:text-slate-400 font-medium h-32 sm:h-40 text-sm sm:text-base leading-relaxed"
                                placeholder="Tell us what you liked or what could be better..."
                            />
                            <div className="absolute bottom-4 right-4 text-[10px] sm:text-xs font-bold text-slate-400 pointer-events-none uppercase tracking-wide">
                                {comment.length}/500
                            </div>
                        </div>
                    </div>

                    {/* Action Buttons */}
                    <div className="grid grid-cols-2 gap-4 sm:gap-6 pt-2">
                        <button
                            onClick={onClose}
                            className="w-full py-3.5 px-4 bg-white border border-slate-200 hover:bg-slate-50 text-slate-600 rounded-xl font-bold transition-all active:scale-95"
                        >
                            Cancel
                        </button>
                        <button
                            onClick={handleSubmit}
                            disabled={submitting}
                            className="w-full py-3.5 px-4 bg-indigo-600 hover:bg-indigo-700 text-white rounded-xl font-bold shadow-lg shadow-indigo-600/20 hover:shadow-indigo-600/30 transition-all disabled:opacity-70 disabled:cursor-not-allowed active:scale-95 flex justify-center items-center gap-2"
                        >
                            {submitting && <Loader2 className="w-5 h-5 animate-spin text-white" />}
                            Submit Review
                        </button>
                    </div>
                </div>
            ) : (
                // VIEW MODE
                <div className="flex flex-col max-h-[80vh]">
                    {/* Professional Header */}
                    <div className="px-8 py-6 border-b border-slate-100 flex items-center justify-between bg-white shrink-0 sticky top-0 z-20">
                        <div>
                            <h3 className="text-xl font-black text-slate-900 tracking-tight font-serif">Student Reviews</h3>
                            <p className="text-sm font-medium text-slate-500 mt-1">
                                Feedback for <span className="text-indigo-600 font-bold">{targetName || 'Mentor'}</span>
                            </p>
                        </div>
                        <button
                            onClick={onClose}
                            className="w-8 h-8 flex items-center justify-center text-slate-400 hover:text-slate-600 hover:bg-slate-100 rounded-full transition-all"
                        >
                            <X className="w-5 h-5" />
                        </button>
                    </div>

                    {/* Scrollable Content */}
                    <div className="overflow-y-auto p-6 bg-slate-50/50 custom-scrollbar">
                        {loading ? (
                            <div className="flex flex-col items-center justify-center py-12 space-y-4">
                                <div className="w-12 h-12 rounded-full flex items-center justify-center bg-indigo-50 text-indigo-600">
                                    <Loader2 className="w-6 h-6 animate-spin" />
                                </div>
                                <p className="text-slate-400 font-bold tracking-wide text-sm">LOADING REVIEWS...</p>
                            </div>
                        ) : reviews.length === 0 ? (
                            <div className="flex flex-col items-center justify-center py-16 text-center">
                                <div className="w-20 h-20 bg-white rounded-3xl shadow-sm border border-slate-100 flex items-center justify-center mb-6 rotate-3">
                                    <MessageSquare className="w-8 h-8 text-slate-300" />
                                </div>
                                <h4 className="text-slate-900 font-bold text-xl mb-2">No reviews yet</h4>
                                <p className="text-slate-500 font-medium text-sm max-w-[200px]">Be the first to share your experience with this mentor!</p>
                            </div>
                        ) : (
                            <div className="space-y-4">
                                {reviews.map((review, index) => (
                                    <div
                                        key={review.id}
                                        className="p-5 bg-white rounded-2xl border border-slate-100 shadow-sm hover:shadow-md transition-all duration-300 relative group"
                                        style={{ animationDelay: `${index * 50}ms` }}
                                    >
                                        <Quote className="absolute top-5 right-5 w-8 h-8 text-slate-100 group-hover:text-indigo-50 transition-colors fill-current" />

                                        <div className="flex gap-4 relative z-10">
                                            <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-indigo-100 to-violet-100 text-indigo-600 flex items-center justify-center shrink-0 text-lg font-bold shadow-inner">
                                                {(review.studentName || 'A').charAt(0).toUpperCase()}
                                            </div>
                                            <div className="flex-1 min-w-0 pt-1">
                                                <div className="flex flex-col sm:flex-row sm:items-center justify-between mb-2 gap-1">
                                                    <h4 className="font-bold text-slate-900 text-base">
                                                        {review.studentName || 'Anonymous User'}
                                                    </h4>
                                                    <span className="text-xs font-medium text-slate-400 bg-slate-50 px-2 py-1 rounded-lg">
                                                        {format(new Date(review.createdAt), 'MMM d, yyyy')}
                                                    </span>
                                                </div>

                                                <div className="flex items-center gap-1 mb-3">
                                                    {[...Array(5)].map((_, i) => (
                                                        <Star
                                                            key={i}
                                                            className={cn(
                                                                "w-4 h-4",
                                                                i < review.rating
                                                                    ? "fill-amber-400 text-amber-400 drop-shadow-sm"
                                                                    : "fill-slate-100 text-slate-200"
                                                            )}
                                                        />
                                                    ))}
                                                </div>

                                                <p className="text-slate-600 text-sm leading-relaxed font-medium">
                                                    "{review.comment}"
                                                </p>
                                            </div>
                                        </div>
                                    </div>
                                ))}
                            </div>
                        )}
                    </div>
                </div>
            )}
        </ClayModal>
    );
};

export default ReviewModal;
