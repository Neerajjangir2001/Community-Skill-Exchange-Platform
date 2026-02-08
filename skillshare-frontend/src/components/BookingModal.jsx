import React, { useState } from 'react';
import { bookingService } from '../api/bookings';
import { X, Loader2, Calendar, Clock, DollarSign, Award } from 'lucide-react';
import toast from 'react-hot-toast';

const BookingModal = ({ skill, onClose, onSuccess }) => {
    const [formData, setFormData] = useState({
        startTime: '',
        endTime: ''
    });
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Basic validation
        const start = new Date(formData.startTime);
        const end = new Date(formData.endTime);

        if (start >= end) {
            toast.error('End time must be after start time');
            return;
        }

        setLoading(true);
        try {
            // Backend expects: "2023-10-27T10:00:00+00:00"
            // Date.toISOString() returns Z at the end. We might need to replace Z with +00:00 if backend is picky, 
            // but standard ISO usually works. The previous code manually replaced Z. I'll stick to that to be safe.
            const startTimeISO = start.toISOString().replace('Z', '+00:00');
            const endTimeISO = end.toISOString().replace('Z', '+00:00');

            await bookingService.createBooking({
                skillId: skill.id,
                startTime: startTimeISO,
                endTime: endTimeISO
            });

            toast.success('Session booked successfully!');
            onSuccess?.();
            onClose();
        } catch (error) {
            const errorMessage = error.response?.data?.message || 'Failed to book session';
            toast.error(typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
            console.error(error);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-50 flex items-center justify-center p-4">
            <div className="bg-white rounded-2xl w-full max-w-lg shadow-2xl animate-in fade-in zoom-in duration-200">
                <div className="flex items-center justify-between p-6 border-b border-slate-100">
                    <h2 className="text-xl font-bold text-slate-900">Book Session</h2>
                    <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <div className="p-6">
                    {/* Skill Summary */}
                    <div className="bg-blue-50/50 rounded-xl p-4 mb-6 border border-blue-100">
                        <h3 className="font-semibold text-slate-900">{skill.title}</h3>
                        <div className="flex flex-wrap gap-4 mt-3 text-sm text-slate-600">
                            <div className="flex items-center gap-1.5">
                                <span className="text-blue-600 font-bold">₹</span>
                                <span>{skill.pricePerHour > 0 ? `${skill.pricePerHour}/hr` : 'Free'}</span>
                            </div>
                            <div className="flex items-center gap-1.5">
                                <Award className="w-4 h-4 text-blue-600" />
                                <span>{skill.level}</span>
                            </div>
                        </div>
                    </div>

                    <form onSubmit={handleSubmit} className="space-y-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Start Time</label>
                            <div className="relative">
                                <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5 pointer-events-none" />
                                <input
                                    type="datetime-local"
                                    required
                                    value={formData.startTime}
                                    onChange={(e) => setFormData({ ...formData, startTime: e.target.value })}
                                    className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                                />
                            </div>
                        </div>

                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">End Time</label>
                            <div className="relative">
                                <Clock className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5 pointer-events-none" />
                                <input
                                    type="datetime-local"
                                    required
                                    value={formData.endTime}
                                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value })}
                                    className="w-full pl-10 pr-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                                />
                            </div>
                        </div>

                        <div className="pt-4 flex justify-end gap-3">
                            <button
                                type="button"
                                onClick={onClose}
                                className="px-4 py-2 text-slate-700 hover:bg-slate-100 rounded-lg transition-colors font-medium"
                            >
                                Cancel
                            </button>
                            <button
                                type="submit"
                                disabled={loading}
                                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors font-medium flex items-center gap-2"
                            >
                                {loading && <Loader2 className="w-4 h-4 animate-spin" />}
                                {loading ? 'Booking...' : 'Confirm Booking'}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default BookingModal;
