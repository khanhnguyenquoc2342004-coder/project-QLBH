package com.example.demo.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long id,
        Long packageId,
        String code,
        String name,
        String description,
        String imageUrl,
        BigDecimal price,
        int discountPercent,
        int quantity
) {
}
