import { useState, useEffect, useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../contexts/AuthContext';
import { toast } from 'react-hot-toast';

const OTPVerification = ({ email, onVerificationComplete, onResendClick }) => {
  const [otp, setOtp] = useState(['', '', '', '', '', '']);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [resendDisabled, setResendDisabled] = useState(true);
  const [countdown, setCountdown] = useState(120); // 2 minutes in seconds
  const { verifyEmail, resendOtp } = useAuth();
  const inputRefs = useRef([]);
  const navigate = useNavigate();

  // Handle OTP input change
  const handleChange = (index, value) => {
    if (isNaN(value)) return;

    const newOtp = [...otp];
    newOtp[index] = value;
    setOtp(newOtp);

    // Auto-focus to next input
    if (value && index < 5) {
      inputRefs.current[index + 1].focus();
    }
  };

  // Handle backspace
  const handleKeyDown = (index, e) => {
    if (e.key === 'Backspace' && !otp[index] && index > 0) {
      inputRefs.current[index - 1].focus();
    }
  };

  // Handle paste
  const handlePaste = (e) => {
    e.preventDefault();
    const pastedData = e.clipboardData.getData('text/plain').trim();
    if (/^\d{6}$/.test(pastedData)) {
      const otpArray = pastedData.split('').slice(0, 6);
      setOtp(otpArray);
      inputRefs.current[5].focus();
    }
  };

  // Handle form submission
  const handleSubmit = async (e) => {
    e.preventDefault();
    const otpCode = otp.join('');
    
    if (otpCode.length !== 6) {
      toast.error('Please enter a valid 6-digit OTP');
      return;
    }

    setIsSubmitting(true);
    const { success, message, error } = await verifyEmail(email, otpCode);
    
    if (success) {
      toast.success(message || 'Email verified successfully!');
      if (onVerificationComplete) {
        onVerificationComplete();
      }
      navigate('/login');
    } else {
      toast.error(error || 'Verification failed. Please try again.');
    }
    
    setIsSubmitting(false);
  };

  // Handle resend OTP
  const handleResendOTP = async () => {
    const { success, message, error } = await resendOtp(email);
    
    if (success) {
      toast.success(message || 'New OTP sent successfully!');
      setCountdown(120); // Reset countdown
      setResendDisabled(true);
    } else {
      toast.error(error || 'Failed to resend OTP. Please try again.');
    }
  };

  // Countdown timer for resend OTP
  useEffect(() => {
    let timer;
    if (countdown > 0) {
      timer = setTimeout(() => setCountdown(countdown - 1), 1000);
    } else {
      setResendDisabled(false);
    }
    return () => clearTimeout(timer);
  }, [countdown]);

  // Format seconds to MM:SS
  const formatTime = (seconds) => {
    const mins = Math.floor(seconds / 60);
    const secs = seconds % 60;
    return `${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}`;
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-gray-50 py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Verify Your Email
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            We've sent a 6-digit verification code to <span className="font-medium">{email}</span>
          </p>
        </div>
        
        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="flex justify-center space-x-2">
            {otp.map((digit, index) => (
              <input
                key={index}
                type="text"
                maxLength={1}
                value={digit}
                onChange={(e) => handleChange(index, e.target.value)}
                onKeyDown={(e) => handleKeyDown(index, e)}
                onPaste={handlePaste}
                ref={(el) => (inputRefs.current[index] = el)}
                className="w-12 h-12 text-center text-xl border border-gray-300 rounded-md focus:ring-indigo-500 focus:border-indigo-500"
                autoFocus={index === 0}
              />
            ))}
          </div>

          <div className="text-center text-sm text-gray-600">
            <p>Didn't receive the code?</p>
            <button
              type="button"
              onClick={handleResendOTP}
              disabled={resendDisabled}
              className={`mt-2 text-indigo-600 hover:text-indigo-500 font-medium ${
                resendDisabled ? 'opacity-50 cursor-not-allowed' : ''
              }`}
            >
              {resendDisabled 
                ? `Resend OTP in ${formatTime(countdown)}` 
                : 'Resend OTP'}
            </button>
          </div>

          <div>
            <button
              type="submit"
              disabled={isSubmitting}
              className={`group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 ${
                isSubmitting ? 'opacity-70 cursor-not-allowed' : ''
              }`}
            >
              {isSubmitting ? 'Verifying...' : 'Verify Email'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default OTPVerification;
