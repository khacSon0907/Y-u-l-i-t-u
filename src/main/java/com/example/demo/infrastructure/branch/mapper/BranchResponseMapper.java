package com.example.demo.infrastructure.branch.mapper;

import com.example.demo.domain.dto.res.BranchResponse;
import com.example.demo.domain.entities.BranchEntity;

public class BranchResponseMapper {

    private BranchResponseMapper() {}

    public static BranchResponse toResponse(BranchEntity entity) {
        if (entity == null) return null;
        var addr = entity.getAddress();
        return BranchResponse.builder()
                .id(entity.getId())
                .name(entity.getName())
                .street(addr != null ? addr.getStreet() : null)
                .ward(addr != null ? addr.getWard() : null)
                .district(addr != null ? addr.getDistrict() : null)
                .province(addr != null ? addr.getProvince() : null)
                .country(addr != null ? addr.getCountry() : null)
                .build();
    }
}
