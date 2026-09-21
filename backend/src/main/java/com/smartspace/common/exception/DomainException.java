package com.smartspace.common.exception;

import lombok.Getter;

import java.util.List;
import java.util.Map;

@Getter
public class DomainException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<ApiError.ErrorDetail> details;
    private final List<Map<String, Object>> alternatives;

    public DomainException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = null;
        this.alternatives = null;
    }

    public DomainException(ErrorCode errorCode, String message, List<ApiError.ErrorDetail> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
        this.alternatives = null;
    }

    public DomainException(ErrorCode errorCode, String message, List<ApiError.ErrorDetail> details, List<Map<String, Object>> alternatives) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
        this.alternatives = alternatives;
    }
}
