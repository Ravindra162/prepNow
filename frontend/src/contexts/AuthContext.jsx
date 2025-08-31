import { createContext, useContext, useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { authService } from '../utils/api';
import { toast } from 'react-hot-toast';

const AuthContext = createContext();

export function useAuth() {
  return useContext(AuthContext);
}

export function AuthProvider({ children }) {
  const [currentUser, setCurrentUser] = useState(null);
  const [loading, setLoading] = useState(true);
  const [verificationStatus, setVerificationStatus] = useState({
    email: '',
    isVerified: false,
    userId: null
  });
  const navigate = useNavigate();

  // Check for existing session on initial load
  useEffect(() => {
    // In a real app, you would verify the session with your backend here
    const user = JSON.parse(localStorage.getItem('prepnow-user'));
    if (user) {
      setCurrentUser(user);
    }
    setLoading(false);
  }, []);

  // Register a new user
  const register = async (userData) => {
    try {
      const response = await authService.register({
        email: userData.email,
        password: userData.password,
        username: userData.username || userData.email.split('@')[0]
      });
      
      setVerificationStatus({
        email: userData.email,
        isVerified: false,
        userId: response.userId
      });
      
      return { 
        success: true, 
        message: response.message,
        userId: response.userId
      };
    } catch (error) {
      const errorMessage = error.message || 'Failed to register. Please try again.';
      return { 
        success: false, 
        error: typeof error === 'string' ? error : errorMessage 
      };
    }
  };

  // Verify email with OTP
  const verifyEmail = async (email, otp) => {
    try {
      const response = await authService.verifyEmail(email, otp);
      
      // Update verification status
      setVerificationStatus(prev => ({
        ...prev,
        isVerified: true
      }));
      
      return { 
        success: true, 
        message: response.message || 'Email verified successfully!' 
      };
    } catch (error) {
      return { 
        success: false, 
        error: error.message || 'Invalid or expired OTP. Please try again.' 
      };
    }
  };

  // Resend OTP
  const resendOtp = async (email) => {
    try {
      const response = await authService.resendOtp(email);
      return { 
        success: true, 
        message: response.message || 'New OTP sent successfully.' 
      };
    } catch (error) {
      return { 
        success: false, 
        error: error.message || 'Failed to resend OTP. Please try again.' 
      };
    }
  };

  // Login function
  const login = async (email, password) => {
    try {
      const response = await authService.login(email, password);
      
      // Save user data to local storage
      const user = { 
        email,
        name: response.username || email.split('@')[0],
        isAuthenticated: true
      };
      
      localStorage.setItem('prepnow-user', JSON.stringify(user));
      setCurrentUser(user);
      
      return { 
        success: true, 
        username: response.username 
      };
    } catch (error) {
      return { 
        success: false, 
        error: error.message || 'Invalid email or password. Please try again.' 
      };
    }
  };

  // Logout function
  const logout = async () => {
    try {
      await authService.logout();
    } catch (error) {
      console.error('Logout error:', error);
    } finally {
      localStorage.removeItem('prepnow-user');
      setCurrentUser(null);
      setVerificationStatus({ email: '', isVerified: false, userId: null });
      navigate('/login');
    }
  };

  const value = {
    currentUser,
    verificationStatus,
    register,
    verifyEmail,
    resendOtp,
    login,
    logout,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
}

export default AuthContext;
