package com.example.demo.service.auth;

import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.redis.RedisService;
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

    // =========================
    // 🆕 REGISTER
    // =========================
    @Override
    public UserResponse register(CreateUserReq req) {
        return userService.createUser(req);
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

        // 3️⃣ Chuẩn production: subject = userId
        String userId = user.getId();

        // 4️⃣ Convert role → Spring Security format
        // VD: USER → ROLE_USER
        List<String> roles = List.of(
                "ROLE_" + user.getRole().name()
        );

        // 5️⃣ Generate token
        String accessToken = jwtService.generateAccessToken(userId, roles);
        String refreshToken = jwtService.generateRefreshToken(userId);
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

    @Override
    public UserResponse updateUser(String userId, UpdateUserReq req) {
        return userService.updateUser(userId, req);
    }
}
