package com.example.demo.service.authService;

import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.req.*;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.auth.AuthError;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.emailService.AsyncEmailService;
import com.example.demo.service.redisConfig.RedisFacade;
import com.example.demo.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisFacade redis;
    private final AsyncEmailService asyncEmailService;

    // =====================================================
    // 🆕 REGISTER
    // =====================================================
    @Override
    public UserResponse register(CreateUserReq req) {

        UserResponse user = userService.createUser(req);

        String verifyToken = jwtService.generateVerifyToken(user.getId());

        redis.verifyEmailToken.save(
                user.getId(),
                verifyToken,
                jwtService.getVerifyTokenExpiration()
        );

        asyncEmailService.sendVerifyEmailAsync(user.getEmail(), verifyToken);

        return user;
    }

    // =====================================================
    // 🔁 RESEND VERIFY EMAIL
    // =====================================================
    @Override
    public UserResponse resendEmail(ResendEmailReq req) {

        String email = req.getEmail().trim();

        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            return UserResponseMapper.toResponse(user);
        }

        String existingToken = redis.verifyEmailToken.get(user.getId());
        if (existingToken != null) {
            return UserResponseMapper.toResponse(user);
        }

        String newToken = jwtService.generateVerifyToken(user.getId());

        redis.verifyEmailToken.save(
                user.getId(),
                newToken,
                jwtService.getVerifyTokenExpiration()
        );

        asyncEmailService.sendVerifyEmailAsync(user.getEmail(), newToken);

        return UserResponseMapper.toResponse(user);
    }

    // =====================================================
    // 🔐 LOGIN (RATE LIMIT)
    // =====================================================
    @Override
    public AuthResponse login(LoginReq req) {

        String email = req.getEmail().trim();

        if (redis.loginRateLimit.isBlocked(email)) {
            long retryAfter = redis.loginRateLimit.getBlockRemaining(email);
            throw new BusinessException(
                    AuthError.TOO_MANY_LOGIN_ATTEMPTS,
                    "Đăng nhập sai quá nhiều. Thử lại sau " + retryAfter + " giây.",
                    retryAfter
            );
        }

        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> {
                    handleFailedLogin(email);
                    return new BusinessException(UserError.INVALID_CREDENTIALS);
                });

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            handleFailedLogin(email);
            throw new BusinessException(UserError.INVALID_CREDENTIALS);
        }

        if (!user.isEmailVerified()) {
            throw new BusinessException(UserError.EMAIL_NOT_VERIFIED);
        }

        redis.loginRateLimit.clear(email);

        String userId = user.getId();
        List<String> roles = List.of("ROLE_" + user.getRole().name());

        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = jwtService.generateRefreshToken(userId);

        redis.refreshToken.save(
                userId,
                refreshToken,
                jwtService.getRefreshTokenExpiration()
        );

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponseMapper.toResponse(user))
                .build();
    }

    private void handleFailedLogin(String email) {

        boolean blocked = redis.loginRateLimit.recordFailed(email);

        if (blocked) {
            long retryAfter = redis.loginRateLimit.getBlockRemaining(email);
            throw new BusinessException(
                    AuthError.TOO_MANY_LOGIN_ATTEMPTS,
                    "Đăng nhập sai quá nhiều. Thử lại sau " + retryAfter + " giây.",
                    retryAfter
            );
        }
    }

    // =====================================================
    // 🔐 FORGOT PASSWORD (SEND OTP)
    // =====================================================
    @Override
    public void forgotPassword(ForgotPasswordReq req) {

        String email = req.getEmail().trim();

        userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        String otp = generateOtp();

        redis.forgotPasswordOtp.save(
                email,
                otp,
                Duration.ofMinutes(5)
        );

        asyncEmailService.sendForgotPasswordOtpAsync(email, otp);
    }

    // =====================================================
    // 🔐 VERIFY OTP (RATE LIMIT)
    // =====================================================
    @Override
    public String verifyForgotPasswordOtp(VerifyForgotPasswordOtpReq req) {

        String email = req.getEmail().trim();
        String otp = req.getOtp().trim();

        if (redis.otpRateLimit.isBlocked(email)) {
            long retryAfter = redis.otpRateLimit.getBlockRemaining(email);
            throw new BusinessException(
                    AuthError.TOO_MANY_OTP_ATTEMPTS,
                    "Nhập OTP sai quá nhiều. Thử lại sau " + retryAfter + " giây.",
                    retryAfter
            );
        }

        String storedOtp = redis.forgotPasswordOtp.get(email);

        if (storedOtp == null || !storedOtp.equals(otp)) {

            boolean blocked = redis.otpRateLimit.recordFailed(email);

            if (blocked) {
                long retryAfter = redis.otpRateLimit.getBlockRemaining(email);
                throw new BusinessException(
                        AuthError.TOO_MANY_OTP_ATTEMPTS,
                        "Nhập OTP sai quá nhiều. Thử lại sau " + retryAfter + " giây.",
                        retryAfter
                );
            }

            throw new BusinessException(AuthError.INVALID_OTP);
        }

        redis.otpRateLimit.clear(email);
        redis.forgotPasswordOtp.delete(email);

        String resetToken = jwtService.generateResetPasswordToken(email);

        redis.resetPasswordToken.save(
                email,
                resetToken,
                Duration.ofMinutes(10)
        );

        return resetToken;
    }

    // =====================================================
    // 🔐 RESET PASSWORD
    // =====================================================
    @Override
    public void resetPassword(ResetPasswordReq req) {

        String resetToken = req.getResetToken();

        if (!jwtService.validateToken(resetToken)) {
            throw new BusinessException(AuthError.INVALID_RESET_TOKEN);
        }

        String email = jwtService.extractEmail(resetToken);

        String storedToken = redis.resetPasswordToken.get(email);
        if (storedToken == null || !storedToken.equals(resetToken)) {
            throw new BusinessException(AuthError.INVALID_RESET_TOKEN);
        }

        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userService.save(user);

        redis.resetPasswordToken.delete(email);
    }

    // =====================================================
    // 🔐 VERIFY EMAIL
    // =====================================================
    @Override
    public UserResponse verifyEmail(String token) {

        if (!jwtService.validateToken(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        String userId = jwtService.extractUserId(token);

        String storedToken = redis.verifyEmailToken.get(userId);
        if (storedToken == null || !storedToken.equals(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        UserResponse response = userService.verifyEmail(userId);
        redis.verifyEmailToken.delete(userId);

        return response;
    }

    // =====================================================
    // 🚪 LOGOUT
    // =====================================================
    @Override
    public void logout(String accessToken) {

        String jti = jwtService.extractJti(accessToken);
        redis.accessTokenBlacklist.blacklist(
                jti,
                jwtService.getRemainingDuration(accessToken)
        );



        String userId = jwtService.extractUserId(accessToken);
        redis.refreshToken.delete(userId);
    }

    // =====================================================
    // 🔁 REFRESH TOKEN
    // =====================================================
    @Override
    public AuthResponse refreshToken(RefreshTokenReq req) {

        String refreshToken = req.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException(AuthError.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtService.extractUserId(refreshToken);

        String storedToken = redis.refreshToken.get(userId);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(AuthError.REFRESH_TOKEN_NOT_FOUND);
        }

        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        List<String> roles = List.of("ROLE_" + user.getRole().name());

        String newAccessToken = jwtService.generateAccessToken(userId, roles);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        redis.refreshToken.save(
                userId,
                newRefreshToken,
                jwtService.getRefreshTokenExpiration()
        );

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserResponseMapper.toResponse(user))
                .build();
    }

    // =====================================================
    // 🔢 UTIL
    // =====================================================
    private String generateOtp() {
        return String.valueOf((int) (Math.random() * 900000) + 100000);
    }
}
