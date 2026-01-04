package com.example.demo.service.auth;

import com.example.demo.config.jwt.JwtService;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.user.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public AuthResponse login(LoginReq req) {

        // 1️⃣ Lấy user theo email
        UserEntity user = userService.getByEmail(req.getEmail())
                .orElseThrow(() ->
                        new BusinessException(UserError.INVALID_CREDENTIALS)
                );

        // 2️⃣ Check password
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BusinessException(UserError.INVALID_CREDENTIALS);
        }

        // 3️⃣ Generate JWT
        String subject = user.getEmail(); // hoặc userId
        String accessToken = jwtService.generateAccessToken(subject);
        String refreshToken = jwtService.generateRefreshToken(subject);

        // 4️⃣ Map user → response (ẩn password)
        UserResponse userResponse = UserResponseMapper.toResponse(user);

        // 5️⃣ Trả AuthResponse
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userResponse)
                .build();
    }
}
