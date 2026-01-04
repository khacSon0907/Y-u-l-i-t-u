package com.example.demo.infrastructure.user.mapper;

import com.example.demo.domain.entities.UserEntity;
import com.example.demo.domain.model.UserDocument;

public class UserMapper {


    public static UserEntity toEntity(UserDocument document) {
        if (document == null) return null;

        return UserEntity.builder()
                .id(document.getId())
                .username(document.getUsername())
                .email(document.getEmail())
                .password(document.getPassword())
                .year(document.getYear())
                .build();
    }

    public static UserDocument toDocument(UserEntity entity) {
        if (entity == null) return null;

        return UserDocument.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .year(entity.getYear())
                .build();
    }
}
