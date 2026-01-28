package com.example.demo.service.redisConfig.rate_limit;


import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OtpRateLimitService {

    private final StringRedisTemplate redis;

    private static final int MAX_ATTEMPTS = 5;
    private static final Duration ATTEMPT_WINDOW = Duration.ofMinutes(5);
    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    public boolean isBlocked(String email) {
        return Boolean.TRUE.equals(redis.hasKey("otp:block:" + email));
    }

    public boolean recordFailed(String email) {
        String key = "otp:attempt:" + email;
        Long count = redis.opsForValue().increment(key);

        if (count != null && count == 1) {
            redis.expire(key, ATTEMPT_WINDOW);
        }

        if (count != null && count >= MAX_ATTEMPTS) {
            redis.opsForValue().set(
                    "otp:block:" + email,
                    "blocked",
                    BLOCK_DURATION
            );
            redis.delete(key);
            return true;
        }
        return false;
    }

    public void clear(String email) {
        redis.delete("otp:attempt:" + email);
    }

    public long getBlockRemaining(String email) {
        Long ttl = redis.getExpire("otp:block:" + email);
        return ttl != null && ttl > 0 ? ttl : 0;
    }
}
