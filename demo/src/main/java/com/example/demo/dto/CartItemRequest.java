package com.example.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record CartItemRequest(
        @NotNull(message = "Gói dịch vụ không được để trống")
        Long packageId,
        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 100, message = "Số lượng tối đa là 100")
        Integer quantity
) {
}
