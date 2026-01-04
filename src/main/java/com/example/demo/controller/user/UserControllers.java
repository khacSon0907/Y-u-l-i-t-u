package com.example.demo.controller.user;

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
}
