package com.example.demo.infrastructure.branch;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.entities.BranchEntity;
import com.example.demo.infrastructure.branch.mapper.BranchMapper;
import com.example.demo.infrastructure.branch.repository.MongoBranchRepository;
import com.example.demo.service.branchService.repository.IBranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BranchRepositoryImpl implements IBranchRepository {

    private final MongoBranchRepository mongoBranchRepository;

    @Override
    public BranchEntity create(CreateBranchReq req) {
        var entity = BranchMapper.toEntity(req);
        var doc = BranchMapper.toDocument(entity);
        var saved = mongoBranchRepository.save(doc);
        return BranchMapper.toEntity(saved);
    }

    @Override
    public Optional<BranchEntity> update(String id, UpdateBranchReq req) {
        var existingOpt = mongoBranchRepository.findById(id);
        if (existingOpt.isEmpty()) return Optional.empty();

        var doc = existingOpt.get();
        var entity = BranchMapper.toEntity(doc);
        // apply update from req onto entity
        BranchMapper.applyUpdate(entity, req);

        var updatedDoc = BranchMapper.toDocument(entity);
        var saved = mongoBranchRepository.save(updatedDoc);
        return Optional.of(BranchMapper.toEntity(saved));
    }

    @Override
    public BranchEntity save(BranchEntity branch) {
        var doc = BranchMapper.toDocument(branch);
        var saved = mongoBranchRepository.save(doc);
        return BranchMapper.toEntity(saved);
    }

    @Override
    public List<BranchEntity> findAll() {
        return mongoBranchRepository.findAll()
                .stream()
                .map(BranchMapper::toEntity)
                .toList();
    }

    @Override
    public Optional<BranchEntity> findById(String id) {
        return mongoBranchRepository.findById(id)
                .map(BranchMapper::toEntity);
    }

    @Override
    public void deleteById(String id) {
        mongoBranchRepository.deleteById(id);
    }

    @Override
    public List<BranchEntity> search(String q) {
        if (q == null || q.isBlank()) return findAll();
        return mongoBranchRepository.findByNameContainingIgnoreCase(q)
                .stream()
                .map(BranchMapper::toEntity)
                .toList();
    }
}
