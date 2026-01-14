package com.example.demo.service.emailService;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
    public void sendVerifyEmail(String to, String token) {

        String verifyLink = backendUrl + verifyEndpoint + "?token=" + token;

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setFrom(from);
        mail.setTo(to);
        mail.setSubject("Xác nhận email");
        mail.setText(
                "Vui lòng xác nhận email bằng link sau:\n" + verifyLink
        );

        mailSender.send(mail);
    }
}
