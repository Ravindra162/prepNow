import axios from 'axios';

const API_BASE_URL = 'http://localhost:8081/auth'; // Update this with your backend URL

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
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

// Add a response interceptor to handle common errors
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // The request was made and the server responded with a status code
      // that falls out of the range of 2xx
      console.error('API Error:', error.response.data);
      return Promise.reject(error.response.data || 'Something went wrong');
    } else if (error.request) {
      // The request was made but no response was received
      console.error('API Error:', error.request);
      return Promise.reject('No response from server. Please check your connection.');
    } else {
      // Something happened in setting up the request that triggered an Error
      console.error('API Error:', error.message);
      return Promise.reject(error.message);
    }
  }
);

export const authService = {
  // Register a new user
  register: async (userData) => {
    const response = await api.post('/register', userData);
    return response.data;
  },

  // Verify email with OTP
  verifyEmail: async (email, otp) => {
    const response = await api.post('/verify-email', null, {
      params: { email, otp }
    });
    return response.data;
  },

  // Resend OTP
  resendOtp: async (email) => {
    const response = await api.post('/resend-otp', null, {
      params: { email }
    });
    return response.data;
  },

  // Login
  login: async (email, password) => {
    const response = await api.post('/login', { email, password });
    return response.data;
  },

  // Logout
  logout: async () => {
    const response = await api.post('/logout');
    return response.data;
  },
};

export default api;
