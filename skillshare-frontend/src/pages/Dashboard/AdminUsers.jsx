import React, { useState, useEffect } from 'react';
import { userService } from '../../api/user';
import { Search, Mail, Shield, Check, X, Trash2 } from 'lucide-react';
import toast from 'react-hot-toast';
import { format } from 'date-fns';
import { useNavigate } from 'react-router-dom';

const AdminUsers = () => {
    const [users, setUsers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [searchTerm, setSearchTerm] = useState('');
    const navigate = useNavigate();

    useEffect(() => {
        fetchUsers();
    }, []);

    const fetchUsers = async () => {
        try {
            const data = await userService.getAllUsers();
            setUsers(data);
        } catch (error) {
            console.error('Failed to fetch users:', error);
            toast.error('Failed to load users');
        } finally {
            setLoading(false);
        }
    };

    const handleDeleteUser = async (userId) => {
        if (!window.confirm('Are you sure you want to delete this user? This cannot be undone.')) return;

        try {
            await userService.deleteUser(userId);
            toast.success('User deleted successfully');
            setUsers(users.filter(u => u.id !== userId));
        } catch (error) {
            console.error('Failed to delete user:', error);
            toast.error('Failed to delete user');
        }
    };

    const filteredUsers = users.filter(user =>
        user.email.toLowerCase().includes(searchTerm.toLowerCase()) ||
        (user.roles && user.roles.some(role => role.toLowerCase().includes(searchTerm.toLowerCase())))
    );

    return (
        <div className="space-y-8">
            <div className="flex flex-col md:flex-row md:items-center justify-between gap-4">
                <div>
                    <h1 className="text-3xl font-black text-slate-900 font-display">User Management</h1>
                    <p className="text-slate-500 font-medium">Manage all registered users, teachers, and admins.</p>
                </div>

                <div className="relative max-w-md w-full md:w-auto">
                    <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-400" />
                    <input
                        type="text"
                        placeholder="Search users..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full pl-12 pr-4 py-3 bg-white rounded-xl border border-slate-100 shadow-sm focus:outline-none focus:ring-2 focus:ring-indigo-100 text-sm font-medium"
                    />
                </div>
            </div>

            <div className="bg-white rounded-[2rem] shadow-clay-card border border-white/50 overflow-hidden">
                <div className="overflow-x-auto">
                    <table className="w-full text-left border-collapse">
                        <thead>
                            <tr className="bg-slate-50/50 border-b border-slate-100">
                                <th className="px-6 py-4 text-xs font-black text-slate-400 uppercase tracking-wider">User</th>
                                <th className="px-6 py-4 text-xs font-black text-slate-400 uppercase tracking-wider">Roles</th>
                                <th className="px-6 py-4 text-xs font-black text-slate-400 uppercase tracking-wider">Status</th>
                                <th className="px-6 py-4 text-xs font-black text-slate-400 uppercase tracking-wider text-right">Actions</th>
                            </tr>
                        </thead>
                        <tbody className="divide-y divide-slate-50">
                            {loading ? (
                                <tr>
                                    <td colSpan="4" className="px-6 py-8 text-center text-slate-400 font-medium">
                                        Loading users...
                                    </td>
                                </tr>
                            ) : filteredUsers.length > 0 ? (
                                filteredUsers.map((user) => (
                                    <tr
                                        key={user.id}
                                        className="hover:bg-slate-50/50 transition-colors group cursor-pointer"
                                        onClick={() => navigate(`/profile/${user.id}`, { state: { roles: user.roles } })}
                                    >
                                        <td className="px-6 py-4">
                                            <div className="flex items-center gap-3">
                                                <div className="w-10 h-10 rounded-full bg-indigo-50 flex items-center justify-center text-indigo-600 font-bold">
                                                    {user.email[0].toUpperCase()}
                                                </div>
                                                <div>
                                                    <p className="font-bold text-slate-800 text-sm">{user.email}</p>
                                                    <p className="text-xs text-slate-400 font-mono mt-0.5">{user.id.substring(0, 8)}...</p>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex flex-wrap gap-2">
                                                {user.roles.map(role => (
                                                    <span key={role} className={`px-2 py-1 rounded-lg text-[10px] font-bold uppercase tracking-wider border ${role === 'ADMIN' ? 'bg-rose-50 text-rose-600 border-rose-100' :
                                                        role === 'TEACHER' ? 'bg-indigo-50 text-indigo-600 border-indigo-100' :
                                                            'bg-emerald-50 text-emerald-600 border-emerald-100'
                                                        }`}>
                                                        {role}
                                                    </span>
                                                ))}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            {user.enabled ? (
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-emerald-100/50 text-emerald-700 text-xs font-bold">
                                                    <Check className="w-3 h-3" /> Active
                                                </span>
                                            ) : (
                                                <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full bg-slate-100 text-slate-500 text-xs font-bold">
                                                    <X className="w-3 h-3" /> Inactive
                                                </span>
                                            )}
                                        </td>
                                        <td className="px-6 py-4 text-right">
                                            <button
                                                onClick={(e) => {
                                                    e.stopPropagation();
                                                    handleDeleteUser(user.id);
                                                }}
                                                className="p-2 text-slate-400 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-all opacity-0 group-hover:opacity-100"
                                                title="Delete User"
                                            >
                                                <Trash2 className="w-4 h-4" />
                                            </button>
                                        </td>
                                    </tr>
                                ))
                            ) : (
                                <tr>
                                    <td colSpan="4" className="px-6 py-12 text-center text-slate-400">
                                        <div className="w-16 h-16 bg-slate-50 rounded-full flex items-center justify-center mx-auto mb-4">
                                            <Shield className="w-8 h-8 text-slate-300" />
                                        </div>
                                        <p className="font-medium">No users found</p>
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    );
};

export default AdminUsers;
