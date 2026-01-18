package com.example.demo.service.redisConfig;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // =========================
    // 🔒 BLACKLIST ACCESS TOKEN
    // =========================
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

    // =========================
    // 🔁 REFRESH TOKEN
    // =========================
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

    // =========================
    // ✉️ VERIFY EMAIL TOKEN
    // =========================
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
