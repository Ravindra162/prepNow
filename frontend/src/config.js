const config = {
  // Base API URLs
  API_BASE_URL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
  QUESTION_SERVICE_URL: process.env.REACT_APP_QUESTION_SERVICE_URL || 'http://localhost:8082',
  ASSESSMENT_SERVICE_URL: process.env.REACT_APP_ASSESSMENT_SERVICE_URL || 'http://localhost:8081',
  CODE_EXECUTION_SERVICE_URL: process.env.REACT_APP_CODE_EXECUTION_SERVICE_URL || 'http://localhost:8083',

  // API endpoints
  endpoints: {
    auth: {
      login: '/auth/login',
      register: '/auth/register',
      verifyEmail: '/auth/verify-email',
      resendOtp: '/auth/resend-otp',
      logout: '/auth/logout',
    },
    codeExecution: {
      run: '/api/code/run',
    },
  },
};

export default config;
