package com.example.demo.service.emailService;

public interface IEmailService {

    void sendVerifyEmail(String to, String token);

    // 🆕 Forgot password OTP
    void sendForgotPasswordOtp(String to, String otp);


}
