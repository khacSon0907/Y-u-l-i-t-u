package com.example.demo.infrastructure.user.mapper;

import com.example.demo.domain.entities.UserEntity;
import com.example.demo.domain.enums.Role;
import com.example.demo.domain.model.UserDocument;

import java.util.Optional;
public class UserMapper {
    // Document (Mongo) -> Entity (Domain)
    public static UserEntity toEntity(UserDocument document) {
        if (document == null) return null;
        return UserEntity.builder()
                .id(document.getId())
                .username(document.getUsername())
                .email(document.getEmail())
                .password(document.getPassword())
                .year(document.getYear())
                .emailVerified(document.isEmailVerified())
                .role(Optional.ofNullable(document.getRole())
                        .orElse(Role.USER))
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .build();
    }
    // Entity (Domain) -> Document (Mongo)
    public static UserDocument toDocument(UserEntity entity) {
        if (entity == null) return null;
        return UserDocument.builder()
                .id(entity.getId())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .password(entity.getPassword())
                .year(entity.getYear())
                .emailVerified(entity.isEmailVerified())
                .role(entity.getRole())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
