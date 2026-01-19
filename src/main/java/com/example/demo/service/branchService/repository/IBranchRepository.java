package com.example.demo.service.branchService.repository;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.entities.BranchEntity;

import java.util.List;
import java.util.Optional;

public interface IBranchRepository {

    // Create from request (mapping handled by infra)
    BranchEntity create(CreateBranchReq req);

    // Update from request, return Optional.empty() if not found
    Optional<BranchEntity> update(String id, UpdateBranchReq req);

    // Basic operations
    BranchEntity save(BranchEntity branch);

    List<BranchEntity> findAll();

    Optional<BranchEntity> findById(String id);

    void deleteById(String id);

    List<BranchEntity> search(String q);

}
