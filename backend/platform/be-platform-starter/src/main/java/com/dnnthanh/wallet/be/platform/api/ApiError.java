package com.dnnthanh.wallet.be.platform.api;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Stable API error contract shared by backend APIs. */
public record ApiError(
        String code,
        String message,
        String traceId,
        LocalDateTime timestamp,
        String path,
        List<FieldError> fieldErrors,
        Map<String, Object> details) {
    public record FieldError(String field, String message) {}
}
