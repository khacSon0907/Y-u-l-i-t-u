package com.example.demo.domain.dto.req;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResendEmailReq {

    @NotBlank(message = "EMAIL.NOT_BLANK")
    @Email(message = "EMAIL.INVALID")
    private String email;
}

