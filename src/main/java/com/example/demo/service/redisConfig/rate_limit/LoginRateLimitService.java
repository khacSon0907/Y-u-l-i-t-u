package com.example.demo.service.redisConfig.rate_limit;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class LoginRateLimitService {

    private final StringRedisTemplate redis;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(15);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    public boolean isBlocked(String identifier) {
        return Boolean.TRUE.equals(redis.hasKey("login:block:" + identifier));
    }

    public boolean recordFailed(String identifier) {
        String key = "login:attempt:" + identifier;
        Long count = redis.opsForValue().increment(key);

        if (count != null && count == 1) {
            redis.expire(key, ATTEMPT_WINDOW);
        }

        if (count != null && count >= MAX_ATTEMPTS) {
            redis.opsForValue().set(
                    "login:block:" + identifier,
                    "blocked",
                    BLOCK_DURATION
            );
            redis.delete(key);
            return true;
        }
        return false;
    }

    public void clear(String identifier) {
        redis.delete("login:attempt:" + identifier);
    }

    public long getBlockRemaining(String identifier) {
        Long ttl = redis.getExpire("login:block:" + identifier);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
