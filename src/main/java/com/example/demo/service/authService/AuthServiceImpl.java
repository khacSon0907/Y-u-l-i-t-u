package com.example.demo.service.authService;

import com.example.demo.domain.dto.req.*;
import com.example.demo.exception.auth.AuthError;
import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.emailService.AsyncEmailService;  // 👈 THAY ĐỔI
import com.example.demo.service.redisConfig.RedisService;
import com.example.demo.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
@Slf4j  // 👈 THÊM
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;
    private final AsyncEmailService asyncEmailService;  // 👈 THAY ĐỔI: IEmailService → AsyncEmailService

    // =========================
    // 🆕 REGISTER
    // =========================
    @Override
    public UserResponse register(CreateUserReq req) {

        log.info("📝 Register request for email: {}", req.getEmail());
        long startTime = System.currentTimeMillis();

        try {
            // 1️⃣ Tạo user mới
            UserResponse user = userService.createUser(req);

            // 2️⃣ Generate token & save Redis (SYNC - nhanh)
            String verifyToken = jwtService.generateVerifyToken(user.getId());
            redisService.saveVerifyEmailToken(
                    user.getId(),
                    verifyToken,
                    jwtService.getVerifyTokenExpiration()
            );

            // 3️⃣ Gửi email ASYNC - KHÔNG CHỜ ĐỢI 🚀
            asyncEmailService.sendVerifyEmailAsync(user.getEmail(), verifyToken);

            long duration = System.currentTimeMillis() - startTime;
            log.info("✅ User registered successfully: {} (API took {}ms)", user.getId(), duration);

            return user;

        } catch (BusinessException ex) {

            // Nếu email chưa verify → xử lý resend
            if (ex.getError() == UserError.EMAIL_NOT_VERIFIED) {

                UserEntity user = userService.getByEmail(req.getEmail())
                        .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

                handleResendVerifyEmail(user);

                throw ex;
            }

            throw ex;
        }
    }

    // =========================
    // 🔁 RESEND EMAIL
    // =========================
    @Override
    public UserResponse resendEmail(ResendEmailReq req) {

        if (req == null || req.getEmail() == null) {
            throw new BusinessException(UserError.INVALID_EMAIL);
        }

        String email = req.getEmail().trim();
        log.info("📧 Resend email request for: {}", email);

        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            log.info("⚠️ Email already verified: {}", email);
            return UserResponseMapper.toResponse(user);
        }

        handleResendVerifyEmail(user);

        return UserResponseMapper.toResponse(user);
    }

    // =========================
    // 🔐 FORGOT PASSWORD
    // =========================
    @Override
    public void forgotPassword(ForgotPasswordReq req) {

        if (req == null || req.getEmail() == null || req.getEmail().isBlank()) {
            throw new BusinessException(UserError.INVALID_EMAIL);
        }

        String email = req.getEmail().trim();
        log.info("🔐 Forgot password request for: {}", email);

        // 1️⃣ Check user tồn tại
        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        // 2️⃣ Sinh OTP
        String otp = generateOtp();

        // 3️⃣ Lưu OTP vào Redis (5 phút)
        redisService.saveForgotPasswordOtp(
                email,
                otp,
                5 * 60 * 1000 // 5 phút
        );

        // 4️⃣ Gửi email OTP - ASYNC 🚀
        asyncEmailService.sendForgotPasswordOtpAsync(email, otp);

        log.info("✅ Forgot password OTP generated for: {}", email);
    }

    // =========================
    // 📧 PRIVATE METHODS
    // =========================

    private void handleResendVerifyEmail(UserEntity user) {

        String userId = user.getId();

        // Check token cũ trong Redis
        String existingToken = redisService.getVerifyEmailToken(userId);

        if (existingToken != null) {
            log.info("⏳ Token still valid for user: {}, skipping resend", userId);
            return;
        }

        // Token hết hạn → tạo token mới
        log.info("🔁 Generating new verify token for user: {}", userId);

        String verifyToken = jwtService.generateVerifyToken(userId);

        redisService.saveVerifyEmailToken(
                userId,
                verifyToken,
                jwtService.getVerifyTokenExpiration()
        );

        // Gửi email ASYNC 🚀
        asyncEmailService.sendVerifyEmailAsync(user.getEmail(), verifyToken);
    }

    private String generateOtp() {
        return String.valueOf((int)(Math.random() * 900000) + 100000);
    }

    // =========================
    // 🔐 VERIFY EMAIL (giữ nguyên)
    // =========================
    @Override
    public UserResponse verifyEmail(String token) {

        if (token == null || !jwtService.validateToken(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        String purpose = jwtService.extractPurpose(token);
        if (!"verify".equals(purpose)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        String userId = jwtService.extractUserId(token);

        String storedToken = redisService.getVerifyEmailToken(userId);
        if (storedToken == null || !storedToken.equals(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        UserResponse response = userService.verifyEmail(userId);

        redisService.deleteVerifyEmailToken(userId);

        return response;
    }

    // =========================
    // 🔐 LOGIN (giữ nguyên)
    // =========================
    @Override
    public AuthResponse login(LoginReq req) {git

        log.info("🔐 Login attempt for email: {}", req.getEmail());

        // 1️⃣ Tìm user theo email
        UserEntity user = userService.getByEmail(req.getEmail())
                .orElseThrow(() -> {
                    log.warn("❌ Login failed - Email not found: {}", req.getEmail());
                    return new BusinessException(UserError.INVALID_CREDENTIALS);
                });

        // 2️⃣ Check password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            log.warn("❌ Login failed - Wrong password for: {}", req.getEmail());
            throw new BusinessException(UserError.INVALID_CREDENTIALS);
        }

        // 3️⃣ 🆕 Check email đã verify chưa
        if (!user.isEmailVerified()) {
            log.warn("⚠️ Login failed - Email not verified: {}", req.getEmail());
            throw new BusinessException(UserError.EMAIL_NOT_VERIFIED);
        }

        // 4️⃣ Generate tokens
        String userId = user.getId();
        List<String> roles = List.of("ROLE_" + user.getRole().name());

        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = jwtService.generateRefreshToken(userId);

        // 5️⃣ Save refresh token vào Redis
        redisService.saveRefreshToken(
                userId,
                refreshToken,
                jwtService.getRefreshTokenExpiration()
        );

        log.info("✅ Login successful for user: {}", userId);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponseMapper.toResponse(user))
                .build();
    }


    // =========================
    // 🚪 LOGOUT (giữ nguyên)
    // =========================
    @Override
    public void logout(String accessToken) {

        String jti = jwtService.extractJti(accessToken);
        long ttlMillis = jwtService.getRemainingTime(accessToken);

        redisService.blacklistAccessToken(jti, ttlMillis);

        String userId = jwtService.extractUserId(accessToken);
        redisService.deleteRefreshToken(userId);
    }

    // =========================
    // 🔁 REFRESH TOKEN (giữ nguyên)
    // =========================
    @Override
    public AuthResponse refreshToken(RefreshTokenReq req) {

        String refreshToken = req.getRefreshToken();

        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException(AuthError.INVALID_REFRESH_TOKEN);
        }

        String userId = jwtService.extractUserId(refreshToken);

        String storedRefreshToken = redisService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(AuthError.REFRESH_TOKEN_NOT_FOUND);
        }

        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() ->
                        new BusinessException(UserError.USER_NOT_FOUND)
                );

        List<String> roles = List.of(
                "ROLE_" + user.getRole().name()
        );

        String newAccessToken = jwtService.generateAccessToken(userId, roles);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        redisService.saveRefreshToken(
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
}
