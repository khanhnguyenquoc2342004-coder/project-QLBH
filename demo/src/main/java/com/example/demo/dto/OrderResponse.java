package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String orderCode,
        String status,
        String paymentMethod,
        String paymentStatus,
        BigDecimal totalAmount,
        Instant createdAt,
        String customerName,
        String customerEmail,
        String customerPhone,
        String customerAddress,
        String note,
        List<OrderItemResponse> items
) {
}
