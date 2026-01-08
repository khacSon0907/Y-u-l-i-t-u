package com.example.demo.domain.dto.req;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserReq {

    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3–50 ký tự")
    private String username;

    @Min(value = 1900, message = "Năm sinh phải ≥ 1900")
    @Max(value = 2100, message = "Năm sinh phải ≤ 2100")
    private Integer year;

}
