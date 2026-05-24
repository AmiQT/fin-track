import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Layout from './components/Layout';
import ErrorBoundary from './components/ErrorBoundary';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Employees from './pages/Employees';
import Payroll from './pages/Payroll';
import Leaves from './pages/Leaves';
import MyPayslips from './pages/MyPayslips';
import MyLeaves from './pages/MyLeaves';
import MyProfile from './pages/MyProfile';

const LoadingSpinner = () => (
  <div className="min-h-screen flex items-center justify-center bg-gray-950">
    <div className="flex flex-col items-center gap-3">
      <div className="w-10 h-10 border-4 border-blue-500 border-t-transparent rounded-full animate-spin" />
      <span className="text-gray-400 text-sm">Loading...</span>
    </div>
  </div>
);

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { user, loading, isAdmin } = useAuth();

  if (loading) return <LoadingSpinner />;
  if (!user) return <Navigate to="/login" />;
  if (requireAdmin && !isAdmin) return <Navigate to="/" />;

  return children;
};

function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route path="/login" element={<Login />} />

            <Route path="/" element={
              <ProtectedRoute>
                <Layout />
              </ProtectedRoute>
            }>
              <Route index element={<ErrorBoundary><Dashboard /></ErrorBoundary>} />
              <Route path="employees" element={<ProtectedRoute requireAdmin><ErrorBoundary><Employees /></ErrorBoundary></ProtectedRoute>} />
              <Route path="payroll" element={<ProtectedRoute requireAdmin><ErrorBoundary><Payroll /></ErrorBoundary></ProtectedRoute>} />
              <Route path="leaves" element={<ProtectedRoute requireAdmin><ErrorBoundary><Leaves /></ErrorBoundary></ProtectedRoute>} />
              <Route path="my-payslips" element={<ErrorBoundary><MyPayslips /></ErrorBoundary>} />
              <Route path="my-leaves" element={<ErrorBoundary><MyLeaves /></ErrorBoundary>} />
              <Route path="my-profile" element={<ErrorBoundary><MyProfile /></ErrorBoundary>} />
            </Route>

            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}

export default App;
