import axios from "axios";
import config from "../config";

const api = axios.create({
  baseURL: config.API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
  withCredentials: true, // Important for cookies/sessions
});

// Add a request interceptor to include the auth token in requests
api.interceptors.request.use(
  (config) => {
    // You can add auth headers here if needed
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Add a response interceptor for handling errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    // Handle errors here
    return Promise.reject(error);
  }
);

// API functions
export const authApi = {
  // Register a new user
  register: (userData) => api.post('/register', userData),
  
  // Verify email with OTP
  verifyEmail: (email, otp) => api.post('/verify-email', { email, otp }),
  
  // Resend OTP
  resendOtp: (email) => api.post('/resend-otp', { email }),
  
  // Login
  login: (email, password) => api.post('/login', { email, password }),
  
  // Logout
  logout: () => api.post('/logout')
};

export default authApi;
