package com.example.demo.service.user;

import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;

import java.util.List;
import java.util.Optional;

public interface IUserService {

    UserResponse createUser(CreateUserReq req);

    List<UserResponse> getAllUsers();

    Optional<UserEntity> getByEmail(String email);

}

