package com.example.demo.controller.auth;

import com.example.demo.domain.dto.req.*;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.service.authService.IAuthService;
import com.example.demo.share.response.ApiResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.domain:localhost}")
    private String cookieDomain;

    @Value("${app.cookie.access-token-max-age:900}")  // 15 phút
    private int accessTokenMaxAge;

    @Value("${app.cookie.refresh-token-max-age:604800}")  // 7 ngày
    private int refreshTokenMaxAge;

    // =========================
    // 🆕 REGISTER
    // =========================
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> register(
            @Valid @RequestBody CreateUserReq req,
            HttpServletRequest request
    ) {
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

    // =========================
    // 🔐 LOGIN - Set HttpOnly Cookies
    // =========================
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(
            @Valid @RequestBody LoginReq req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthResponse authResponse = authService.login(req);

        // 🍪 Set Access Token Cookie
        addCookie(response, "access_token", authResponse.getAccessToken(), accessTokenMaxAge);

        // 🍪 Set Refresh Token Cookie
        addCookie(response, "refresh_token", authResponse.getRefreshToken(), refreshTokenMaxAge);

        // ⚠️ Không trả token trong response body nữa - chỉ trả user info
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.LOGIN_SUCCESS",
                "Login successfully",
                authResponse,  // Chỉ trả user, không trả tokens
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    // =========================
    // 🔁 REFRESH TOKEN - Đọc từ Cookie
    // =========================
    @PostMapping("/refresh-token")
    public ApiResponse<UserResponse> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new IllegalArgumentException("Refresh token is missing");
        }

        RefreshTokenReq req = new RefreshTokenReq(refreshToken);
        AuthResponse authResponse = authService.refreshToken(req);

        // 🍪 Set new Access Token Cookie
        addCookie(response, "access_token", authResponse.getAccessToken(), accessTokenMaxAge);

        // 🍪 Set new Refresh Token Cookie (rotation)
        addCookie(response, "refresh_token", authResponse.getRefreshToken(), refreshTokenMaxAge);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.REFRESH_TOKEN_SUCCESS",
                "Refresh token successfully",
                authResponse.getUser(),
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    // =========================
    // 🚪 LOGOUT - Clear Cookies
    // =========================
    @PostMapping("/logout")
    public ApiResponse<Void> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // Logout trên server (blacklist token)
        if (accessToken != null && !accessToken.isBlank()) {
            authService.logout(accessToken);
        }

        // 🍪 Clear cookies
        clearCookie(response, "access_token");
        clearCookie(response, "refresh_token");

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.LOGOUT_SUCCESS",
                "Logout successfully",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    // =========================
    // 📧 RESEND EMAIL
    // =========================
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

    // =========================
    // 🔑 FORGOT PASSWORD
    // =========================
    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(
            @Valid @RequestBody ForgotPasswordReq req,
            HttpServletRequest request
    ) {
        authService.forgotPassword(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.FORGOT_PASSWORD_OTP_SENT",
                "OTP has been sent to your email",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }
    // =========================
// 🔐 VERIFY FORGOT PASSWORD OTP
// =========================
    @PostMapping("/verify-forgot-password-otp")
    public ApiResponse<String> verifyForgotPasswordOtp(
            @Valid @RequestBody VerifyForgotPasswordOtpReq req,
            HttpServletRequest request
    ) {
        String resetToken = authService.verifyForgotPasswordOtp(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.FORGOT_PASSWORD_OTP_VERIFIED",
                "OTP verified successfully",
                resetToken, // ⚠️ token dùng để reset password
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }
    // =========================
// 🔁 RESET PASSWORD
// =========================
    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(
            @Valid @RequestBody ResetPasswordReq req,
            HttpServletRequest request
    ) {
        authService.resetPassword(req);

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "AUTH.RESET_PASSWORD_SUCCESS",
                "Password reset successfully",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }


    // =========================
    // 🍪 COOKIE HELPER METHODS
    // =========================

    /**
     * Add HttpOnly Secure Cookie
     */
    private void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);           // 🔒 JS không đọc được
        cookie.setSecure(cookieSecure);     // 🔒 Chỉ gửi qua HTTPS (production)
        cookie.setPath("/");                // 🌐 Có hiệu lực toàn app
        cookie.setMaxAge(maxAge);           // ⏰ Thời gian sống
        cookie.setAttribute("SameSite", "Strict");  // 🛡️ Chống CSRF

        // Set domain nếu cần (cho subdomain)
        if (cookieDomain != null && !cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }


    private void clearCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(cookieSecure);
        cookie.setPath("/");
        cookie.setMaxAge(0);  // 🗑️ Xóa cookie
        cookie.setAttribute("SameSite", "Strict");

        if (cookieDomain != null && !cookieDomain.equals("localhost")) {
            cookie.setDomain(cookieDomain);
        }

        response.addCookie(cookie);
    }
}