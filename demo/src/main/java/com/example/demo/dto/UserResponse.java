package com.example.demo.dto;

public record UserResponse(
        Long id,
        String username,
        String fullName,
        String email,
        String phone,
        String address,
        String role,
        boolean enabled
) {
}
