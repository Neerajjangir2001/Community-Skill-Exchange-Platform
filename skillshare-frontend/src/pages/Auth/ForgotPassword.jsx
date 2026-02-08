import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { authService } from '../../api/auth';
import { Mail, Loader2, ArrowLeft } from 'lucide-react';
import toast from 'react-hot-toast';
import { ClayCard, ClayButton, ClayInput } from '../../components/ui/ClayComponents';

const ForgotPassword = () => {
    const [email, setEmail] = useState('');
    const [loading, setLoading] = useState(false);
    const [submitted, setSubmitted] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);
        try {
            await authService.forgotPassword(email);
            setSubmitted(true);
            toast.success('Reset link sent!');
        } catch (error) {
            console.error(error);
            toast.error('Failed to send reset link. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    if (submitted) {
        return (
            <ClayCard className="p-10 w-full text-center relative overflow-hidden max-w-md mx-auto mt-20">
                <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-emerald-100 to-transparent rounded-bl-[4rem] pointer-events-none"></div>
                <div className="mb-6 mx-auto w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center">
                    <Mail className="w-8 h-8" />
                </div>
                <h2 className="text-2xl font-black text-slate-800 font-display mb-2">Check Your Email</h2>
                <p className="text-slate-500 font-medium mb-8">
                    We've sent a password reset link to <br />
                    <span className="font-bold text-slate-700">{email}</span>
                </p>
                <Link to="/login">
                    <ClayButton className="w-full py-3">Return to Login</ClayButton>
                </Link>
            </ClayCard>
        );
    }

    return (
        <ClayCard className="p-10 w-full relative overflow-hidden max-w-md mx-auto mt-20">
            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-indigo-100 to-transparent rounded-bl-[4rem] pointer-events-none"></div>

            <div className="mb-8">
                <Link to="/login" className="inline-flex items-center text-slate-400 hover:text-indigo-600 mb-6 transition-colors font-bold text-xs uppercase tracking-wider gap-1">
                    <ArrowLeft className="w-4 h-4" /> Back to Login
                </Link>
                <h2 className="text-3xl font-black text-slate-800 font-display mb-2">Forgot Password?</h2>
                <p className="text-slate-500 font-medium">Enter your email and we'll send you instructions to reset your password.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-3 ml-2">Email Address</label>
                    <ClayInput
                        type="email"
                        icon={Mail}
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="you@example.com"
                        required
                    />
                </div>

                <ClayButton
                    type="submit"
                    disabled={loading}
                    className="w-full py-4 text-lg shadow-indigo-200"
                >
                    {loading ? <Loader2 className="w-6 h-6 animate-spin" /> : 'Send Reset Link'}
                </ClayButton>
            </form>
        </ClayCard>
    );
};

export default ForgotPassword;
