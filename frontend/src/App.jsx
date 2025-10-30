import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { Toaster } from 'react-hot-toast';
import { AuthProvider, useAuth } from './contexts/AuthContext';

// Pages
import Home from './pages/Home';
import Login from './pages/Login';
import Signup from './pages/Signup';
import Dashboard from './pages/Dashboard';
import Companies from './pages/Companies';
import AssessmentInstructions from './pages/AssessmentInstructions';
import AssessmentTest from './pages/AssessmentTest';
import MyTests from './pages/MyTests';

// Admin Components
import AdminLayout from './components/AdminLayout';
import AdminDashboard from './pages/AdminDashboard';
import AdminSections from './pages/AdminSections';
import AdminQuestions from './pages/AdminQuestions';
import AdminAssessments from './pages/AdminAssessments';
import AdminCompanies from './pages/AdminCompanies';

// Private Route Component
const PrivateRoute = ({ children }) => {
  const { currentUser } = useAuth();
  return currentUser ? children : <Navigate to="/login" />;
};

function App() {
  return (
    <AuthProvider>
      <div className="min-h-screen bg-gray-50">
        <Toaster position="top-right" />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/login" element={<Login />} />
          <Route path="/signup" element={<Signup />} />
          <Route 
            path="/dashboard" 
            element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/companies" 
            element={
              <PrivateRoute>
                <Companies />
              </PrivateRoute>
            }
          />
          <Route
            path="/my-tests"
            element={
              <PrivateRoute>
                <MyTests />
              </PrivateRoute>
            }
          />
          <Route 
            path="/assessment/:assessmentId/instructions" 
            element={
              <PrivateRoute>
                <AssessmentInstructions />
              </PrivateRoute>
            } 
          />
          <Route 
            path="/assessment/:assessmentId/test" 
            element={
              <PrivateRoute>
                <AssessmentTest />
              </PrivateRoute>
            } 
          />
          
          {/* Admin Routes */}
          <Route 
            path="/admin" 
            element={
              <PrivateRoute>
                <AdminLayout />
              </PrivateRoute>
            }
          >
            <Route index element={<AdminDashboard />} />
            <Route path="sections" element={<AdminSections />} />
            <Route path="questions" element={<AdminQuestions />} />
            <Route path="assessments" element={<AdminAssessments />} />
            <Route path="companies" element={<AdminCompanies />} />
          </Route>
          
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </AuthProvider>
  );
}

export default App;