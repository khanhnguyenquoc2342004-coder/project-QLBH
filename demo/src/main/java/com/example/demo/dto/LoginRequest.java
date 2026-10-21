package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Size(max = 50, message = "Tên đăng nhập không hợp lệ")
        String username,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(max = 72, message = "Mật khẩu không hợp lệ")
        String password
) {
}
