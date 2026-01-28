package com.example.demo.service.redisConfig.token;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ResetPasswordTokenService {

    private final StringRedisTemplate redis;

    public void save(String email, String token, Duration ttl) {
        redis.opsForValue().set(
                "forgot:password:reset:" + email,
                token,
                ttl
        );
    }

    public String get(String email) {
        return redis.opsForValue().get(
                "forgot:password:reset:" + email
        );
    }

    public void delete(String email) {
        redis.delete("forgot:password:reset:" + email);
    }
}
