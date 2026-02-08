import React from 'react';
import { useNavigate } from 'react-router-dom';
import { Search, BookOpen, Users, Calendar, ArrowRight, Star, Heart, CheckCircle } from 'lucide-react';
import { ClayButton, ClayCard, ClayInput } from '../components/ui/ClayComponents';

const Landing = () => {
    const navigate = useNavigate();

    const popularSkills = [
        { icon: "🎸", name: "Music", count: "120+ Teachers" },
        { icon: "💻", name: "Technology", count: "300+ Teachers" },
        { icon: "🗣️", name: "Languages", count: "250+ Teachers" },
        { icon: "🎨", name: "Arts & Design", count: "180+ Teachers" },
        { icon: "🏋️", name: "Fitness", count: "90+ Teachers" },
        { icon: "🍳", name: "Cooking", count: "70+ Teachers" },
    ];

    const featuredTeachers = [
        {
            name: "Sarah Jenkins",
            skill: "Advanced Guitar",
            rating: "4.9",
            reviews: "128",
            image: "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&q=80&w=200",
            price: "45"
        },
        {
            name: "David Chen",
            skill: "React Development",
            rating: "5.0",
            reviews: "85",
            image: "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&q=80&w=200",
            price: "60"
        },
        {
            name: "Elena Rodriguez",
            skill: "Spanish Conversation",
            rating: "4.8",
            reviews: "210",
            image: "https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&q=80&w=200",
            price: "30"
        }
    ];

    return (
        <div className="min-h-screen bg-clay-bg text-clay-text font-sans">
            {/* Navbar */}
            <nav className="sticky top-0 z-50 bg-clay-bg/80 backdrop-blur-md border-b border-white/50">
                <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
                    <div className="flex items-center gap-2">
                        <div className="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center text-white font-bold text-xl shadow-lg shadow-primary-600/20">
                            S
                        </div>
                        <span className="text-xl font-bold text-gray-900 tracking-tight">SkillShare</span>
                    </div>
                    <div className="hidden md:flex items-center gap-8 font-medium text-gray-600">
                        <a href="#how-it-works" className="hover:text-primary-600 transition-colors">How it Works</a>
                        <a href="#skills" className="hover:text-primary-600 transition-colors">Explore Skills</a>
                        <a href="#teachers" className="hover:text-primary-600 transition-colors">Teachers</a>
                    </div>
                    <div className="flex items-center gap-4">
                        <button onClick={() => navigate('/login')} className="font-semibold text-gray-600 hover:text-primary-600 px-4 py-2 transition-colors">
                            Sign In
                        </button>
                        <ClayButton onClick={() => navigate('/register')} className="shadow-lg shadow-primary-600/20">
                            Get Started
                        </ClayButton>
                    </div>
                </div>
            </nav>

            {/* Hero Section */}
            <header className="relative pt-20 pb-32 overflow-hidden">
                <div className="absolute inset-0 bg-gradient-to-b from-primary-50/50 to-transparent pointer-events-none" />
                <div className="max-w-7xl mx-auto px-6 relative z-10 grid lg:grid-cols-2 gap-16 items-center">
                    <div className="space-y-8 animate-in slide-in-from-left duration-700">
                        <h1 className="text-5xl lg:text-7xl font-extrabold text-gray-900 tracking-tight leading-[1.1]">
                            Learn Anything.<br />
                            <span className="text-transparent bg-clip-text bg-gradient-to-r from-primary-600 to-indigo-600">
                                Teach Everything.
                            </span>
                        </h1>
                        <p className="text-xl text-gray-600 leading-relaxed max-w-lg">
                            Connect with local experts or share your passion with the world. The community-driven platform for real skill exchange.
                        </p>

                        <div className="bg-white p-2 rounded-2xl shadow-clay-card border border-gray-100 flex items-center gap-2 max-w-md">
                            <Search className="w-6 h-6 text-gray-400 ml-3" />
                            <input
                                type="text"
                                placeholder="What do you want to learn today?"
                                className="flex-1 bg-transparent border-none outline-none text-gray-900 placeholder:text-gray-400 h-12"
                            />
                            <ClayButton onClick={() => navigate('/login')} className="rounded-xl px-6">
                                Find Teacher
                            </ClayButton>
                        </div>

                        <div className="flex items-center gap-6 text-sm font-semibold text-gray-500">
                            <div className="flex items-center gap-2">
                                <CheckCircle className="w-5 h-5 text-emerald-500" />
                                <span>Verified Teachers</span>
                            </div>
                            <div className="flex items-center gap-2">
                                <CheckCircle className="w-5 h-5 text-emerald-500" />
                                <span>Secure Payments</span>
                            </div>
                        </div>
                    </div>

                    <div className="relative animate-in slide-in-from-right duration-700 delay-200 hidden lg:block">
                        <div className="absolute -inset-4 bg-gradient-to-tr from-primary-200 to-transparent rounded-[3rem] opacity-30 blur-2xl" />
                        <img
                            src="https://images.unsplash.com/photo-1522202176988-66273c2fd55f?auto=format&fit=crop&q=80&w=800"
                            alt="Students learning"
                            className="relative rounded-[2.5rem] shadow-2xl border-4 border-white object-cover h-[600px] w-full"
                        />

                        {/* Floating Cards */}
                        <ClayCard className="absolute -left-8 top-20 !p-4 flex items-center gap-4 animate-bounce-slow">
                            <div className="w-12 h-12 bg-amber-100 rounded-full flex items-center justify-center text-2xl">🎸</div>
                            <div>
                                <p className="font-bold text-gray-900">Guitar Lesson</p>
                                <p className="text-xs text-gray-500">Today at 4:00 PM</p>
                            </div>
                        </ClayCard>

                        <ClayCard className="absolute -right-8 bottom-32 !p-4 flex items-center gap-4 animate-bounce-slow delay-700">
                            <div className="flex -space-x-3">
                                {[1, 2, 3].map(i => (
                                    <div key={i} className="w-10 h-10 rounded-full border-2 border-white bg-gray-200" />
                                ))}
                            </div>
                            <div>
                                <p className="font-bold text-gray-900">10k+ Learners</p>
                                <p className="text-xs text-emerald-600 font-bold">Joined this week</p>
                            </div>
                        </ClayCard>
                    </div>
                </div>
            </header>

            {/* How It Works */}
            <section id="how-it-works" className="py-24 bg-white/50">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="text-center mb-16">
                        <h2 className="text-3xl font-bold text-gray-900 mb-4">How It Works</h2>
                        <p className="text-gray-600 max-w-2xl mx-auto">Start your learning journey in three simple steps. No complicated processes, just pure knowledge exchange.</p>
                    </div>

                    <div className="grid md:grid-cols-3 gap-12">
                        {[
                            { icon: BookOpen, title: "Browse Skills", desc: "Explore thousands of skills from music to coding, taught by passionate experts." },
                            { icon: Calendar, title: "Book a Session", desc: "Choose a time that works for you. In-person or online options available." },
                            { icon: Users, title: "Start Learning", desc: "Connect with your teacher and start mastering your new skill immediately." }
                        ].map((item, idx) => (
                            <div key={idx} className="relative group text-center">
                                <div className="w-20 h-20 mx-auto bg-primary-50 rounded-3xl flex items-center justify-center text-primary-600 mb-6 group-hover:scale-110 transition-transform duration-300 shadow-sm border border-primary-100">
                                    <item.icon className="w-10 h-10" />
                                </div>
                                <h3 className="text-xl font-bold text-gray-900 mb-3">{item.title}</h3>
                                <p className="text-gray-500 leading-relaxed">{item.desc}</p>
                            </div>
                        ))}
                    </div>
                </div>
            </section>

            {/* Popular Skills */}
            <section id="skills" className="py-24">
                <div className="max-w-7xl mx-auto px-6">
                    <div className="flex items-center justify-between mb-12">
                        <h2 className="text-3xl font-bold text-gray-900">Popular Skills</h2>
                        <button onClick={() => navigate('/login')} className="text-primary-600 font-bold flex items-center gap-2 hover:gap-3 transition-all">
                            View all categories <ArrowRight className="w-4 h-4" />
                        </button>
                    </div>

                    <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-6 gap-6">
                        {popularSkills.map((skill, idx) => (
                            <ClayCard key={idx} hoverEffect className="!p-6 text-center cursor-pointer group border-0 shadow-sm hover:shadow-md bg-white">
                                <div className="text-4xl mb-4 group-hover:scale-110 transition-transform duration-300">{skill.icon}</div>
                                <h3 className="font-bold text-gray-900 mb-1">{skill.name}</h3>
                                <p className="text-xs text-gray-500 font-medium">{skill.count}</p>
                            </ClayCard>
                        ))}
                    </div>
                </div>
            </section>

            {/* Featured Teachers */}
            <section id="teachers" className="py-24 bg-primary-50/50">
                <div className="max-w-7xl mx-auto px-6">
                    <h2 className="text-3xl font-bold text-gray-900 mb-12 text-center">Meet Top Rated Teachers</h2>

                    <div className="grid md:grid-cols-3 gap-8">
                        {featuredTeachers.map((teacher, idx) => (
                            <ClayCard key={idx} hoverEffect className="!p-0 overflow-hidden border-0 shadow-sm hover:shadow-xl bg-white">
                                <div className="h-64 overflow-hidden relative group">
                                    <img src={teacher.image} alt={teacher.name} className="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105" />
                                    <div className="absolute top-4 right-4 bg-white/95 backdrop-blur-sm px-3 py-1 rounded-full text-xs font-bold text-primary-600 shadow-sm">
                                        ${teacher.price}/hr
                                    </div>
                                </div>
                                <div className="p-6">
                                    <div className="flex items-center justify-between mb-2">
                                        <span className="text-xs font-bold uppercase tracking-wider text-primary-600">{teacher.skill}</span>
                                        <div className="flex items-center gap-1 text-amber-500 text-sm font-bold">
                                            <Star className="w-4 h-4 fill-current" /> {teacher.rating} <span className="text-gray-400 font-normal">({teacher.reviews})</span>
                                        </div>
                                    </div>
                                    <h3 className="text-xl font-bold text-gray-900 mb-4">{teacher.name}</h3>
                                    <ClayButton onClick={() => navigate('/login')} variant="secondary" className="w-full">
                                        View Profile
                                    </ClayButton>
                                </div>
                            </ClayCard>
                        ))}
                    </div>
                </div>
            </section>

            {/* CTA */}
            <section className="py-24 relative overflow-hidden">
                <div className="max-w-5xl mx-auto px-6 text-center relative z-10">
                    <h2 className="text-4xl lg:text-5xl font-extrabold text-gray-900 mb-6 tracking-tight">
                        Ready to share your passion?
                    </h2>
                    <p className="text-xl text-gray-600 mb-10 max-w-2xl mx-auto">
                        Join thousands of teachers who are earning money by sharing what they love. Sign up today and create your first listing in minutes.
                    </p>
                    <div className="flex flex-col sm:flex-row items-center justify-center gap-4">
                        <ClayButton onClick={() => navigate('/register')} className="px-10 py-4 text-lg shadow-xl shadow-primary-600/20">
                            Become a Teacher
                        </ClayButton>
                        <button onClick={() => navigate('/login')} className="px-10 py-4 text-lg font-bold text-gray-600 hover:text-primary-600 transition-colors">
                            Find a Teacher
                        </button>
                    </div>
                </div>

                {/* Background Decor */}
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-[800px] h-[800px] bg-gradient-to-tr from-primary-100 to-indigo-50 rounded-full blur-3xl opacity-50 -z-10" />
            </section>

            {/* Footer */}
            <footer className="bg-white border-t border-gray-100 py-16">
                <div className="max-w-7xl mx-auto px-6 grid md:grid-cols-4 gap-12">
                    <div>
                        <div className="flex items-center gap-2 mb-6">
                            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center text-white font-bold">S</div>
                            <span className="text-lg font-bold text-gray-900">SkillShare</span>
                        </div>
                        <p className="text-gray-500 text-sm leading-relaxed">
                            Empowering the world to learn and teach. Connect, share, and grow with your local community.
                        </p>
                    </div>
                    <div>
                        <h4 className="font-bold text-gray-900 mb-6">Platform</h4>
                        <ul className="space-y-4 text-sm text-gray-500">
                            <li><a href="#" className="hover:text-primary-600">Browse Skills</a></li>
                            <li><a href="#" className="hover:text-primary-600">How it Works</a></li>
                            <li><a href="#" className="hover:text-primary-600">Pricing</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold text-gray-900 mb-6">Company</h4>
                        <ul className="space-y-4 text-sm text-gray-500">
                            <li><a href="#" className="hover:text-primary-600">About Us</a></li>
                            <li><a href="#" className="hover:text-primary-600">Careers</a></li>
                            <li><a href="#" className="hover:text-primary-600">Contact</a></li>
                        </ul>
                    </div>
                    <div>
                        <h4 className="font-bold text-gray-900 mb-6">Stay Updated</h4>
                        <div className="flex gap-2">
                            <input
                                type="email"
                                placeholder="Enter your email"
                                className="bg-gray-50 border border-gray-200 rounded-lg px-4 py-2 text-sm flex-1 focus:outline-none focus:ring-2 focus:ring-primary-100"
                            />
                            <button className="bg-primary-600 text-white rounded-lg px-4 py-2 text-sm font-bold hover:bg-primary-700 transition-colors">
                                Subscribe
                            </button>
                        </div>
                    </div>
                </div>
                <div className="max-w-7xl mx-auto px-6 mt-16 pt-8 border-t border-gray-100 text-center text-sm text-gray-400">
                    © 2026 SkillShare Platform. All rights reserved.
                </div>
            </footer>
        </div>
    );
};

export default Landing;
