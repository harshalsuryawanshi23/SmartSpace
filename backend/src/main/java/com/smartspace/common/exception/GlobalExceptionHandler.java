package com.smartspace.common.exception;

import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    private final Clock clock;

    public GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ApiError> handleDomainException(DomainException ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now(clock))
                .status(ex.getErrorCode().getHttpStatus().value())
                .code(ex.getErrorCode().name())
                .message(ex.getMessage())
                .details(ex.getDetails())
                .alternatives(ex.getAlternatives())
                .traceId(MDC.get("traceId"))
                .build();

        return new ResponseEntity<>(apiError, ex.getErrorCode().getHttpStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationException(MethodArgumentNotValidException ex) {
        List<ApiError.ErrorDetail> details = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ApiError.ErrorDetail.builder()
                        .field(error.getField())
                        .issue(error.getDefaultMessage())
                        .build())
                .collect(Collectors.toList());

        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now(clock))
                .status(HttpStatus.BAD_REQUEST.value())
                .code(ErrorCode.VALIDATION_ERROR.name())
                .message("Validation failed")
                .details(details)
                .traceId(MDC.get("traceId"))
                .build();

        return new ResponseEntity<>(apiError, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        ApiError apiError = ApiError.builder()
                .timestamp(Instant.now(clock))
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .code(ErrorCode.INTERNAL_ERROR.name())
                .message("An unexpected error occurred")
                .traceId(MDC.get("traceId"))
                .build();
        ex.printStackTrace();

        // In production we should log the full stack trace, but only return the ApiError
        return new ResponseEntity<>(apiError, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
