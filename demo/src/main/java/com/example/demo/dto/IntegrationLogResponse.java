package com.example.demo.dto;

import java.time.Instant;

public record IntegrationLogResponse(
        Long id,
        Long registrationId,
        String registrationCode,
        String requestId,
        String traceId,
        String eventType,
        String outcome,
        String detail,
        Instant createdAt
) {
}
