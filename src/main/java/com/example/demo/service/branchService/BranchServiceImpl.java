package com.example.demo.service.branchService;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.dto.res.BranchResponse;
import com.example.demo.infrastructure.branch.mapper.BranchResponseMapper;
import com.example.demo.service.branchService.repository.IBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BranchServiceImpl implements IBranchService {

    private final IBranchRepository branchRepository;

    @Override
    public BranchResponse createBranch(CreateBranchReq req) {
        var saved = branchRepository.create(req);
        return BranchResponseMapper.toResponse(saved);
    }

    @Override
    public List<BranchResponse> getAllBranches() {
        return branchRepository.findAll()
                .stream()
                .map(BranchResponseMapper::toResponse)
                .toList();
    }

    @Override
    public Optional<BranchResponse> getBranchById(String branchId) {
        return branchRepository.findById(branchId)
                .map(BranchResponseMapper::toResponse);
    }

    @Override
    public BranchResponse updateBranch(String branchId, UpdateBranchReq req) {
        var updatedOpt = branchRepository.update(branchId, req);
        if (updatedOpt.isEmpty()) return null;
        return BranchResponseMapper.toResponse(updatedOpt.get());
    }

    @Override
    public void deleteBranch(String branchId) {
        branchRepository.deleteById(branchId);
    }

    @Override
    public List<BranchResponse> searchBranches(String q) {
        return branchRepository.search(q)
                .stream()
                .map(BranchResponseMapper::toResponse)
                .toList();
    }
}
