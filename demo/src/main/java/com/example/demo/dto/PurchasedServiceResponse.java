package com.example.demo.dto;

import com.example.demo.entity.RegistrationStatus;

public record PurchasedServiceResponse(
        Long orderItemId,
        Long orderId,
        String orderCode,
        Long packageId,
        String packageCode,
        String packageName,
        int quantity,
        RegistrationStatus latestActivationStatus,
        boolean canActivate
) {
}
