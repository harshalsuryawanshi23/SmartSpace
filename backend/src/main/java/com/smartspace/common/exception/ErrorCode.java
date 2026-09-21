package com.smartspace.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST),
    INVALID_SLOT(HttpStatus.BAD_REQUEST),
    PAYMENT_SIGNATURE_INVALID(HttpStatus.BAD_REQUEST),
    OTP_INVALID(HttpStatus.BAD_REQUEST),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED),
    FORBIDDEN(HttpStatus.FORBIDDEN),
    KYC_REQUIRED(HttpStatus.FORBIDDEN),
    RATING_NOT_ELIGIBLE(HttpStatus.FORBIDDEN),
    OTP_LOCKED(HttpStatus.FORBIDDEN),
    NOT_FOUND(HttpStatus.NOT_FOUND),
    SLOT_UNAVAILABLE(HttpStatus.CONFLICT),
    ILLEGAL_STATE_TRANSITION(HttpStatus.CONFLICT),
    AMOUNT_MISMATCH(HttpStatus.CONFLICT),
    IDEMPOTENCY_CONFLICT(HttpStatus.CONFLICT),
    ALREADY_EXISTS(HttpStatus.CONFLICT),
    BOOKING_EXPIRED(HttpStatus.GONE),
    TOO_SOON(HttpStatus.UNPROCESSABLE_ENTITY),
    OUTSIDE_BOOKING_WINDOW(HttpStatus.UNPROCESSABLE_ENTITY),
    OUTSIDE_OPEN_HOURS(HttpStatus.UNPROCESSABLE_ENTITY),
    HALL_BLOCKED(HttpStatus.UNPROCESSABLE_ENTITY),
    AFTER_LATEST_END(HttpStatus.UNPROCESSABLE_ENTITY),
    QUIET_HOURS(HttpStatus.UNPROCESSABLE_ENTITY),
    OVER_CAPACITY(HttpStatus.UNPROCESSABLE_ENTITY),
    TOO_MANY_LOCKS(HttpStatus.UNPROCESSABLE_ENTITY),
    HALL_NOT_AVAILABLE(HttpStatus.UNPROCESSABLE_ENTITY),
    RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR);

    private final HttpStatus httpStatus;

    ErrorCode(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
