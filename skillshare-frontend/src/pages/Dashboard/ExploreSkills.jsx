import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { skillService } from '../../api/skills';
import BookingModal from '../../components/BookingModal';
import CreateSkillModal from '../../components/CreateSkillModal';
import { Search, SlidersHorizontal, Plus, Star, DollarSign, Award, ArrowRight, TrendingUp } from 'lucide-react';
import { cn } from '../../utils/cn';
import { useSearchParams } from 'react-router-dom';

import ReviewModal from '../../components/ReviewModal';

// Deterministic mock rating based on ID
const getMockRating = (id) => {
    if (!id) return { rating: "4.5", count: 10 };
    let hash = 0;
    for (let i = 0; i < id.length; i++) {
        hash = id.charCodeAt(i) + ((hash << 5) - hash);
    }
    const rating = (Math.abs(hash % 15) / 10) + 3.5; // Range 3.5 to 5.0
    const count = Math.abs(hash % 200) + 5;
    return { rating: rating.toFixed(1), count };
};

// Extracted Card for reuse
const SkillCard = ({ skill, onClick, onReviewClick }) => {
    const { rating, count } = getMockRating(skill.id);
    const isTopRated = parseFloat(rating) >= 4.8;
    const isPopular = parseFloat(rating) >= 4.5;
    return (
        <div className="bg-white rounded-[2rem] p-7 shadow-[8px_8px_24px_rgba(0,0,0,0.04),-8px_-8px_24px_rgba(255,255,255,1)] border border-white hover:shadow-[12px_12px_32px_rgba(0,0,0,0.06),-12px_-12px_32px_rgba(255,255,255,1)] hover:-translate-y-1 transition-all duration-300 flex flex-col group relative z-10">
            {/* Header: Level & Price */}
            <div className="flex items-center justify-between mb-6">
                <span className={cn(
                    "px-3 py-1 rounded-xl text-[10px] font-bold uppercase tracking-wider border shadow-sm",
                    skill.level === 'BEGINNER' ? 'bg-emerald-50 text-emerald-700 border-emerald-100' :
                        skill.level === 'INTERMEDIATE' ? 'bg-rose-50 text-rose-700 border-rose-100' :
                            'bg-violet-50 text-violet-700 border-violet-100'
                )}>
                    {skill.level || 'Beginner'}
                </span>

                {/* Price Display */}
                <div className="text-right flex items-baseline gap-0.5">
                    <span className="text-2xl font-black text-slate-900">
                        ₹{skill.pricePerHour}
                    </span>
                    <span className="text-sm text-slate-400 font-bold">/hr</span>
                </div>
            </div>

            {/* Title */}
            <h3 className="text-2xl font-serif font-bold text-slate-900 mb-6 leading-tight group-hover:text-indigo-700 transition-colors line-clamp-2">
                {skill.title}
            </h3>

            {/* Mentor Info + Rating */}
            <div className="flex items-center gap-3 mb-8 pb-6 border-b border-slate-50 border-dashed">
                <div className="flex items-center gap-3">
                    <div className="w-10 h-10 rounded-2xl bg-indigo-50 flex items-center justify-center text-sm font-bold text-indigo-700 shadow-inner shrink-0">
                        {(skill.providerName || 'U').charAt(0).toUpperCase()}
                    </div>
                    <div className="flex flex-col">
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-0.5">Mentor</span>
                        <span className="text-sm font-bold text-slate-700 leading-none truncate max-w-[120px]">
                            {skill.providerName || 'Unknown'}
                        </span>
                    </div>
                </div>

                <div className="flex-grow"></div>

                {/* Rating Badge */}
                <div
                    className="flex flex-col items-end cursor-pointer group/rating"
                    onClick={(e) => {
                        e.stopPropagation();
                        onReviewClick(skill);
                    }}
                >
                    <div className="flex items-center gap-1.5">
                        <Star className="w-4 h-4 text-amber-400 fill-current" />
                        <span className="text-sm font-black text-slate-900">{rating}</span>
                    </div>
                    <span className="text-[10px] text-slate-400 hover:text-indigo-600 transition-colors font-medium">{count} reviews</span>
                </div>
            </div>

            {/* Description - Removed to match clean card look, or keep trimmed? Screenshot doesn't clearly show desc, but standard cards usually have it. Let's keep it but very subtle or remove if cluttering. Screenshot has tags. Let's prioritize Tags. */}
            <p className="text-slate-500 text-sm leading-relaxed line-clamp-2 mb-6 h-[2.5rem] hidden">
                {skill.description}
            </p>

            {/* Description is hidden above as per design preference inferred from clean screenshot, focusing on Title/Tags/Price */}
            <div className="text-slate-500 text-sm leading-relaxed mb-6 line-clamp-2">{skill.description}</div>


            {/* Tags area */}
            <div className="flex flex-wrap gap-2 mb-8 mt-auto">
                {isTopRated && (
                    <span className="px-3 py-1 rounded-xl text-[10px] font-bold uppercase bg-amber-50 text-amber-700 flex items-center gap-1 border border-amber-100 shadow-sm">
                        <Award className="w-3 h-3" />
                        Top Rated
                    </span>
                )}
                {skill.tags && skill.tags.slice(0, 2).map((tag, i) => (
                    <span key={i} className="text-[10px] bg-slate-50 text-slate-500 px-3 py-1 rounded-xl font-bold border border-slate-100 shadow-sm">
                        #{tag}
                    </span>
                ))}
            </div>

            {/* Action Button - Outlined & Wide */}
            <button
                onClick={onClick}
                className="w-full bg-white hover:bg-indigo-50 text-indigo-900 font-bold py-3.5 rounded-2xl border-2 border-slate-100 hover:border-indigo-200 hover:text-indigo-700 transition-all flex items-center justify-center gap-2 group/btn shadow-sm"
            >
                <span>Book Session</span>
                <ArrowRight className="w-4 h-4 group-hover/btn:translate-x-1 transition-transform" />
            </button>
        </div>
    );
};

