package com.example.demo.infrastructure.user.mapper;

import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;

public class UserResponseMapper {

    private UserResponseMapper() {
        // ngăn new Mapper
    }

    public static UserResponse toResponse(UserEntity entity) {

        if (entity == null) {
            return null;
        }

        return UserResponse.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .year(entity.getYear())
                .build();
    }
}
