package com.dnnthanh.wallet.be.platform.trace;

import io.micrometer.tracing.Tracer;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;

@RequiredArgsConstructor
public class TraceContextAccessor {
    private final ObjectProvider<Tracer> tracerProvider;

    public String currentTraceId() {
        Tracer tracer = tracerProvider.getIfAvailable();
        if (Objects.isNull(tracer)) {
            return null;
        }
        var currentSpan = tracer.currentSpan();
        return Objects.isNull(currentSpan) ? null : currentSpan.context().traceId();
    }
}
