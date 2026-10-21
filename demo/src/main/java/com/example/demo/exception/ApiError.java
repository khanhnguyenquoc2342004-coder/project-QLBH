package com.example.demo.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String errorCode,
        String message,
        String traceId,
        Map<String, String> validationErrors
) {
}
