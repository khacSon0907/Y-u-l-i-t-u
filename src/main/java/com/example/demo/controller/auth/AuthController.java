package com.example.demo.controller.auth;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.req.RefreshTokenReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.req.ResendEmailReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.service.authService.IAuthService;
import com.example.demo.share.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/authService")
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody CreateUserReq req,
            HttpServletRequest request
    )
    {
        UserResponse userResponse = authService.register(req);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "AUTH.REGISTER_SUCCESS",
                "Register successfully",
                userResponse,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginReq req,
            HttpServletRequest request
    )
    {
        AuthResponse authResponse = authService.login(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.LOGIN_SUCCESS",
                "Login successfully",
                authResponse,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PostMapping("/refresh-token")
    public ApiResponse<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenReq req,
            HttpServletRequest request
    )
    {
        AuthResponse authResponse = authService.refreshToken(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.REFRESH_TOKEN_SUCCESS",
                "Refresh token successfully",
                authResponse,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PutMapping("/user/{userId}")
    public ApiResponse<UserResponse> updateUser(
            @PathVariable("userId") String userId,
            @Valid @RequestBody UpdateUserReq req,
            HttpServletRequest request
    ) {
        UserResponse userResponse = authService.updateUser(userId, req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.UPDATE_SUCCESS",
                "Update user successfully",
                userResponse,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request
    ) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Missing or invalid Authorization header");
        }

        String token = authorization.substring(7);
        authService.logout(token);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.LOGOUT_SUCCESS",
                "Logout successfully",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PostMapping("/resend-email")
    public ApiResponse<UserResponse> resendEmail(
            @Valid @RequestBody ResendEmailReq req,
            HttpServletRequest request
    ) {
        UserResponse userResponse = authService.resendEmail(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.RESEND_EMAIL_SENT",
                "Resend verification email successfully",
                userResponse,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }
}
