package com.example.demo.service.redisConfig;

import com.example.demo.service.redisConfig.blacklist.AccessTokenBlacklistService;
import com.example.demo.service.redisConfig.otp.ForgotPasswordOtpService;
import com.example.demo.service.redisConfig.rate_limit.LoginRateLimitService;
import com.example.demo.service.redisConfig.rate_limit.OtpRateLimitService;
import com.example.demo.service.redisConfig.token.RefreshTokenService;
import com.example.demo.service.redisConfig.token.ResetPasswordTokenService;
import com.example.demo.service.redisConfig.token.VerifyEmailTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisFacade {

    public final LoginRateLimitService loginRateLimit;
    public final OtpRateLimitService otpRateLimit;

    public final ForgotPasswordOtpService forgotPasswordOtp;

    public final RefreshTokenService refreshToken;   // ✅ BỔ SUNG
    public final ResetPasswordTokenService resetPasswordToken;
    public final VerifyEmailTokenService verifyEmailToken;

    public final AccessTokenBlacklistService accessTokenBlacklist;
}
