package com.dnnthanh.wallet.be.platform.idempotency;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.exception.PlatformErrorCode;
import org.junit.jupiter.api.Test;

class IdempotencyKeyTest {
    @Test
    void trimsAndKeepsValidKey() {
        assertThat(new IdempotencyKey(" request-123 ").value()).isEqualTo("request-123");
    }

    @Test
    void rejectsBlankAndOversizedKeysWithStableBadRequestCode() {
        assertInvalidIdempotencyKey(" ");
        assertInvalidIdempotencyKey("x".repeat(IdempotencyKey.MAX_LENGTH + 1));
    }

    private void assertInvalidIdempotencyKey(String value) {
        assertThatThrownBy(() -> new IdempotencyKey(value))
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY));
    }
}
