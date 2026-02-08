import React from 'react';
import { Outlet } from 'react-router-dom';

const AuthLayout = () => {
    return (
        <div className="min-h-screen bg-gradient-to-br from-[#EEF2FF] to-[#F8FAFC] flex items-center justify-center p-4 relative overflow-hidden">
            {/* Soft decorative blobs */}
            <div className="absolute top-0 left-0 w-96 h-96 bg-indigo-100 rounded-full blur-3xl -translate-x-1/2 -translate-y-1/2 opacity-50"></div>
            <div className="absolute bottom-0 right-0 w-96 h-96 bg-purple-100 rounded-full blur-3xl translate-x-1/2 translate-y-1/2 opacity-50"></div>

            <div className="w-full max-w-md relative z-10">
                <div className="mb-10 text-center">
                    <div className="inline-flex items-center justify-center w-16 h-16 rounded-[1.5rem] bg-clay-bg shadow-clay-float mb-4 transform -rotate-6">
                        <span className="text-3xl">🎓</span>
                    </div>
                    <h1 className="text-4xl font-black text-slate-800 tracking-tight font-display mb-2 drop-shadow-sm">SkillShare</h1>
                    <p className="text-slate-500 font-bold text-lg">Connect, Learn, Grow</p>
                </div>
                <Outlet />
            </div>
        </div>
    );
};

export default AuthLayout;
