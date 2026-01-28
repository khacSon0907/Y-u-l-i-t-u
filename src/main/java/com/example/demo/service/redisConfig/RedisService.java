package com.example.demo.service.redisConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // =====================================================
    // 🚫 LOGIN RATE LIMIT
    // =====================================================

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final Duration LOGIN_BLOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration LOGIN_ATTEMPT_WINDOW = Duration.ofMinutes(15);

    // =====================================================
    // 🔐 OTP VERIFY RATE LIMIT (🆕)
    // =====================================================

    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final Duration OTP_ATTEMPT_WINDOW = Duration.ofMinutes(5);
    private static final Duration OTP_BLOCK_DURATION = Duration.ofMinutes(15);

    // =====================================================
    // 🔐 RESET PASSWORD TOKEN
    // =====================================================

    public void saveResetPasswordToken(String email, String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "forgot:password:reset:" + email,
                token,
                Duration.ofMillis(ttlMillis)
        );
    }

    public String getResetPasswordToken(String email) {
        return redisTemplate.opsForValue().get(
                "forgot:password:reset:" + email
        );
    }

    public void deleteResetPasswordToken(String email) {
        redisTemplate.delete(
                "forgot:password:reset:" + email
        );
    }

    // =====================================================
    // 🚫 LOGIN RATE LIMIT
    // =====================================================

    public boolean isLoginBlocked(String identifier) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("login:block:" + identifier)
        );
    }

    public boolean recordFailedLogin(String identifier) {

        String attemptKey = "login:attempt:" + identifier;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, LOGIN_ATTEMPT_WINDOW);
        }

        if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
            redisTemplate.opsForValue().set(
                    "login:block:" + identifier,
                    "blocked",
                    LOGIN_BLOCK_DURATION
            );
            redisTemplate.delete(attemptKey);
            return true;
        }

        return false;
    }

    public void clearLoginAttempts(String identifier) {
        redisTemplate.delete("login:attempt:" + identifier);
    }

    public int getRemainingLoginAttempts(String identifier) {
        String value = redisTemplate.opsForValue().get("login:attempt:" + identifier);
        if (value == null) return MAX_LOGIN_ATTEMPTS;
        return Math.max(0, MAX_LOGIN_ATTEMPTS - Integer.parseInt(value));
    }

    public long getLoginBlockTimeRemaining(String identifier) {
        Long ttl = redisTemplate.getExpire("login:block:" + identifier);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    // =====================================================
    // 🔐 OTP VERIFY RATE LIMIT (🆕)
    // =====================================================

    public boolean isOtpBlocked(String email) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("otp:block:" + email)
        );
    }

    public boolean recordFailedOtpAttempt(String email) {

        String attemptKey = "otp:attempt:" + email;
        Long attempts = redisTemplate.opsForValue().increment(attemptKey);

        if (attempts != null && attempts == 1) {
            redisTemplate.expire(attemptKey, OTP_ATTEMPT_WINDOW);
        }

        if (attempts != null && attempts >= MAX_OTP_ATTEMPTS) {
            redisTemplate.opsForValue().set(
                    "otp:block:" + email,
                    "blocked",
                    OTP_BLOCK_DURATION
            );
            redisTemplate.delete(attemptKey);
            return true;
        }

        return false;
    }

    public void clearOtpAttempts(String email) {
        redisTemplate.delete("otp:attempt:" + email);
    }

    public int getRemainingOtpAttempts(String email) {
        String value = redisTemplate.opsForValue().get("otp:attempt:" + email);
        if (value == null) return MAX_OTP_ATTEMPTS;
        return Math.max(0, MAX_OTP_ATTEMPTS - Integer.parseInt(value));
    }

    public long getOtpBlockTimeRemaining(String email) {
        Long ttl = redisTemplate.getExpire("otp:block:" + email);
        return ttl != null && ttl > 0 ? ttl : 0;
    }

    // =====================================================
    // 🔑 FORGOT PASSWORD OTP
    // =====================================================

    public void saveForgotPasswordOtp(String email, String otp, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "forgot:password:otp:" + email,
                otp,
                Duration.ofMillis(ttlMillis)
        );
    }

    public String getForgotPasswordOtp(String email) {
        return redisTemplate.opsForValue().get(
                "forgot:password:otp:" + email
        );
    }

    public void deleteForgotPasswordOtp(String email) {
        redisTemplate.delete(
                "forgot:password:otp:" + email
        );
    }

    // =====================================================
    // 🔒 BLACKLIST ACCESS TOKEN
    // =====================================================

    public void blacklistAccessToken(String jti, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "blacklist:access:" + jti,
                "true",
                Duration.ofMillis(ttlMillis)
        );
    }

    public boolean isAccessTokenBlacklisted(String jti) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:access:" + jti)
        );
    }

    // =====================================================
    // 🔁 REFRESH TOKEN
    // =====================================================

    public void saveRefreshToken(String userId, String refreshToken, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "refresh:" + userId,
                refreshToken,
                Duration.ofMillis(ttlMillis)
        );
    }

    public String getRefreshToken(String userId) {
        return redisTemplate.opsForValue().get("refresh:" + userId);
    }

    public void deleteRefreshToken(String userId) {
        redisTemplate.delete("refresh:" + userId);
    }

    // =====================================================
    // ✉️ VERIFY EMAIL TOKEN
    // =====================================================

    public void saveVerifyEmailToken(String userId, String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "verify:email:" + userId,
                token,
                Duration.ofMillis(ttlMillis)
        );
    }

    public String getVerifyEmailToken(String userId) {
        return redisTemplate.opsForValue().get("verify:email:" + userId);
    }

    public void deleteVerifyEmailToken(String userId) {
        redisTemplate.delete("verify:email:" + userId);
    }
}
