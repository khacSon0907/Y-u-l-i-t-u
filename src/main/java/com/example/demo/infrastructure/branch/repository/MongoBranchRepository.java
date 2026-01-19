package com.example.demo.infrastructure.branch.repository;

import com.example.demo.domain.model.BranchDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MongoBranchRepository extends MongoRepository<BranchDocument, String> {

    List<BranchDocument> findByNameContainingIgnoreCase(String name);

}

