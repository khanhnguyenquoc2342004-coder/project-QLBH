package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record ServiceRegistrationRequest(
        @NotNull(message = "Chon goi dich vu da mua can kich hoat")
        Long orderItemId,

        @NotBlank(message = "Thieu ma requestId")
        @Pattern(regexp = "[A-Za-z0-9-]{8,80}", message = "requestId khong hop le")
        String requestId,

        @NotNull(message = "Chon kich ban xu ly")
        ActivationSimulation simulation
) {
}
