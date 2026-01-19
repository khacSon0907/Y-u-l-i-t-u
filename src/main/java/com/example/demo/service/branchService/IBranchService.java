package com.example.demo.service.branchService;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.dto.res.BranchResponse;

import java.util.List;
import java.util.Optional;

public interface IBranchService {

    BranchResponse createBranch(CreateBranchReq req);

    List<BranchResponse> getAllBranches();

    Optional<BranchResponse> getBranchById(String branchId);

    BranchResponse updateBranch(String branchId, UpdateBranchReq req);

    void deleteBranch(String branchId);

    List<BranchResponse> searchBranches(String q);

}


