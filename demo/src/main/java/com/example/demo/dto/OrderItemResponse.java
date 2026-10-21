package com.example.demo.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
        Long packageId,
        String packageCode,
        String packageName,
        BigDecimal unitPrice,
        int discountPercent,
        BigDecimal lineTotal,
        int quantity
) {
}