// Skeleton Loader for Skill Card
const SkeletonCard = () => (
    <div className="bg-white rounded-2xl p-6 shadow-sm border border-slate-200 flex flex-col h-full animate-pulse relative z-0">
        {/* Header: Level & Price */}
        <div className="flex items-center justify-between mb-4">
            <div className="h-6 w-20 bg-slate-100 rounded-lg"></div>
            <div className="h-6 w-16 bg-slate-100 rounded-lg"></div>
        </div>

        {/* Title */}
        <div className="space-y-2 mb-6">
            <div className="h-7 w-3/4 bg-slate-100 rounded-md"></div>
            <div className="h-7 w-1/2 bg-slate-100 rounded-md"></div>
        </div>

        {/* Mentor Info */}
        <div className="flex items-center gap-3 mb-6 pb-4 border-b border-slate-100">
            <div className="w-8 h-8 rounded-full bg-slate-100 shrink-0"></div>
            <div className="flex flex-col gap-1.5">
                <div className="h-3 w-10 bg-slate-100 rounded"></div>
                <div className="h-4 w-24 bg-slate-100 rounded"></div>
            </div>
            <div className="flex-grow"></div>
            <div className="h-8 w-12 bg-slate-100 rounded-lg"></div>
        </div>

        {/* Tags area */}
        <div className="flex flex-wrap gap-2 mb-6 mt-auto">
            <div className="h-5 w-16 bg-slate-100 rounded-md"></div>
            <div className="h-5 w-20 bg-slate-100 rounded-md"></div>
        </div>

        {/* Action Button */}
        <div className="h-12 w-full bg-slate-100 rounded-xl"></div>
    </div>
);

