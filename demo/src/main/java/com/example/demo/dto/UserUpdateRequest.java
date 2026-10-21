package com.example.demo.dto;

import com.example.demo.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
        String fullName,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không hợp lệ")
        @Size(max = 120, message = "Email tối đa 120 ký tự")
        String email,

        @Size(min = 8, max = 72, message = "Mật khẩu mới phải có 8-72 ký tự")
        @Pattern(regexp = "^$|.*[A-Za-z].*", message = "Mật khẩu mới cần có ít nhất một chữ cái")
        @Pattern(regexp = "^$|.*[0-9].*", message = "Mật khẩu mới cần có ít nhất một chữ số")
        String newPassword,

        @NotNull(message = "Vai trò không được để trống")
        UserRole role,

        @NotNull(message = "Trạng thái không được để trống")
        Boolean enabled
) {
}
