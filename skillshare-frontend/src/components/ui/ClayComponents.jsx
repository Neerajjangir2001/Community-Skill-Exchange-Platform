import React from 'react';
import { cn } from '../../utils/cn';

// --- Base Components ---

export const ClayCard = ({ children, className, hoverEffect = false, ...props }) => {
    return (
        <div
            className={cn(
                "bg-clay-card rounded-[2rem] shadow-clay-card border border-white/50 transition-all duration-300",
                hoverEffect && "hover:shadow-clay-float hover:-translate-y-1",
                className
            )}
            {...props}
        >
            {children}
        </div>
    );
};

export const ClayButton = ({ children, className, variant = 'primary', onClick, disabled, ...props }) => {
    const baseStyles = "font-display font-bold rounded-2xl transition-all duration-200 active:scale-[0.98] disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 px-6 py-3 tracking-wide";

    const variants = {
        primary: "bg-clay-bg text-indigo-600 shadow-clay-btn hover:text-indigo-700 active:shadow-clay-pressed",
        secondary: "bg-transparent text-slate-500 hover:text-indigo-600",
        icon: "p-3 bg-clay-bg text-slate-400 shadow-clay-btn hover:text-indigo-600 active:shadow-clay-pressed rounded-xl",
        danger: "bg-clay-bg text-rose-500 shadow-clay-btn hover:text-rose-600 active:shadow-clay-pressed"
    };

    return (
        <button
            onClick={onClick}
            disabled={disabled}
            className={cn(baseStyles, variants[variant], className)}
            {...props}
        >
            {children}
        </button>
    );
};

export const ClayInput = ({ className, icon: Icon, endIcon: EndIcon, onEndIconClick, ...props }) => {
    return (
        <div className="relative w-full">
            {Icon && <Icon className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />}
            <input
                className={cn(
                    "w-full bg-clay-bg shadow-clay-inner rounded-xl py-3 text-sm font-medium text-slate-700 placeholder:text-slate-400 focus:outline-none focus:ring-2 focus:ring-indigo-100 transition-all border-none",
                    Icon ? "pl-12 pr-4" : "px-4",
                    EndIcon ? "pr-12" : "",
                    className
                )}
                {...props}
            />
            {EndIcon && (
                <button
                    type="button"
                    onClick={onEndIconClick}
                    className="absolute right-4 top-1/2 -translate-y-1/2 text-slate-400 hover:text-indigo-600 transition-colors focus:outline-none"
                >
                    <EndIcon className="w-5 h-5" />
                </button>
            )}
        </div>
    );
};

export const ClayBadge = ({ children, variant = 'neutral', className }) => {
    const variants = {
        neutral: "bg-slate-100 text-slate-600",
        success: "bg-emerald-100 text-emerald-600",
        warning: "bg-amber-100 text-amber-600",
        error: "bg-rose-100 text-rose-600",
        primary: "bg-indigo-100 text-indigo-600"
    };

    return (
        <span className={cn(
            "px-3 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider",
            variants[variant],
            className
        )}>
            {children}
        </span>
    );
};

export const ClaySelect = ({ children, className, ...props }) => {
    return (
        <div className="relative w-full">
            <select
                className={cn(
                    "w-full appearance-none bg-clay-bg shadow-clay-btn rounded-xl py-3 px-5 text-sm font-bold text-slate-700 outline-none focus:ring-2 focus:ring-indigo-100 cursor-pointer border-none",
                    className
                )}
                {...props}
            >
                {children}
            </select>
            <div className="absolute right-4 top-1/2 -translate-y-1/2 pointer-events-none text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="m6 9 6 6 6-6" /></svg>
            </div>
        </div>
    );
};

// --- Complex Components ---

export const ClayModal = ({ isOpen, onClose, title, children, className }) => {
    if (!isOpen) return null;

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/20 backdrop-blur-sm animate-in fade-in duration-200">
            <div className="absolute inset-0" onClick={onClose} />
            <ClayCard className={cn("w-full max-w-md relative z-10 animate-in zoom-in-95 duration-200", className)}>
                {title && (
                    <div className="flex items-center justify-between mb-6 pb-4 border-b border-slate-100">
                        <h3 className="text-xl font-black text-slate-800 font-display">{title}</h3>
                        <button onClick={onClose} className="p-2 text-slate-400 hover:text-rose-500 hover:bg-rose-50 rounded-xl transition-colors">
                            <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M18 6 6 18" /><path d="m6 6 12 12" /></svg>
                        </button>
                    </div>
                )}
                {children}
            </ClayCard>
        </div>
    );
};
