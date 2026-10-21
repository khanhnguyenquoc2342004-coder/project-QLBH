package com.example.demo.dto;

import java.time.Instant;

public record AuditLogResponse(Long id, String actor, String action, String targetType, String targetId, String detail, Instant createdAt) {
}
