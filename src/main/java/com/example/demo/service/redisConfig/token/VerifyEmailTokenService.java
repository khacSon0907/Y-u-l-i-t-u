package com.example.demo.service.redisConfig.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class VerifyEmailTokenService {

    private final StringRedisTemplate redis;

    public void save(String userId, String token, Duration ttl) {
        redis.opsForValue().set(
                "verify:email:" + userId,
                token,
                ttl
        );
    }

    public String get(String userId) {
        return redis.opsForValue().get(
                "verify:email:" + userId
        );
    }

    public void delete(String userId) {
        redis.delete("verify:email:" + userId);
    }
}
