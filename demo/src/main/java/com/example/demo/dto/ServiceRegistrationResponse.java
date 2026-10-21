package com.example.demo.dto;

import com.example.demo.entity.RegistrationStatus;

import java.time.Instant;

public record ServiceRegistrationResponse(
        Long id,
        String registrationCode,
        String requestId,
        Long orderItemId,
        Long orderId,
        String orderCode,
        Long packageId,
        String packageCode,
        String packageName,
        RegistrationStatus status,
        String errorCode,
        String message,
        String externalReference,
        String traceId,
        boolean replayed,
        boolean retryable,
        Instant createdAt,
        Instant updatedAt
) {
    /**
     * Compatibility constructor for code compiled before activation was linked
     * to an order item. New code should use the full constructor above.
     */
    public ServiceRegistrationResponse(
            Long id,
            String registrationCode,
            String requestId,
            Long packageId,
            String packageCode,
            String packageName,
            RegistrationStatus status,
            String errorCode,
            String message,
            String externalReference,
            String traceId,
            boolean replayed,
            boolean retryable,
            Instant createdAt,
            Instant updatedAt
    ) {
        this(
                id,
                registrationCode,
                requestId,
                null,
                null,
                null,
                packageId,
                packageCode,
                packageName,
                status,
                errorCode,
                message,
                externalReference,
                traceId,
                replayed,
                retryable,
                createdAt,
                updatedAt
        );
    }
}
