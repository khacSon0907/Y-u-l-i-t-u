
package com.example.demo.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateBranchReq {

    @NotBlank(message = "Branch name is required")
    private String name;

    @NotNull(message = "Address is required")
    private AddressReq address;
}

