package com.example.demo.controller.auth;

import com.example.demo.service.authService.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/authService")
@RequiredArgsConstructor
public class VerifyController {

    private final IAuthService authService;

    @GetMapping("/verify")
    public String verifyEmail(@RequestParam("token") String token, Model model) {
        try {
            authService.verifyEmail(token);
            model.addAttribute("success", true);
        } catch (Exception e) {
            model.addAttribute("success", false);
            String msg = e.getMessage();
            model.addAttribute("message", msg != null ? msg : "Token invalid or expired");
        }
        return "verify-result";
    }
}

