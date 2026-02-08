import React, { useState } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { Toaster } from 'react-hot-toast';
import ProtectedRoute from './components/ProtectedRoute';
import ErrorBoundary from './components/ErrorBoundary';
import { NotificationProvider } from './context/NotificationContext';
import SplashScreen from './components/SplashScreen';

// Layouts
import DashboardLayout from './layouts/DashboardLayout';
import AuthLayout from './layouts/AuthLayout';

// Pages
import Login from './pages/Auth/Login';
import Register from './pages/Auth/Register';
import ForgotPassword from './pages/Auth/ForgotPassword';
import ResetPassword from './pages/Auth/ResetPassword';
import CreateProfile from './pages/Auth/CreateProfile';
import ExploreSkills from './pages/Dashboard/ExploreSkills';
import MySkills from './pages/Dashboard/MySkills';
import MyBookings from './pages/Dashboard/MyBookings';
import Chat from './pages/Dashboard/Chat';
import Profile from './pages/Dashboard/Profile';
import AdminUsers from './pages/Dashboard/AdminUsers';
import AdminReviews from './pages/Dashboard/AdminReviews';

function App() {
  const [showSplash, setShowSplash] = useState(true);

  if (showSplash) {
    return <SplashScreen onFinish={() => setShowSplash(false)} />;
  }

  return (
    <BrowserRouter>
      <AuthProvider>
        <ErrorBoundary>
          <NotificationProvider>
            <Toaster position="top-center" toastOptions={{
              className: 'rounded-xl shadow-lg border border-slate-100 font-medium',
              duration: 3000,
            }} />
            <Routes>
              {/* Public Routes */}
              <Route element={<AuthLayout />}>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
                <Route path="/forgot-password" element={<ForgotPassword />} />
                <Route path="/reset-password" element={<ResetPassword />} />
              </Route>

              {/* Protected Dashboard Routes */}
              <Route path="/" element={
                <ProtectedRoute>
                  <DashboardLayout />
                </ProtectedRoute>
              }>
                <Route index element={<ExploreSkills />} />
                <Route path="my-skills" element={<MySkills />} />
                <Route path="bookings" element={<MyBookings />} />
                <Route path="chat" element={<Chat />} />
                <Route path="profile" element={<Profile />} />
                <Route path="profile/:userId" element={<Profile />} />
                <Route path="admin/users" element={<AdminUsers />} />
                <Route path="admin/reviews" element={<AdminReviews />} />
              </Route>

              {/* Onboarding Route - Protected but separate from Dashboard Layout if desired, or same? 
                  The requirement says "next page show to user profile". 
                  Let's keep it separate for focus. */}
              <Route path="/create-profile" element={
                <ProtectedRoute>
                  <CreateProfile />
                </ProtectedRoute>
              } />

              {/* Fallback */}
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </NotificationProvider>
        </ErrorBoundary>
      </AuthProvider>
    </BrowserRouter>
  );
}

export default App;
