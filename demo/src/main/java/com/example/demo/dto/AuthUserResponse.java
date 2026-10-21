package com.example.demo.dto;

public record AuthUserResponse(
        Long id,
        String username,
        String fullName,
        String role,
        String email,
        String phone,
        String address
) {
}
