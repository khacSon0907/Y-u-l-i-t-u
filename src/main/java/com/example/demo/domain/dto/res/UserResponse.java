package com.example.demo.domain.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private String id;
    private String username;
    private String email;
    private Integer year;
}
