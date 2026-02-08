import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Mail, Lock, Loader2, Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { ClayCard, ClayButton, ClayInput } from '../../components/ui/ClayComponents';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [showPassword, setShowPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const { login } = useAuth();
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            await login(email, password);
            toast.success('Welcome back!');
            navigate('/');
        } catch (err) {
            console.error(err);
            const errorMessage = err.response?.data?.message || err.response?.data?.error || 'Invalid email or password';
            toast.error(typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
        } finally {
            setLoading(false);
        }
    };

    return (
        <ClayCard className="p-10 w-full relative overflow-hidden">
            {/* Subtle internal decoration */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-white/40 to-transparent rounded-bl-[4rem] pointer-events-none"></div>

            <div className="mb-8 text-center relative z-10">
                <h2 className="text-3xl font-black text-slate-800 font-display mb-2">Welcome Back</h2>
                <p className="text-slate-500 font-medium">Please sign in to continue</p>
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

                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-3 ml-2">Password</label>
                    <ClayInput
                        type={showPassword ? "text" : "password"}
                        icon={Lock}
                        endIcon={showPassword ? EyeOff : Eye}
                        onEndIconClick={() => setShowPassword(!showPassword)}
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                        required
                    />
                </div>

                <div className="flex justify-end mb-2">
                    <Link to="/forgot-password" className="text-xs font-bold text-indigo-600 hover:text-indigo-800 transition-colors">
                        Forgot Password?
                    </Link>
                </div>

                <ClayButton
                    type="submit"
                    disabled={loading}
                    className="w-full py-4 text-lg bg-white border border-indigo-50 text-indigo-600 hover:text-indigo-700 shadow-[0_20px_40px_-15px_rgba(67,24,255,0.3)] hover:shadow-[0_20px_40px_-10px_rgba(67,24,255,0.4)] font-black rounded-full transition-all hover:-translate-y-1 active:scale-[0.98] tracking-wide relative overflow-hidden group"
                >
                    <div className="absolute inset-0 bg-gradient-to-r from-indigo-50 to-white opacity-0 group-hover:opacity-100 transition-opacity" />
                    <span className="relative z-10 flex items-center justify-center gap-2">
                        {loading ? <Loader2 className="w-6 h-6 animate-spin" /> : 'Log In'}
                    </span>
                </ClayButton>
            </form>

            <div className="mt-10 text-center relative z-10">
                <Link to="/register">
                    <ClayButton variant="secondary" className="w-full py-3 text-sm">
                        Don't have an account? Sign Up
                    </ClayButton>
                </Link>
            </div>
        </ClayCard>
    );
};

export default Login;
