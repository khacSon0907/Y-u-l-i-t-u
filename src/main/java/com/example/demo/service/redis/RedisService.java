package com.example.demo.service.redis;



import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    // 🔒 BLACKLIST ACCESS TOKEN
    public void blacklistAccessToken(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "blacklist:access:" + token,
                "true",
                Duration.ofMillis(ttlMillis)
        );
    }

    public boolean isAccessTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(
                redisTemplate.hasKey("blacklist:access:" + token)
        );
    }

    // 🔁 REFRESH TOKEN
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
}
