package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CustomerProfileRequest(
        @NotBlank(message = "Họ tên không được để trống")
        @Size(max = 120, message = "Họ tên tối đa 120 ký tự")
        String fullName,

        @Pattern(regexp = "^$|^[0-9+ -]{8,20}$", message = "Số điện thoại không hợp lệ")
        String phone,

        @Size(max = 300, message = "Địa chỉ tối đa 300 ký tự")
        String address
) {
}
