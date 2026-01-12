package com.example.demo.service.user;

import com.example.demo.domain.dto.req.CreateUserReq;
import com.example.demo.domain.dto.req.UpdateUserReq;
import com.example.demo.domain.dto.res.UserResponse;
import com.example.demo.domain.entities.UserEntity;
import com.example.demo.domain.enums.Role;
import com.example.demo.exception.BusinessException;
import com.example.demo.exception.user.UserError;
import com.example.demo.infrastructure.user.mapper.UserResponseMapper;
import com.example.demo.service.user.repository.IUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements IUserService {

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse createUser(CreateUserReq req) {

        String normalizedUsername = normalizeUsername(req.getUsername());

        // Optional: nếu bạn có lỗi riêng cho username invalid thì dùng, không thì bỏ block này
        if (normalizedUsername == null || normalizedUsername.isBlank()) {
            throw new BusinessException(UserError.USERNAME_INVALID); // nếu chưa có thì đổi sang lỗi khác bạn đang dùng
        }

        // 1️⃣ Check email
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new BusinessException(UserError.EMAIL_EXISTS);
        }

        // 2️⃣ Check username (DÙNG normalized)
        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new BusinessException(UserError.USERNAME_EXISTS);
        }

        // 3️⃣ Check tuổi
        int age = Year.now().getValue() - req.getYear();
        if (age < 18) {
            throw new BusinessException(UserError.UNDER_AGE);
        }

        // 4️⃣ Encode password + Save (DÙNG normalized)
        UserEntity user = UserEntity.builder()
                .username(normalizedUsername)
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .year(req.getYear())
                .role(Role.USER)
                .build();

        // 5️⃣ Save
        UserEntity savedUser = userRepository.save(user);

        // 6️⃣ Map Entity → Response
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

    @Override
    public Optional<UserEntity> getUserById(String userId) {
        return userRepository.findById(userId);
    }

    @Override
    public UserResponse updateUser(String userId, UpdateUserReq req) {

        // 1️⃣ Fetch existing user
        UserEntity existing = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(UserError.USER_NOT_FOUND));

        // 2️⃣ Update username if provided
        if (req.getUsername() != null) {
            String normalizedUsername = normalizeUsername(req.getUsername());

            if (normalizedUsername != null && !normalizedUsername.isBlank()) {
                // If username changed, ensure uniqueness (DÙNG normalized)
                if (!normalizedUsername.equals(existing.getUsername())
                        && userRepository.existsByUsername(normalizedUsername)) {
                    throw new BusinessException(UserError.USERNAME_EXISTS);
                }
                existing.setUsername(normalizedUsername);
            }
        }

        // 3️⃣ Update year if provided
        if (req.getYear() != null) {
            int age = Year.now().getValue() - req.getYear();
            if (age < 18) {
                throw new BusinessException(UserError.UNDER_AGE);
            }
            existing.setYear(req.getYear());
        }

        // 4️⃣ Persist
        UserEntity saved = userRepository.save(existing);

        // 5️⃣ Map to response
        return UserResponseMapper.toResponse(saved);
    }

    /**
     * Normalize username:
     * - trim 2 đầu
     * - gộp nhiều khoảng trắng thành 1 khoảng trắng
     * - lowercase hết
     *
     * Ví dụ: " Khắc      sơn      " -> "khắc sơn"
     */
    private String normalizeUsername(String username) {
        if (username == null) return null;
        String cleaned = username.trim().replaceAll("\\s+", " ");
        return cleaned.toLowerCase(Locale.ROOT);
    }
}
