package com.dnnthanh.wallet.be.platform.api;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ApiResponseContractTest {
    @Test
    void keepsReferenceStyleStableErrorEnvelope() {
        var error =
                new ApiError(
                        "DUPLICATE_REQUEST",
                        "Duplicate request",
                        "trace-123",
                        LocalDateTime.of(2026, 8, 6, 0, 0),
                        "/api/v1/transfers",
                        List.of(),
                        Map.of("idempotencyKey", "request-123"));

        var response = ApiResponse.failure(error);

        assertThat(response.data()).isNull();
        assertThat(response.error()).isEqualTo(error);
        assertThat(response.error().code()).isEqualTo("DUPLICATE_REQUEST");
    }
}
