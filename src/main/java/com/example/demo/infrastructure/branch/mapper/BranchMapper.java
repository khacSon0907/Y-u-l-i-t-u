package com.example.demo.infrastructure.branch.mapper;

import com.example.demo.domain.dto.req.CreateBranchReq;
import com.example.demo.domain.dto.req.UpdateBranchReq;
import com.example.demo.domain.dto.res.BranchResponse;
import com.example.demo.domain.entities.AddressEntity;
import com.example.demo.domain.entities.BranchEntity;
import com.example.demo.domain.model.AddressDocument;
import com.example.demo.domain.model.BranchDocument;

public class BranchMapper {

    // Document -> Entity
    public static BranchEntity toEntity(BranchDocument doc) {
        if (doc == null) return null;
        return BranchEntity.builder()
                .id(doc.getId())
                .name(doc.getName())
                .address(toAddressEntity(doc.getAddress()))
                .build();
    }

    // Entity -> Document
    public static BranchDocument toDocument(BranchEntity entity) {
        if (entity == null) return null;
        return BranchDocument.builder()
                .id(entity.getId())
                .name(entity.getName())
                .address(toAddressDocument(entity.getAddress()))
                .build();
    }

    // Create request -> Entity
    public static BranchEntity toEntity(CreateBranchReq req) {
        if (req == null) return null;
        return BranchEntity.builder()
                .name(req.getName())
                .address(addressReqToEntity(req.getAddress()))
                .build();
    }

    // Update request -> apply to existing entity (helper)
    public static void applyUpdate(BranchEntity existing, UpdateBranchReq req) {
        if (existing == null || req == null) return;
        if (req.getName() != null) existing.setName(req.getName());
        if (req.getAddress() != null) existing.setAddress(addressReqToEntity(req.getAddress()));
    }

    // Entity -> Response DTO
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

    // Address mappings
    public static AddressEntity toAddressEntity(AddressDocument d) {
        if (d == null) return null;
        return AddressEntity.builder()
                .street(d.getStreet())
                .ward(d.getWard())
                .district(d.getDistrict())
                .province(d.getProvince())
                .country(d.getCountry())
                .build();
    }

    public static AddressDocument toAddressDocument(AddressEntity e) {
        if (e == null) return null;
        return AddressDocument.builder()
                .street(e.getStreet())
                .ward(e.getWard())
                .district(e.getDistrict())
                .province(e.getProvince())
                .country(e.getCountry())
                .build();
    }

    public static AddressEntity addressReqToEntity(com.example.demo.domain.dto.req.AddressReq req) {
        if (req == null) return null;
        return AddressEntity.builder()
                .street(req.getStreet())
                .ward(req.getWard())
                .district(req.getDistrict())
                .province(req.getProvince())
                .country(req.getCountry())
                .build();
    }
}
