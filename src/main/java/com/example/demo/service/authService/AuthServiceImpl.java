package com.example.demo.service.authService;

import com.example.demo.exception.auth.AuthError;
import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.req.RefreshTokenReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.req.ResendEmailReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.emailService.IEmailService;
import com.example.demo.service.redisConfig.RedisService;
import com.example.demo.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisService redisService;
    private  final IEmailService emailService;

    // =========================
    // 🆕 REGISTER
    // =========================
    @Override
    public UserResponse register(CreateUserReq req) {

        try {
            // 1️⃣ Thử tạo user mới
            UserResponse user = userService.createUser(req);

            // 👉 User mới → gửi verify lần đầu
            sendVerifyEmail(user.getId(), user.getEmail());
            return user;

        } catch (BusinessException ex) {

            // 2️⃣ Nếu email chưa verify → xử lý resend
            if (ex.getError() == UserError.EMAIL_NOT_VERIFIED) {

                UserEntity user = userService.getByEmail(req.getEmail())
                        .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

                handleResendVerifyEmail(user);

                throw ex; // vẫn trả EMAIL_NOT_VERIFIED cho FE
            }

            throw ex;
        }
    }


    @Override
    public UserResponse verifyEmail(String token) {

        // 1️⃣ Validate JWT
        if (token == null || !jwtService.validateToken(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        // 2️⃣ Check purpose
        String purpose = jwtService.extractPurpose(token);
        if (!"verify".equals(purpose)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        // 3️⃣ Extract userId
        String userId = jwtService.extractUserId(token);

        // 4️⃣ Check token trong Redis
        String storedToken = redisService.getVerifyEmailToken(userId);
        if (storedToken == null || !storedToken.equals(token)) {
            throw new BusinessException(AuthError.INVALID_VERIFY_TOKEN);
        }

        // 5️⃣ Verify email trong DB
        UserResponse response = userService.verifyEmail(userId);

        // 6️⃣ Xóa token khỏi Redis (chống reuse)
        redisService.deleteVerifyEmailToken(userId);

        return response;
    }


    // =========================
    // 🔐 LOGIN
    // =========================
    @Override
    public AuthResponse login(LoginReq req) {

        // 1️⃣ Tìm user theo email
        UserEntity user = userService.getByEmail(req.getEmail())
                .orElseThrow(() ->
                        new BusinessException(UserError.INVALID_CREDENTIALS)
                );

        // 2️⃣ Check password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(UserError.INVALID_CREDENTIALS);
        }

        String userId = user.getId();

        // 3️⃣ Role → ROLE_*
        List<String> roles = List.of(
                "ROLE_" + user.getRole().name()
        );

        // 4️⃣ Generate tokens
        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = jwtService.generateRefreshToken(userId);

        // 5️⃣ Lưu refresh token vào Redis
        redisService.saveRefreshToken(
                userId,
                refreshToken,
                jwtService.getRefreshTokenExpiration()
        );

        // 6️⃣ Response
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(UserResponseMapper.toResponse(user))
                .build();
    }

    // =========================
    // 🚪 LOGOUT (CHUẨN)
    // =========================
    @Override
    public void logout(String accessToken) {

        // 1️⃣ Extract jti + ttl
        String jti = jwtService.extractJti(accessToken);
        long ttlMillis = jwtService.getRemainingTime(accessToken);

        // 2️⃣ Blacklist access token
        redisService.blacklistAccessToken(jti, ttlMillis);

        // 3️⃣ Xóa refresh token
        String userId = jwtService.extractUserId(accessToken);
        redisService.deleteRefreshToken(userId);
    }

    // =========================
    // 🔁 REFRESH TOKEN
    // =========================

    @Override
    public AuthResponse refreshToken(RefreshTokenReq req) {

        String refreshToken = req.getRefreshToken();

        // 1️⃣ Validate refresh token
        if (!jwtService.validateToken(refreshToken)) {
            throw new BusinessException(AuthError.INVALID_REFRESH_TOKEN);
        }

        // 2️⃣ Extract userId
        String userId = jwtService.extractUserId(refreshToken);

        // 3️⃣ Check refresh token trong Redis
        String storedRefreshToken = redisService.getRefreshToken(userId);
        if (storedRefreshToken == null || !storedRefreshToken.equals(refreshToken)) {
            throw new BusinessException(AuthError.REFRESH_TOKEN_NOT_FOUND);
        }

        // 4️⃣ Lấy user
        UserEntity user = userService.getUserById(userId)
                .orElseThrow(() ->
                        new BusinessException(UserError.USER_NOT_FOUND)
                );

        // 5️⃣ Roles
        List<String> roles = List.of(
                "ROLE_" + user.getRole().name()
        );

        // 6️⃣ Generate token mới
        String newAccessToken = jwtService.generateAccessToken(userId, roles);
        String newRefreshToken = jwtService.generateRefreshToken(userId);

        // 7️⃣ Update Redis
        redisService.saveRefreshToken(
                userId,
                newRefreshToken,
                jwtService.getRefreshTokenExpiration()
        );

        // 8️⃣ Response
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .user(UserResponseMapper.toResponse(user))
                .build();
    }

    // =========================
    // ✏️ UPDATE USER
    // =========================
    @Override
    public UserResponse updateUser(String userId, UpdateUserReq req) {
        return userService.updateUser(userId, req);
    }
    private void handleResendVerifyEmail(UserEntity user) {

        String userId = user.getId();

        // 🔍 Check token cũ trong Redis
        String existingToken = redisService.getVerifyEmailToken(userId);

        if (existingToken != null) {
            // ✅ Token còn hạn → KHÔNG gửi lại
            return;
        }

        // 🔁 Token hết hạn → tạo token mới
        sendVerifyEmail(userId, user.getEmail());
    }

    private void sendVerifyEmail(String userId, String email) {

        String verifyToken = jwtService.generateVerifyToken(userId);

        redisService.saveVerifyEmailToken(
                userId,
                verifyToken,
                jwtService.getVerifyTokenExpiration()
        );

        emailService.sendVerifyEmail(email, verifyToken);
    }

    @Override
    public UserResponse resendEmail(ResendEmailReq req) {
        if (req == null || req.getEmail() == null) {
            throw new BusinessException(UserError.INVALID_EMAIL);
        }

        String email = req.getEmail().trim();

        UserEntity user = userService.getByEmail(email)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        if (user.isEmailVerified()) {
            // already verified -> return current user response
            return UserResponseMapper.toResponse(user);
        }

        // reuse existing resend handling
        handleResendVerifyEmail(user);

        return UserResponseMapper.toResponse(user);
    }

}
