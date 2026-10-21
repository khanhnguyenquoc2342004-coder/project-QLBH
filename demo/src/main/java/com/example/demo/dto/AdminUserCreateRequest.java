package com.example.demo.dto;

import com.example.demo.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AdminUserCreateRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
        String fullName,

        @NotBlank(message = "Tên đăng nhập không được để trống")
        @Pattern(regexp = "^[a-zA-Z0-9._-]{3,50}$", message = "Tên đăng nhập gồm 3-50 ký tự chữ, số, dấu chấm, gạch dưới hoặc gạch ngang")
        String username,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email tối đa 120 ký tự")
        String email,

        @NotBlank(message = "Mật khẩu không được để trống")
        @Size(min = 8, max = 72, message = "Mật khẩu phải có 8-72 ký tự")
        @Pattern(regexp = ".*[A-Za-z].*", message = "Mật khẩu cần có ít nhất một chữ cái")
        @Pattern(regexp = ".*[0-9].*", message = "Mật khẩu cần có ít nhất một chữ số")
        String password,

        @NotNull(message = "Vai trò không được để trống")
        UserRole role
) {
}
