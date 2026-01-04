package com.example.demo.service.user.repository;

import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface IUserRepository {

    UserEntity save(UserEntity req);

    List<UserEntity> findAll();


    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    // 👉 THÊM DÒNG NÀY
    Optional<UserEntity> findByEmail(String email);
}
