package com.example.demo.domain.dto.res;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BranchResponse {
    private String id;
    private String name;
    private String street;
    private String ward;
    private String district;
    private String province;
    private String country;
}
