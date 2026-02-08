import React, { useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { authService } from '../../api/auth';
import { Lock, Loader2, Check } from 'lucide-react';
import toast from 'react-hot-toast';
import { ClayCard, ClayButton, ClayInput } from '../../components/ui/ClayComponents';

const ResetPassword = () => {
    const [searchParams] = useSearchParams();
    const token = searchParams.get('token');
    const navigate = useNavigate();

    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [loading, setLoading] = useState(false);
    const [success, setSuccess] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (!token) {
            toast.error("Invalid link. Please request a new one.");
            return;
        }

        if (password !== confirmPassword) {
            toast.error("Passwords do not match");
            return;
        }

        setLoading(true);
        try {
            await authService.resetPassword(token, password);
            setSuccess(true);
            toast.success('Password reset successfully!');
            setTimeout(() => navigate('/login'), 2000);
        } catch (error) {
            console.error(error);
            toast.error(error.response?.data || 'Failed to reset password');
        } finally {
            setLoading(false);
        }
    };

    if (success) {
        return (
            <ClayCard className="p-10 w-full text-center relative overflow-hidden max-w-md mx-auto mt-20">
                <div className="absolute top-0 right-0 w-32 h-32 bg-emerald-100 rounded-bl-[4rem] pointer-events-none"></div>
                <div className="mb-6 mx-auto w-16 h-16 bg-emerald-100 text-emerald-600 rounded-full flex items-center justify-center animate-bounce">
                    <Check className="w-8 h-8" />
                </div>
                <h2 className="text-2xl font-black text-slate-800 font-display mb-2">Password Reset!</h2>
                <p className="text-slate-500 font-medium mb-8">
                    Your password has been securely updated. Redirecting you to login...
                </p>
                <Link to="/login">
                    <ClayButton className="w-full py-3">Login Now</ClayButton>
                </Link>
            </ClayCard>
        );
    }

    if (!token) {
        return (
            <ClayCard className="p-10 w-full text-center max-w-md mx-auto mt-20">
                <h2 className="text-xl font-black text-slate-800 mb-2">Invalid Link</h2>
                <p className="text-slate-500 mb-6">This password reset link is invalid or missing.</p>
                <Link to="/forgot-password">
                    <ClayButton>Request New Link</ClayButton>
                </Link>
            </ClayCard>
        );
    }

    return (
        <ClayCard className="p-10 w-full relative overflow-hidden max-w-md mx-auto mt-20">
            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-indigo-100 to-transparent rounded-bl-[4rem] pointer-events-none"></div>

            <div className="mb-8 relative z-10">
                <h2 className="text-3xl font-black text-slate-800 font-display mb-2">New Password</h2>
                <p className="text-slate-500 font-medium">Create a strong password for your account.</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6 relative z-10">
                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-3 ml-2">New Password</label>
                    <ClayInput
                        type="password"
                        icon={Lock}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                        minLength={6}
                    />
                </div>
                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-3 ml-2">Confirm Password</label>
                    <ClayInput
                        type="password"
                        icon={Lock}
                        value={confirmPassword}
                        onChange={(e) => setConfirmPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                        minLength={6}
                    />
                </div>

                <ClayButton
                    type="submit"
                    disabled={loading}
                    className="w-full py-4 text-lg shadow-indigo-200"
                >
                    {loading ? <Loader2 className="w-6 h-6 animate-spin" /> : 'Reset Password'}
                </ClayButton>
            </form>
        </ClayCard>
    );
};

export default ResetPassword;
