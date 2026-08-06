package com.dnnthanh.wallet.be.platform.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PlatformErrorCode implements ErrorCode {
    VALIDATION_FAILED("VALIDATION_FAILED", "error.validation", HttpStatus.BAD_REQUEST),
    BAD_REQUEST("BAD_REQUEST", "error.bad-request", HttpStatus.BAD_REQUEST),
    INVALID_IDEMPOTENCY_KEY(
            "INVALID_IDEMPOTENCY_KEY", "error.invalid-idempotency-key", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("UNAUTHORIZED", "error.unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("FORBIDDEN", "error.forbidden", HttpStatus.FORBIDDEN),
    NOT_FOUND("NOT_FOUND", "error.not-found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED(
            "METHOD_NOT_ALLOWED", "error.method-not-allowed", HttpStatus.METHOD_NOT_ALLOWED),
    UNSUPPORTED_MEDIA_TYPE(
            "UNSUPPORTED_MEDIA_TYPE",
            "error.unsupported-media-type",
            HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    NOT_ACCEPTABLE("NOT_ACCEPTABLE", "error.not-acceptable", HttpStatus.NOT_ACCEPTABLE),
    PAYLOAD_TOO_LARGE("PAYLOAD_TOO_LARGE", "error.payload-too-large", HttpStatus.PAYLOAD_TOO_LARGE),
    DATA_CONFLICT("DATA_CONFLICT", "error.conflict", HttpStatus.CONFLICT),
    DEPENDENCY_TIMEOUT("DEPENDENCY_TIMEOUT", "error.timeout", HttpStatus.SERVICE_UNAVAILABLE),
    SERVICE_TOKEN_ACQUISITION_FAILED(
            "SERVICE_TOKEN_ACQUISITION_FAILED",
            "error.service-token-acquisition",
            HttpStatus.SERVICE_UNAVAILABLE),
    INTERNAL_ERROR("INTERNAL_ERROR", "error.internal", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String messageKey;
    private final HttpStatus httpStatus;
}
