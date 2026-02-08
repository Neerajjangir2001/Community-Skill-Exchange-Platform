import React, { useState, useEffect } from 'react';
import { skillService } from '../../api/skills';
import { Plus, Edit2, Trash2, BookOpen } from 'lucide-react';
import toast from 'react-hot-toast';
import CreateSkillModal from '../../components/CreateSkillModal';

const MySkills = () => {
    const [skills, setSkills] = useState([]);
    const [loading, setLoading] = useState(true);

    const [showCreateModal, setShowCreateModal] = useState(false);
    const [editingSkill, setEditingSkill] = useState(null);

    useEffect(() => {
        fetchMySkills();
    }, []);

    const fetchMySkills = async () => {
        setLoading(true);
        try {
            const data = await skillService.getMySkills();
            setSkills(data);
        } catch (error) {
            console.error('Failed to fetch my skills', error);
        } finally {
            setLoading(false);
        }
    };

    const handleEdit = (skill) => {
        setEditingSkill(skill);
        setShowCreateModal(true);
    };

    const handleDelete = async (id) => {
        if (!window.confirm('Are you sure you want to delete this skill?')) return;
        try {
            await skillService.deleteSkill(id);
            toast.success('Skill deleted');
            fetchMySkills();
        } catch (error) {
            toast.error('Failed to delete skill');
        }
    };

    return (
        <div>
            <div className="flex items-center justify-between mb-8">
                <h1 className="text-2xl font-bold text-slate-900">My Skills</h1>
                <button
                    onClick={() => {
                        setEditingSkill(null);
                        setShowCreateModal(true);
                    }}
                    className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-xl font-medium transition-all flex items-center gap-2 shadow-sm"
                >
                    <Plus className="w-5 h-5" />
                    Offer New Skill
                </button>
            </div>

            {loading ? (
                <div className="text-center py-12">Loading...</div>
            ) : skills.length === 0 ? (
                <div className="text-center py-16 bg-white rounded-2xl border border-dashed border-slate-200">
                    <BookOpen className="w-12 h-12 text-slate-300 mx-auto mb-3" />
                    <h3 className="text-lg font-medium text-slate-900">You haven't offered any skills yet.</h3>
                    <p className="text-slate-500 mt-1 mb-6">Share your expertise with the community!</p>
                    <button
                        onClick={() => {
                            setEditingSkill(null);
                            setShowCreateModal(true);
                        }}
                        className="text-blue-600 font-medium hover:underline"
                    >
                        Create your first skill
                    </button>
                </div>
            ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                    {skills.map(skill => (
                        <div key={skill.id} className="bg-white rounded-2xl border border-slate-100 shadow-sm overflow-hidden flex flex-col group">
                            <div className="p-6 flex-1">
                                <div className="flex justify-between items-start mb-4">
                                    <span className="px-2 py-1 bg-blue-50 text-blue-700 text-xs font-semibold rounded-md uppercase">
                                        {skill.level}
                                    </span>
                                    <span className="font-semibold text-slate-900">
                                        ₹{skill.pricePerHour}/hr
                                    </span>
                                </div>
                                <h3 className="text-lg font-bold text-slate-900 mb-2">{skill.title}</h3>
                                <p className="text-slate-500 text-sm line-clamp-3 mb-4">{skill.description}</p>
                                <div className="flex flex-wrap gap-2">
                                    {skill.tags?.map((tag, i) => (
                                        <span key={i} className="text-xs text-slate-500 bg-slate-100 px-2 py-1 rounded">#{tag}</span>
                                    ))}
                                </div>
                            </div>
                            <div className="p-4 bg-white border-t border-slate-100 flex justify-end gap-2">
                                <button
                                    onClick={() => handleEdit(skill)}
                                    className="p-2 text-slate-500 hover:bg-white hover:text-blue-600 rounded-lg transition-colors border border-transparent hover:border-slate-200"
                                >
                                    <Edit2 className="w-4 h-4" />
                                </button>
                                <button
                                    onClick={() => handleDelete(skill.id)}
                                    className="p-2 text-rose-500 hover:bg-white hover:text-rose-600 rounded-lg transition-colors border border-transparent hover:border-slate-200"
                                >
                                    <Trash2 className="w-4 h-4" />
                                </button>
                            </div>
                        </div>
                    ))}
                </div>
            )}

            {showCreateModal && (
                <CreateSkillModal
                    onClose={() => {
                        setShowCreateModal(false);
                        setEditingSkill(null);
                    }}
                    onSuccess={fetchMySkills}
                    initialData={editingSkill}
                />
            )}
        </div>
    );
};

export default MySkills;
