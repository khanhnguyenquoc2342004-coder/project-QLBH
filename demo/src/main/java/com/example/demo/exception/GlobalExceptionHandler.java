package com.example.demo.exception;

import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler({BadRequestException.class, UnauthorizedException.class})
    public ResponseEntity<ApiError> handleClientError(RuntimeException exception) {
        HttpStatus status = exception instanceof UnauthorizedException ? HttpStatus.UNAUTHORIZED : HttpStatus.BAD_REQUEST;
        return buildResponse(status, exception.getMessage(), Map.of());
    }

    @ExceptionHandler({DuplicateResourceException.class, DataIntegrityViolationException.class})
    public ResponseEntity<ApiError> handleConflict(RuntimeException exception) {
        return buildResponse(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> validationErrors = new LinkedHashMap<>();

        for (FieldError fieldError : exception.getBindingResult().getFieldErrors()) {
            validationErrors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return buildResponse(HttpStatus.BAD_REQUEST, "Dữ liệu gửi lên không hợp lệ", validationErrors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedError(Exception exception) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Có lỗi hệ thống, vui lòng thử lại sau", Map.of());
    }

    private ResponseEntity<ApiError> buildResponse(
            HttpStatus status,
            String message,
            Map<String, String> validationErrors
    ) {
        ApiError response = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                toErrorCode(status),
                message,
                MDC.get("traceId"),
                validationErrors
        );

        return ResponseEntity.status(status).body(response);
    }

    private String toErrorCode(HttpStatus status) {
        return switch (status) {
            case NOT_FOUND -> "RESOURCE_NOT_FOUND";
            case CONFLICT -> "DUPLICATE_RESOURCE";
            case UNAUTHORIZED -> "UNAUTHORIZED";
            case FORBIDDEN -> "ACCESS_DENIED";
            case BAD_REQUEST -> "VALIDATION_OR_BUSINESS_ERROR";
            default -> "INTERNAL_ERROR";
        };
    }
}
