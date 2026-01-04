package com.example.demo.service.user;


import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserReq req) {

        // 1️⃣ Check email
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(UserError.EMAIL_EXISTS);
        }

        // 2️⃣ Check username
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new BusinessException(UserError.USERNAME_EXISTS);
        }

        // 3️⃣ Check tuổi
        int age = Year.now().getValue() - req.getYear();
        if (age < 18) {
            throw new BusinessException(UserError.UNDER_AGE);
        }

        // 4️⃣ Encode password
        UserEntity user = UserEntity.builder()
                .username(req.getUsername())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .year(req.getYear())
                .build();

        // 5️⃣ Save
        UserEntity savedUser = userRepository.save(user);

        // 6️⃣ Map Entity → Response (❗ QUAN TRỌNG)
        return UserResponseMapper.toResponse(savedUser);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponseMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<UserEntity> getByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
