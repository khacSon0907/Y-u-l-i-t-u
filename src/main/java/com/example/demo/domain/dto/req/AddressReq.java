package com.example.demo.domain.dto.req;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddressReq {
    @Size(max = 200)
    private String street;
    @Size(max = 100)
    private String ward;
    @Size(max = 100)
    private String district;
    @Size(max = 100)
    private String province;
    @Size(max = 100)
    private String country;
}
