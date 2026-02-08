import React, { useState } from 'react';
import { skillService } from '../api/skills';
import { X, Loader2 } from 'lucide-react';
import toast from 'react-hot-toast';

const CreateSkillModal = ({ onClose, onSuccess, initialData = null }) => {
    const [formData, setFormData] = useState({
        title: initialData?.title || '',
        description: initialData?.description || '',
        level: initialData?.level || 'BEGINNER',
        pricePerHour: initialData?.pricePerHour || 0,
        tags: initialData?.tags?.join(', ') || ''
    });
    const [loading, setLoading] = useState(false);

    const isEditing = !!initialData;

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const tagsArray = formData.tags.split(',').map(t => t.trim()).filter(Boolean);
            const payload = {
                ...formData,
                tags: tagsArray
            };

            if (isEditing) {
                await skillService.updateSkill(initialData.id, payload);
                toast.success('Skill updated successfully!');
            } else {
                await skillService.createSkill(payload);
                toast.success('Skill created successfully!');
            }

            onSuccess?.();
            onClose();
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message || `Failed to ${isEditing ? 'update' : 'create'} skill`;
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
                    <h2 className="text-xl font-bold text-slate-900">{isEditing ? 'Edit Skill' : 'Offer a Skill'}</h2>
                    <button onClick={onClose} className="p-2 hover:bg-slate-100 rounded-full transition-colors text-slate-500">
                        <X className="w-5 h-5" />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-4">
                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Title</label>
                        <input
                            type="text"
                            required
                            value={formData.title}
                            onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                            className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                            placeholder="e.g. Advanced React Patterns"
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Description</label>
                        <textarea
                            required
                            rows={3}
                            value={formData.description}
                            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                            className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none resize-none"
                            placeholder="Describe what you will teach..."
                        />
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Level</label>
                            <select
                                value={formData.level}
                                onChange={(e) => setFormData({ ...formData, level: e.target.value })}
                                className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                            >
                                <option value="BEGINNER">Beginner</option>
                                <option value="INTERMEDIATE">Intermediate</option>
                                <option value="ADVANCED">Advanced</option>
                            </select>
                        </div>
                        <div>
                            <label className="block text-sm font-medium text-slate-700 mb-1">Price / Hour ($)</label>
                            <input
                                type="number"
                                min="0"
                                value={formData.pricePerHour}
                                onChange={(e) => setFormData({ ...formData, pricePerHour: parseFloat(e.target.value) })}
                                className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                            />
                        </div>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-slate-700 mb-1">Tags (comma separated)</label>
                        <input
                            type="text"
                            value={formData.tags}
                            onChange={(e) => setFormData({ ...formData, tags: e.target.value })}
                            className="w-full px-4 py-2 bg-slate-50 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none"
                            placeholder="react, coding, web dev"
                        />
                    </div>

                    <div className="pt-2 flex justify-end gap-3">
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
                            {loading ? (isEditing ? 'Updating...' : 'Creating...') : (isEditing ? 'Update Skill' : 'Create Skill')}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default CreateSkillModal;
