package com.example.demo.service.user;

import com.example.demo.domain.dto.req.ChangePasswordReq;
import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface IUserService {





    // =========================
    // 🆕 CREATE
    // =========================
    UserResponse createUser(CreateUserReq req);

    // =========================
    // 📄 READ
    // =========================
    List<UserResponse> getAllUsers();

    Optional<UserEntity> getByEmail(String email);

    Optional<UserEntity> getUserById(String userId);

    // =========================
    // ✏️ UPDATE
    // =========================
    UserResponse updateUser(String userId, UpdateUserReq req);

    // Verify user's email (emailVerified = true)
    UserResponse verifyEmail(String userId);

    // =========================
    // 🧱 LOW-LEVEL (🆕)
    // =========================
    UserEntity save(UserEntity user);

    // Change user's password
    void changePassword(String userId, ChangePasswordReq req);
}
