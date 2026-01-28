package com.example.demo.service.authService;

import com.example.demo.domain.dto.req.*;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;

public interface IAuthService {


    AuthResponse login(LoginReq req);
    UserResponse register(CreateUserReq req);

    void logout(String accessToken);

    void forgotPassword(ForgotPasswordReq req);

    AuthResponse refreshToken(RefreshTokenReq req);

    UserResponse verifyEmail(String token);

    // Resend verification email for given email
    UserResponse resendEmail(ResendEmailReq req);

    // 🆕 Xác nhận OTP quên mật khẩu
    String verifyForgotPasswordOtp(VerifyForgotPasswordOtpReq req);

    // 🆕 Reset mật khẩu
    void resetPassword(ResetPasswordReq req);
}
