package com.example.demo.service.auth;

import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;

public interface IAuthService {

    AuthResponse login(LoginReq req);
}
