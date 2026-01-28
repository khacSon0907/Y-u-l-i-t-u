package com.example.demo.domain.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ResetPasswordReq {

    @NotBlank(message = "Token reset không được để trống")
    private String resetToken;

    @NotBlank(message = "Mật khẩu mới không được để trống")
    @Size(min = 8, max = 32, message = "Mật khẩu phải từ 8–32 ký tự")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Mật khẩu phải chứa ít nhất 1 chữ cái và 1 số"
    )
    private String newPassword;
}