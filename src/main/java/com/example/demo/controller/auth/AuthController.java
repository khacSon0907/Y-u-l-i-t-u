package com.example.demo.controller.auth;



import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.LoginReq;
import com.example.demo.domain.dto.res.AuthResponse;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.service.auth.IAuthService;
import com.example.demo.share.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
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
}
