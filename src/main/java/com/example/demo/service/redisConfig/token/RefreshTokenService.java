package com.example.demo.service.redisConfig.token;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    private static final String PREFIX = "refresh:";

    /**
     * Save refresh token
     */
    public void save(String userId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(
                PREFIX + userId,
                refreshToken,
                ttl
        );
    }

    /**
     * Get refresh token
     */
    public String get(String userId) {
        return redisTemplate.opsForValue().get(PREFIX + userId);
    }

    /**
     * Delete refresh token
     */
    public void delete(String userId) {
        redisTemplate.delete(PREFIX + userId);
    }
}
