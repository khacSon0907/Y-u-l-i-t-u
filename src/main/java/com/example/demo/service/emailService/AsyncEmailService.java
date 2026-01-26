package com.example.demo.service.emailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncEmailService {

    private final IEmailService emailService;

    /**
     * 📧 Gửi email verify - ASYNC (non-blocking)
     *
     * @param to    Email người nhận
     * @param token Verify token
     */
    @Async("emailTaskExecutor")
    public void sendVerifyEmailAsync(String to, String token) {

        String threadName = Thread.currentThread().getName();
        log.info("[ASYNC-{}] 🚀 Start sending verify email to: {}", threadName, to);

        long startTime = System.currentTimeMillis();

        try {
            emailService.sendVerifyEmail(to, token);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[ASYNC-{}] ✅ Verify email sent successfully to: {} (took {}ms)",
                    threadName, to, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[ASYNC-{}] ❌ Failed to send verify email to: {} (took {}ms). Error: {}",
                    threadName, to, duration, e.getMessage(), e);

            // TODO: Có thể save vào DB để retry sau
            // emailRetryRepository.save(new EmailRetry(to, token, "VERIFY", e.getMessage()));
        }
    }

    /**
     * 📧 Gửi OTP forgot password - ASYNC (non-blocking)
     *
     * @param to  Email người nhận
     * @param otp Mã OTP
     */
    @Async("emailTaskExecutor")
    public void sendForgotPasswordOtpAsync(String to, String otp) {

        String threadName = Thread.currentThread().getName();
        log.info("[ASYNC-{}] 🚀 Start sending OTP to: {}", threadName, to);

        long startTime = System.currentTimeMillis();

        try {
            emailService.sendForgotPasswordOtp(to, otp);

            long duration = System.currentTimeMillis() - startTime;
            log.info("[ASYNC-{}] ✅ OTP sent successfully to: {} (took {}ms)",
                    threadName, to, duration);

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("[ASYNC-{}] ❌ Failed to send OTP to: {} (took {}ms). Error: {}",
                    threadName, to, duration, e.getMessage(), e);
        }
    }
}