package com.Auth.AuthService.Service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class OtpService {
    private static final Integer EXPIRE_MINUTES = 2;
    private final LoadingCache<String, OtpData> otpCache;
    private final Map<String, LocalDateTime> otpTimestamps;

    private static class OtpData {
        String otp;
        LocalDateTime timestamp;

        OtpData(String otp, LocalDateTime timestamp) {
            this.otp = otp;
            this.timestamp = timestamp;
        }
    }

    public OtpService() {
        this.otpTimestamps = new ConcurrentHashMap<>();
        otpCache = CacheBuilder.newBuilder()
                .expireAfterWrite(EXPIRE_MINUTES, TimeUnit.MINUTES)
                .removalListener(notification -> otpTimestamps.remove(notification.getKey()))
                .build(new CacheLoader<>() {
                    @Override
                    public OtpData load(String key) {
                        return null;
                    }
                });
    }

    public String generateOTP(String key) {
        String otp = String.format("%06d", (int) (Math.random() * 1000000));
        LocalDateTime now = LocalDateTime.now();
        otpCache.put(key, new OtpData(otp, now));
        otpTimestamps.put(key, now);
        return otp;
    }

    public boolean validateOTP(String key, String otpToValidate) {
        try {
            OtpData otpData = otpCache.get(key);
            if (otpData == null) {
                return false;
            }

            // Check if OTP is expired (2 minutes)
            if (LocalDateTime.now().isAfter(otpData.timestamp.plusMinutes(EXPIRE_MINUTES))) {
                clearOTP(key);
                throw new RuntimeException("OTP has expired. Please request a new one.");
            }

            if (otpData.otp.equals(otpToValidate)) {
                clearOTP(key);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public void clearOTP(String key) {
        otpCache.invalidate(key);
        otpTimestamps.remove(key);
    }

    public boolean isOtpExpired(String key) {
        LocalDateTime timestamp = otpTimestamps.get(key);
        if (timestamp == null) {
            return true;
        }
        return LocalDateTime.now().isAfter(timestamp.plusMinutes(EXPIRE_MINUTES));
    }

    // Cleanup expired OTPs
    public void cleanupExpiredOTPs() {
        otpTimestamps.forEach((key, timestamp) -> {
            if (LocalDateTime.now().isAfter(timestamp.plusMinutes(EXPIRE_MINUTES))) {
                clearOTP(key);
            }
        });
    }
}
