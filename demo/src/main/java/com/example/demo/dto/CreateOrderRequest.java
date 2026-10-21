package com.example.demo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotEmpty(message = "Giỏ hàng đang trống")
        List<@Valid OrderItemRequest> items,

        @jakarta.validation.constraints.Pattern(regexp = "CASH|BANK_TRANSFER", message = "Phương thức thanh toán không hợp lệ")
        String paymentMethod,

        @jakarta.validation.constraints.Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        String note
) {
}