const ExploreSkills = () => {
    const [searchParams, setSearchParams] = useSearchParams();
    const [skills, setSkills] = useState([]);
    const [recommendedSkills, setRecommendedSkills] = useState([]); // Fallback skills
    const [loading, setLoading] = useState(true);
    const [searchQuery, setSearchQuery] = useState(searchParams.get('q') || '');
    const [selectedSkill, setSelectedSkill] = useState(null);
    const [showCreateModal, setShowCreateModal] = useState(false);

    // Review Modal State
    const [reviewModalConfig, setReviewModalConfig] = useState(null);

    // Sync URL when search query changes
    useEffect(() => {
        const params = {};
        if (searchQuery) params.q = searchQuery;
        setSearchParams(params, { replace: true });
    }, [searchQuery, setSearchParams]);

    // Sync external URL changes (e.g. from Top Bar) to local state
    useEffect(() => {
        const queryFromUrl = searchParams.get('q') || '';
        if (queryFromUrl !== searchQuery) {
            setSearchQuery(queryFromUrl);
        }
    }, [searchParams]);

    useEffect(() => {
        // Initial load for recommendations
        skillService.getAllSkills().then(data => {
            const all = Array.isArray(data) ? data : (data.content || []);
            setRecommendedSkills(all);
            if (!searchQuery) setSkills(all);
        }).catch(err => console.error(err));
    }, []);

    useEffect(() => {
        const timer = setTimeout(() => {
            fetchSkills(searchQuery);
        }, 500);
        return () => clearTimeout(timer);
    }, [searchQuery]);

    const fetchSkills = async (query = '') => {
        setLoading(true);
        try {
            let data;
            if (query.trim()) {
                data = await skillService.searchSkills(query);
            } else {
                // If query is empty, use the cached recommended (all) skills if available?
                // Or just fetch again. fetch is safer.
                data = await skillService.getAllSkills();
            }
            setSkills(Array.isArray(data) ? data : (data.content || []));
        } catch (error) {
            console.error('Failed to fetch skills', error);
        } finally {
            setLoading(false);
        }
    };

    const { user } = useAuth();

    const handleReviewClick = (skill) => {
        setReviewModalConfig({
            isOpen: true,
            teacherId: skill.userId, // Assuming skill has userId of provider
            targetName: skill.providerName
        });
    };

    return (
        <div>
            {/* Header and Actions */}
            <div className="flex flex-col md:flex-row md:items-end justify-between gap-4 mb-8">
                <div>
                    <h1 className="text-4xl md:text-5xl font-black text-slate-900 tracking-tight mb-3">Explore Skills</h1>
                    <p className="text-slate-500 text-lg md:text-xl font-medium max-w-2xl leading-relaxed">Discover expert mentors and master new skills.</p>
                </div>
                {user?.roles?.includes('TEACHER') && (
                    <button
                        onClick={() => setShowCreateModal(true)}
                        className="bg-brand-gradient text-slate-500 bg-white px-6 py-3 rounded-2xl font-bold transition-all flex items-center gap-2 shadow-lg shadow-purple-500/30 hover:shadow-purple-500/50 hover:scale-105 active:scale-95 border border-white/20"
                    >
                        <Plus className="w-5 h-5 stroke-[3]" />
                        Offer a Skill
                    </button>
                )}
            </div>

            {/* Search Bar - Clay Pressed Well */}
            <div className="relative mb-10 max-w-2xl bg-slate-50 p-2 rounded-[2rem] shadow-[inset_6px_6px_14px_rgba(0,0,0,0.05),inset_-6px_-6px_14px_rgba(255,255,255,1)] flex items-center transition-all">
                <div className="absolute left-6 top-1/2 -translate-y-1/2 text-slate-400">
                    <Search className="w-5 h-5" />
                </div>
                <input
                    type="text"
                    placeholder="Search for skills, topics, or mentors..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-12 pr-4 py-3 bg-transparent border-none focus:ring-0 text-slate-700 placeholder:text-slate-400 font-medium text-base rounded-[2rem]"
                />
                <button className="p-3 mr-1 text-slate-400 hover:text-indigo-600 rounded-full transition-all hover:bg-white hover:shadow-sm">
                    <SlidersHorizontal className="w-5 h-5" />
                </button>
            </div>

            {/* Quick Categories - Clay Floating Bubbles */}
            <div className="flex items-center gap-4 mb-10 overflow-x-auto pb-6 scrollbar-hide px-2">
                {['All', 'Development', 'Design', 'Business', 'Music', 'Lifestyle', 'Academics'].map((category) => (
                    <button
                        key={category}
                        onClick={() => setSearchQuery(category === 'All' ? '' : category)}
                        className={cn(
                            "px-6 py-3 rounded-2xl text-sm font-bold whitespace-nowrap transition-all duration-300",
                            (searchQuery === category || (category === 'All' && searchQuery === ''))
                                ? "bg-indigo-50 text-indigo-700 shadow-[inset_4px_4px_8px_rgba(0,0,0,0.05),inset_-4px_-4px_8px_rgba(255,255,255,1)] scale-95"
                                : "bg-white text-slate-500 shadow-[6px_6px_16px_rgba(0,0,0,0.04),-6px_-6px_16px_rgba(255,255,255,1)] hover:-translate-y-1 hover:text-indigo-600"
                        )}
                    >
                        {category}
                    </button>
                ))}
            </div>

            {/* Grid */}
            {loading ? (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                    {[1, 2, 3, 4, 5, 6].map(i => (
                        <div key={i} className="h-[400px] bg-clay-card rounded-[2.5rem] shadow-clay-card animate-pulse" />
                    ))}
                </div>
            ) : skills.length === 0 ? (
                <div className="flex flex-col gap-12">
                    {/* Empty State Message - Clay Inflated Card */}
                    <div className="text-center py-20 bg-white rounded-[3rem] shadow-[12px_12px_32px_rgba(0,0,0,0.05),-12px_-12px_32px_rgba(255,255,255,1)] border border-white mx-auto max-w-full w-full relative">
                        <div className="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-8 shadow-[inset_5px_5px_10px_rgba(0,0,0,0.05),inset_-5px_-5px_10px_rgba(255,255,255,1)]">
                            <Search className="w-10 h-10 text-indigo-400" />
                        </div>
                        <h3 className="text-2xl font-bold text-slate-900 mb-3">
                            {searchQuery ? `No matches for "${searchQuery}"` : "No skills found"}
                        </h3>
                        <p className="text-slate-500 max-w-md mx-auto mt-2 mb-10 text-lg font-medium">
                            We couldn't find exactly what you're looking for, but check out these top-rated mentors below!
                        </p>

                        <button
                            onClick={() => setSearchQuery('')}
                            className="text-indigo-600 font-bold hover:text-indigo-700 bg-indigo-50 px-8 py-3.5 rounded-2xl hover:shadow-lg hover:-translate-y-0.5 transition-all duration-300"
                        >
                            Clear Search & View All
                        </button>
                    </div>

                    {/* Recommendations (Fallback) */}
                    <div>
                        <h2 className="text-2xl font-bold text-slate-900 mb-8 flex items-center gap-3 ml-2">
                            <div className="p-2 bg-amber-100 rounded-lg">
                                <Star className="w-6 h-6 text-amber-500 fill-current" />
                            </div>
                            Recommended for You
                        </h2>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 opacity-75 grayscale-[0.3] hover:grayscale-0 hover:opacity-100 transition-all duration-500">
                            {recommendedSkills.slice(0, 3).map(skill => (
                                <SkillCard
                                    key={skill.id}
                                    skill={skill}
                                    onClick={() => setSelectedSkill(skill)}
                                    onReviewClick={handleReviewClick}
                                />
                            ))}
                        </div>
                    </div>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 pb-10">
                    {skills.map(skill => (
                        <SkillCard
                            key={skill.id}
                            skill={skill}
                            onClick={() => setSelectedSkill(skill)}
                            onReviewClick={handleReviewClick}
                        />
                    ))}
                </div>
            )}

            {showCreateModal && (
                <CreateSkillModal
                    onClose={() => setShowCreateModal(false)}
                    onSuccess={() => fetchSkills(searchQuery)}
                />
            )}

            {selectedSkill && (
                <BookingModal
                    skill={selectedSkill}
                    onClose={() => setSelectedSkill(null)}
                    onSuccess={() => { }}
                />
            )}

            {/* Review Modal for Viewing Reviews */}
            {reviewModalConfig && (
                <ReviewModal
                    isOpen={reviewModalConfig.isOpen}
                    onClose={() => setReviewModalConfig(null)}
                    mode="view"
                    teacherId={reviewModalConfig.teacherId}
                    targetName={reviewModalConfig.targetName}
                />
            )}
        </div>
    );
};

export default ExploreSkills;
