package com.example.demo.domain.entities;


import com.example.demo.domain.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEntity {
    private String id;
    private String username ;
    private String email ;
    private String password ;
    private int year ;
    private Role role; // ROLE_USER / ROLE_ADMIN

    private boolean emailVerified;
}
