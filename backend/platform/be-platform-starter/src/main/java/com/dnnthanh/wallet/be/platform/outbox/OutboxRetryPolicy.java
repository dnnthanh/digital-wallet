package com.dnnthanh.wallet.be.platform.outbox;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_BASE_DELAY_POSITIVE;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_BASE_DELAY_REQUIRED;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_MAX_ATTEMPTS_POSITIVE;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_MAX_DELAY_NOT_LESS_THAN_BASE;
import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.OUTBOX_MAX_DELAY_REQUIRED;

import java.time.Duration;
import org.apache.commons.lang3.Validate;

public record OutboxRetryPolicy(int maxAttempts, Duration baseDelay, Duration maxDelay) {
    public OutboxRetryPolicy {
        Validate.isTrue(maxAttempts > 0, OUTBOX_MAX_ATTEMPTS_POSITIVE);
        Validate.notNull(baseDelay, OUTBOX_BASE_DELAY_REQUIRED);
        Validate.notNull(maxDelay, OUTBOX_MAX_DELAY_REQUIRED);
        Validate.isTrue(!baseDelay.isZero() && !baseDelay.isNegative(), OUTBOX_BASE_DELAY_POSITIVE);
        Validate.isTrue(maxDelay.compareTo(baseDelay) >= 0, OUTBOX_MAX_DELAY_NOT_LESS_THAN_BASE);
    }

    public static OutboxRetryPolicy defaults() {
        return new OutboxRetryPolicy(10, Duration.ofSeconds(2), Duration.ofMinutes(5));
    }

    public Duration nextDelay(int attemptCount) {
        if (attemptCount <= 1) return baseDelay;
        Duration delay = baseDelay;
        for (int attempt = 1; attempt < attemptCount; attempt++) {
            if (delay.compareTo(maxDelay) >= 0) return maxDelay;
            try {
                Duration doubled = delay.multipliedBy(2);
                delay = doubled.compareTo(maxDelay) > 0 ? maxDelay : doubled;
            } catch (ArithmeticException overflow) {
                return maxDelay;
            }
        }
        return delay;
    }

    public boolean terminal(int attemptCount) {
        return attemptCount >= maxAttempts;
    }
}
