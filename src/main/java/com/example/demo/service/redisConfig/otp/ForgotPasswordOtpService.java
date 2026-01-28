package com.example.demo.service.redisConfig.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class ForgotPasswordOtpService {

    private final StringRedisTemplate redis;

    public void save(String email, String otp, Duration ttl) {
        redis.opsForValue().set(
                "forgot:password:otp:" + email,
                otp,
                ttl
        );
    }

    public String get(String email) {
        return redis.opsForValue().get(
                "forgot:password:otp:" + email
        );
    }

    public void delete(String email) {
        redis.delete("forgot:password:otp:" + email);
    }
}
