package com.example.demo.service.redisConfig.blacklist;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AccessTokenBlacklistService {

    private final StringRedisTemplate redis;

    public void blacklist(String jti, Duration ttl) {
        redis.opsForValue().set(
                "blacklist:access:" + jti,
                "true",
                ttl
        );
    }

    public boolean isBlacklisted(String jti) {
        return Boolean.TRUE.equals(
                redis.hasKey("blacklist:access:" + jti)
        );
    }
}
