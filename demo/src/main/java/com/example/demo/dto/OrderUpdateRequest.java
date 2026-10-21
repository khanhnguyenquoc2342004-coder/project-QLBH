package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record OrderUpdateRequest(
        @NotBlank(message = "Trạng thái đơn hàng không được để trống")
        @Pattern(regexp = "PENDING|CONFIRMED|COMPLETED|CANCELLED|REFUND_REQUESTED|REFUNDED", message = "Trạng thái đơn hàng không hợp lệ")
        String status,
        @Pattern(regexp = "UNPAID|PAID|REFUNDED", message = "Trạng thái thanh toán không hợp lệ")
        String paymentStatus
) {
}
