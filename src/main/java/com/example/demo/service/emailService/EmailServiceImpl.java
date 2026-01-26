package com.example.demo.service.emailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
 @Service
@RequiredArgsConstructor
public class EmailServiceImpl implements IEmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.backend-url}")
    private String backendUrl;

    @Value("${app.mail.verify-endpoint}")
    private String verifyEndpoint;

    @Override
    public void sendForgotPasswordOtp(String to, String otp) {

        log.info("Preparing to send forgot password OTP email to={}, otp={}", to, otp);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Mã OTP đặt lại mật khẩu");

            String htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body {
                        font-family: Arial, sans-serif;
                        background-color: #f4f6f8;
                        padding: 20px;
                    }
                    .container {
                        max-width: 500px;
                        margin: auto;
                        background: #ffffff;
                        padding: 30px;
                        border-radius: 8px;
                        box-shadow: 0 4px 10px rgba(0,0,0,0.1);
                        text-align: center;
                    }
                    .otp {
                        font-size: 28px;
                        font-weight: bold;
                        letter-spacing: 6px;
                        color: #ff5722;
                        margin: 20px 0;
                    }
                    .footer {
                        margin-top: 30px;
                        font-size: 12px;
                        color: #888888;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h2>Đặt lại mật khẩu 🔐</h2>
                    <p>Bạn đã yêu cầu đặt lại mật khẩu cho tài khoản <b>TroSmart</b>.</p>
                    <p>Mã OTP của bạn là:</p>

                    <div class="otp">%s</div>

                    <p>Mã có hiệu lực trong <b>5 phút</b>.</p>

                    <div class="footer">
                        <p>Nếu bạn không yêu cầu thao tác này, hãy bỏ qua email.</p>
                    </div>
                </div>
            </body>
            </html>
        """.formatted(otp);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Forgot password OTP email sent to {} successfully", to);

        } catch (Exception e) {
            log.error("Failed to send forgot password OTP email to {}. exception: {}", to, e.toString(), e);
            throw new RuntimeException("Gửi OTP đặt lại mật khẩu thất bại", e);
        }
    }


    @Override
    public void sendVerifyEmail(String to, String token) {

        String verifyLink = backendUrl + verifyEndpoint + "?token=" + token;

        log.info("Preparing to send verify email to={}, link={}", to, verifyLink);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject("Xác nhận email");

            String htmlContent = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background-color: #f4f6f8;
                            padding: 20px;
                        }
                        .container {
                            max-width: 500px;
                            margin: auto;
                            background: #ffffff;
                            padding: 30px;
                            border-radius: 8px;
                            box-shadow: 0 4px 10px rgba(0,0,0,0.1);
                            text-align: center;
                        }
                        .btn {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 12px 24px;
                            background-color: #4CAF50;
                            color: #ffffff;
                            text-decoration: none;
                            border-radius: 5px;
                            font-weight: bold;
                        }
                        .footer {
                            margin-top: 30px;
                            font-size: 12px;
                            color: #888888;
                        }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <h2>Chào bạn 👋</h2>
                        <p>Cảm ơn bạn đã đăng ký tài khoản tại <b>TroSmart</b>.</p>
                        <p>Vui lòng nhấn nút bên dưới để xác nhận email:</p>

                        <a href="%s" class="btn">Xác nhận email</a>

                        <div class="footer">
                            <p>Nếu bạn không tạo tài khoản, vui lòng bỏ qua email này.</p>
                        </div>
                    </div>
                </body>
                </html>
            """.formatted(verifyLink);

            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("Verify email sent to {} successfully (link={})", to, verifyLink);

        } catch (Exception e) {
            log.error("Failed to send verify email to {} (link={}). exception: {}", to, verifyLink, e.toString(), e);
            throw new RuntimeException("Gửi email xác nhận thất bại", e);
        }




    }
}
