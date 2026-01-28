package com.example.demo.controller.user;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.demo.domain.dto.req.ChangePasswordReq;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.service.user.IUserService;
import com.example.demo.share.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserControllers {
    private final IUserService userService;

    @PostMapping
    public ApiResponse<UserResponse> createUser(
            @Valid @RequestBody CreateUserReq req,
            HttpServletRequest request
    ) {
        UserResponse user = userService.createUser(req);

        return ApiResponse.success(
                HttpStatus.CREATED.value(),
                "USER.CREATE_SUCCESS",
                "Create user successfully",
                user,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ApiResponse<List<UserResponse>> getAllUsers(
            HttpServletRequest request
    ) {
        List<UserResponse> users = userService.getAllUsers();

        return ApiResponse.success(
                HttpStatus.OK.value(),
                "USER.GET_ALL_SUCCESS",
                "Get all users successfully",
                users,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }

    // Không cần @PreAuthorize
    @PutMapping("/change-password")
    public ApiResponse<Void> changePassword(
            @Valid @RequestBody ChangePasswordReq req,
            HttpServletRequest request
    ) {
        // ✅ Lấy userId trực tiếp từ Authentication
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = (String) authentication.getPrincipal(); // Principal là userId (String)
        userService.changePassword(userId, req);
        return ApiResponse.success(
                HttpStatus.OK.value(),
                "USER.CHANGE_PASSWORD_SUCCESS",
                "Đổi mật khẩu thành công",
                null,
                request.getRequestURI(),
                MDC.get("traceId")
        );
    }
}