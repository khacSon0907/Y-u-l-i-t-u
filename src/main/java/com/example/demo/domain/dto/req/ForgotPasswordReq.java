package com.example.demo.domain.dto.req;

import lombok.*;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class ForgotPasswordReq {
    private String email;
}
