import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { userService } from '../../api/user';
import { User, MapPin, Briefcase, ChevronRight, Loader2, Camera } from 'lucide-react';
import toast from 'react-hot-toast';

const CreateProfile = () => {
    const { user } = useAuth();
    const navigate = useNavigate();
    const [loading, setLoading] = useState(false);
    const [formData, setFormData] = useState({
        displayName: '',
        headline: '',
        bio: '',
        location: '',
        skills: ''
    });

    const [locationLoading, setLocationLoading] = useState(false);

    const detectLocation = () => {
        if (!navigator.geolocation) {
            toast.error("Geolocation is not supported by your browser");
            return;
        }

        setLocationLoading(true);
        navigator.geolocation.getCurrentPosition(async (position) => {
            try {
                const { latitude, longitude } = position.coords;
                // Using OpenStreetMap Nominatim API (Free, no key required)
                const response = await fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${latitude}&lon=${longitude}`);
                const data = await response.json();

                const city = data.address.city || data.address.town || data.address.village || data.address.state;
                const country = data.address.country;

                setFormData(prev => ({
                    ...prev,
                    location: `${city}, ${country}`
                }));
                toast.success("Location detected!");
            } catch (error) {
                console.error("Error fetching location:", error);
                toast.error("Failed to detect location details.");
            } finally {
                setLocationLoading(false);
            }
        }, (error) => {
            console.error("Geolocation error:", error);
            setLocationLoading(false);
            switch (error.code) {
                case error.PERMISSION_DENIED:
                    toast.error("Location permission denied");
                    break;
                case error.POSITION_UNAVAILABLE:
                    toast.error("Location information unavailable");
                    break;
                case error.TIMEOUT:
                    toast.error("Location request timed out");
                    break;
                default:
                    toast.error("An unknown error occurred");
            }
        });
    };

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setLoading(true);

        try {
            const skillsArray = formData.skills
                .split(',')
                .map(skill => skill.trim())
                .filter(skill => skill.length > 0);

            await userService.createProfile({
                displayName: formData.displayName,
                headline: formData.headline,
                bio: formData.bio,
                city: formData.location,  // Map location to city for backend
                skills: skillsArray
            });
            toast.success("Profile setup complete!");
            navigate('/');
        } catch (error) {
            console.error(error);
            toast.error("Failed to save profile. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-slate-50 flex items-center justify-center p-4">
            <div className="max-w-2xl w-full bg-white rounded-3xl shadow-xl overflow-hidden border border-slate-100">
                <div className="bg-blue-600 p-8 text-white text-center">
                    <h1 className="text-3xl font-bold mb-2">Welcome to SkillShare!</h1>
                    <p className="text-blue-100">Let's set up your profile to help others get to know you.</p>
                </div>

                <div className="p-8">
                    <form onSubmit={handleSubmit} className="space-y-6">

                        {/* Avatar Placeholder - Logic to upload would go here */}
                        <div className="flex justify-center -mt-16 mb-8">
                            <div className="relative">
                                <div className="w-24 h-24 rounded-full bg-white p-1 shadow-lg">
                                    <div className="w-full h-full rounded-full bg-slate-100 flex items-center justify-center overflow-hidden">
                                        <User className="w-10 h-10 text-slate-300" />
                                    </div>
                                </div>
                                <button type="button" className="absolute bottom-0 right-0 bg-blue-600 text-white p-2 rounded-full shadow-md hover:bg-blue-700 transition-colors">
                                    <Camera className="w-4 h-4" />
                                </button>
                            </div>
                        </div>

                        <div className="grid md:grid-cols-2 gap-6">
                            <div className="col-span-2">
                                <label className="block text-sm font-bold text-slate-700 mb-2">Full Name</label>
                                <div className="relative">
                                    <User className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
                                    <input
                                        type="text"
                                        name="displayName"
                                        value={formData.displayName}
                                        onChange={handleChange}
                                        placeholder="John Doe"
                                        className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all outline-none"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="col-span-2">
                                <label className="block text-sm font-bold text-slate-700 mb-2">Headline</label>
                                <div className="relative">
                                    <Briefcase className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
                                    <input
                                        type="text"
                                        name="headline"
                                        value={formData.headline}
                                        onChange={handleChange}
                                        placeholder="e.g. Software Engineer at Google | React Enthusiast"
                                        className="w-full pl-10 pr-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all outline-none"
                                        required
                                    />
                                </div>
                            </div>

                            <div className="col-span-2">
                                <label className="block text-sm font-bold text-slate-700 mb-2">Bio</label>
                                <textarea
                                    name="bio"
                                    value={formData.bio}
                                    onChange={handleChange}
                                    placeholder="Tell us a bit about yourself..."
                                    rows="4"
                                    className="w-full p-4 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all outline-none resize-none"
                                />
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-slate-700 mb-2">Location</label>
                                <div className="relative">
                                    <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 w-5 h-5" />
                                    <input
                                        type="text"
                                        name="location"
                                        value={formData.location}
                                        onChange={handleChange}
                                        placeholder="e.g. New York, USA"
                                        className="w-full pl-10 pr-32 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all outline-none"
                                    />
                                    <button
                                        type="button"
                                        onClick={detectLocation}
                                        disabled={locationLoading}
                                        className="absolute right-2 top-1/2 -translate-y-1/2 text-xs font-semibold bg-blue-100 text-blue-600 px-3 py-1.5 rounded-lg hover:bg-blue-200 transition-colors disabled:opacity-50 flex items-center gap-1"
                                    >
                                        {locationLoading ? <Loader2 className="w-3 h-3 animate-spin" /> : <MapPin className="w-3 h-3" />}
                                        Detect
                                    </button>
                                </div>
                            </div>

                            <div>
                                <label className="block text-sm font-bold text-slate-700 mb-2">Skills (Comma separated)</label>
                                <input
                                    type="text"
                                    name="skills"
                                    value={formData.skills}
                                    onChange={handleChange}
                                    placeholder="e.g. React, Java, Design"
                                    className="w-full px-4 py-3 bg-slate-50 border border-slate-200 rounded-xl focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 transition-all outline-none"
                                />
                            </div>
                        </div>

                        <div className="pt-4">
                            <button
                                type="submit"
                                disabled={loading}
                                className="w-full bg-blue-600 hover:bg-blue-700 text-white font-bold py-4 rounded-xl shadow-lg shadow-blue-500/30 transition-all flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed"
                            >
                                {loading ? <Loader2 className="w-5 h-5 animate-spin" /> : (
                                    <>
                                        Save & Continue <ChevronRight className="w-5 h-5" />
                                    </>
                                )}
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    );
};

export default CreateProfile;
