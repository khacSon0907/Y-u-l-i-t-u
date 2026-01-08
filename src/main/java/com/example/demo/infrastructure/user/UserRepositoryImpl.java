package com.example.demo.infrastructure.user;

import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.infrastructure.user.repository.MongoUserRepository;
import com.example.demo.service.user.repository.IUserRepository;



import com.example.demo.domain.entities.UserEntity;
import com.example.demo.infrastructure.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements IUserRepository {

    private final MongoUserRepository mongoUserRepository;

    @Override
    public UserEntity save(UserEntity user) {
        var document = UserMapper.toDocument(user);
        var saved = mongoUserRepository.save(document);
        return UserMapper.toEntity(saved);
    }

    @Override
    public List<UserEntity> findAll() {
        return mongoUserRepository.findAll()
                .stream()
                .map(UserMapper::toEntity)
                .toList();
    }

    @Override
    public boolean existsByEmail(String email) {
        return mongoUserRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return mongoUserRepository.existsByUsername(username);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return mongoUserRepository.findByEmail(email)
                .map(UserMapper::toEntity);
    }

    @Override
    public Optional<UserEntity> findById(String id) {
        return mongoUserRepository.findById(id)
                .map(UserMapper::toEntity);
    }
}
