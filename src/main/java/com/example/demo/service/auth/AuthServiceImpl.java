package com.example.demo.service.auth;

import com.example.demo.exception.auth.AuthError;
import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.req.RefreshTokenReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
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
        // Create user (UserService will normalize username)
        UserResponse user = userService.createUser(req);

        // Generate verification token and send verification email
        String verifyToken = jwtService.generateVerifyToken(user.getId());
        // Do not swallow exceptions; let them propagate so caller can handle/report them
        emailService.sendVerifyEmail(user.getEmail(), verifyToken);

        return user;
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
}
