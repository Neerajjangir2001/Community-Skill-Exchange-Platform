import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { Mail, Lock, Loader2, User, Check, Eye, EyeOff } from 'lucide-react';
import toast from 'react-hot-toast';
import { ClayCard, ClayButton, ClayInput } from '../../components/ui/ClayComponents';
import { cn } from '../../utils/cn';

const Register = () => {
    const [formData, setFormData] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        isTeacher: false
    });
    const [showPassword, setShowPassword] = useState(false);
    const [showConfirmPassword, setShowConfirmPassword] = useState(false);
    const [loading, setLoading] = useState(false);
    const { register } = useAuth();
    const useNavigateRef = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();

        if (formData.password !== formData.confirmPassword) {
            toast.error('Passwords do not match');
            return;
        }

        if (formData.password.length < 6) {
            toast.error('Password must be at least 6 characters');
            return;
        }

        setLoading(true);
        try {
            await register({
                email: formData.email,
                password: formData.password,
                roles: formData.isTeacher ? ["TEACHER"] : ["STUDENT"]
            });
            toast.success('Account created successfully!');
            useNavigateRef('/create-profile');
        } catch (err) {
            console.log(err);
            const errorMessage = err.response?.data?.message || err.response?.data?.error || 'Registration failed. Please try again.';
            toast.error(typeof errorMessage === 'string' ? errorMessage : JSON.stringify(errorMessage));
        } finally {
            setLoading(false);
        }
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    return (
        <ClayCard className="p-10 w-full relative overflow-hidden">
            {/* Subtle internal decoration */}
            <div className="absolute top-0 right-0 w-32 h-32 bg-gradient-to-br from-white/40 to-transparent rounded-bl-[4rem] pointer-events-none"></div>

            <div className="mb-8 text-center relative z-10">
                <h2 className="text-3xl font-black text-slate-800 font-display mb-2">Create Account</h2>
                <p className="text-slate-500 font-medium">Join our community today</p>
            </div>

            <form onSubmit={handleSubmit} className="space-y-5 relative z-10">
                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2 ml-2">Email Address</label>
                    <ClayInput
                        type="email"
                        name="email"
                        icon={Mail}
                        value={formData.email}
                        onChange={handleChange}
                        placeholder="you@example.com"
                        required
                    />
                </div>

                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2 ml-2">Password</label>
                    <ClayInput
                        type={showPassword ? "text" : "password"}
                        name="password"
                        icon={Lock}
                        endIcon={showPassword ? EyeOff : Eye}
                        onEndIconClick={() => setShowPassword(!showPassword)}
                        value={formData.password}
                        onChange={handleChange}
                        placeholder="••••••••"
                        required
                    />
                </div>

                <div>
                    <label className="block text-xs font-black text-slate-400 uppercase tracking-wider mb-2 ml-2">Confirm Password</label>
                    <ClayInput
                        type={showConfirmPassword ? "text" : "password"}
                        name="confirmPassword"
                        icon={Lock}
                        endIcon={showConfirmPassword ? EyeOff : Eye}
                        onEndIconClick={() => setShowConfirmPassword(!showConfirmPassword)}
                        value={formData.confirmPassword}
                        onChange={handleChange}
                        placeholder="••••••••"
                        required
                    />
                </div>

                <div
                    onClick={() => setFormData({ ...formData, isTeacher: !formData.isTeacher })}
                    className={cn(
                        "cursor-pointer border border-transparent rounded-2xl p-4 flex items-center gap-4 transition-all duration-300",
                        formData.isTeacher
                            ? "bg-indigo-50 border-indigo-200 shadow-sm"
                            : "bg-slate-50 hover:bg-slate-100"
                    )}
                >
                    <div className={cn(
                        "w-6 h-6 rounded-lg flex items-center justify-center transition-all duration-300",
                        formData.isTeacher
                            ? "bg-indigo-600 text-white shadow-md shadow-indigo-600/20 scale-110"
                            : "bg-white border text-transparent border-slate-300"
                    )}>
                        <Check className="w-4 h-4" />
                    </div>
                    <div>
                        <span className={cn(
                            "block font-bold text-sm transition-colors",
                            formData.isTeacher ? "text-indigo-900" : "text-slate-700"
                        )}>Join as a Mentor</span>
                        <span className="block text-xs text-slate-500 font-medium mt-0.5">I want to share my skills and teach others</span>
                    </div>
                </div>

                <ClayButton
                    type="submit"
                    disabled={loading}
                    className="w-full py-4 text-lg bg-white border border-indigo-50 text-indigo-600 hover:text-indigo-700 shadow-[0_20px_40px_-15px_rgba(67,24,255,0.3)] hover:shadow-[0_20px_40px_-10px_rgba(67,24,255,0.4)] font-black rounded-full transition-all hover:-translate-y-1 active:scale-[0.98] tracking-wide relative overflow-hidden group mt-4"
                >
                    <div className="absolute inset-0 bg-gradient-to-r from-indigo-50 to-white opacity-0 group-hover:opacity-100 transition-opacity" />
                    <span className="relative z-10 flex items-center justify-center gap-2">
                        {loading ? <Loader2 className="w-6 h-6 animate-spin" /> : 'Create Account'}
                    </span>
                </ClayButton>
            </form>

            <div className="mt-8 text-center text-sm font-medium text-slate-500 relative z-10">
                Already have an account?{' '}
                <Link to="/login" className="text-indigo-600 font-bold hover:text-indigo-700 transition-colors">
                    Sign in
                </Link>
            </div>
        </ClayCard>
    );
};

export default Register;
