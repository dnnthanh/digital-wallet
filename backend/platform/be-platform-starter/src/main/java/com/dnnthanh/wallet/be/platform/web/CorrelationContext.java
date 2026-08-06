package com.dnnthanh.wallet.be.platform.web;

import static com.dnnthanh.wallet.be.platform.constant.PlatformInvariantMessages.CORRELATION_ID_REQUIRED;

import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

/** Request-scoped correlation context for synchronous execution on the current thread. */
public final class CorrelationContext {
    private static final ThreadLocal<String> CURRENT = new ThreadLocal<>();

    private CorrelationContext() {}

    public static Optional<String> currentId() {
        return Optional.ofNullable(CURRENT.get());
    }

    public static Scope open(String correlationId) {
        Validate.notBlank(correlationId, CORRELATION_ID_REQUIRED);
        String previous = CURRENT.get();
        CURRENT.set(StringUtils.trim(correlationId));
        return () -> {
            if (Objects.isNull(previous)) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        };
    }

    @FunctionalInterface
    public interface Scope extends AutoCloseable {
        @Override
        void close();
    }
}
