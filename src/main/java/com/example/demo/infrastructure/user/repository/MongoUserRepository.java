package com.example.demo.infrastructure.user.repository;

import com.example.demo.domain.model.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface MongoUserRepository extends MongoRepository<UserDocument, String> {

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);


    // 👉 THÊM DÒNG NÀY
    Optional<UserDocument> findByEmail(String email);
}
