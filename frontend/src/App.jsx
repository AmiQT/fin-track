import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import { ToastProvider } from './context/ToastContext';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Employees from './pages/Employees';
import Payroll from './pages/Payroll';
import Leaves from './pages/Leaves';
import MyPayslips from './pages/MyPayslips';
import MyLeaves from './pages/MyLeaves';
import MyProfile from './pages/MyProfile';

const ProtectedRoute = ({ children, requireAdmin = false }) => {
  const { user, loading, isAdmin } = useAuth();
  
  if (loading) return null;
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
              <Route index element={<Dashboard />} />
              <Route path="employees" element={<ProtectedRoute requireAdmin><Employees /></ProtectedRoute>} />
              <Route path="payroll" element={<ProtectedRoute requireAdmin><Payroll /></ProtectedRoute>} />
              <Route path="leaves" element={<ProtectedRoute requireAdmin><Leaves /></ProtectedRoute>} />
              <Route path="my-payslips" element={<MyPayslips />} />
              <Route path="my-leaves" element={<MyLeaves />} />
              <Route path="my-profile" element={<MyProfile />} />
            </Route>

            <Route path="*" element={<Navigate to="/" />} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  );
}

export default App;