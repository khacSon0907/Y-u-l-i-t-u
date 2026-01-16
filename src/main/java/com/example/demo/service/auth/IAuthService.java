package com.example.demo.service.auth;

import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.req.RefreshTokenReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;

public interface IAuthService {

    AuthResponse login(LoginReq req);
    UserResponse register(CreateUserReq req);

    UserResponse updateUser(String userId, UpdateUserReq req);

    void logout(String accessToken);

    AuthResponse refreshToken(RefreshTokenReq req);

    UserResponse verifyEmail(String token);
}
