package com.dnnthanh.wallet.be.platform.web.logging;

import com.dnnthanh.wallet.be.platform.stereotype.Adapter;
import com.dnnthanh.wallet.be.platform.trace.TraceContextAccessor;
import com.dnnthanh.wallet.be.platform.trace.TraceHeaders;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

@Adapter
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Slf4j
@RequiredArgsConstructor
public class StructuredHttpLoggingFilter extends OncePerRequestFilter {
    private static final Set<String> SENSITIVE_HEADERS =
            Set.of("authorization", "cookie", "set-cookie", "x-api-key", "x-signature");
    private final TraceContextAccessor traceContext;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        long started = System.nanoTime();
        String traceId = traceContext.currentTraceId();
        if (Objects.nonNull(traceId)) response.setHeader(TraceHeaders.TRACE_ID, traceId);
        try (MDC.MDCCloseable ignoredTrace =
                        MDC.putCloseable("traceId", Objects.requireNonNullElse(traceId, ""));
                MDC.MDCCloseable ignoredMethod =
                        MDC.putCloseable("httpMethod", request.getMethod());
                MDC.MDCCloseable ignoredPath =
                        MDC.putCloseable("httpPath", request.getRequestURI())) {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = (System.nanoTime() - started) / 1_000_000;
            log.info(
                    "http_request method={} path={} status={} durationMs={} requestId={} sensitiveHeadersMasked={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    response.getStatus(),
                    durationMs,
                    request.getHeader("X-Request-Id"),
                    containsSensitiveHeader(request));
        }
    }

    private boolean containsSensitiveHeader(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .map(name -> name.toLowerCase(Locale.ROOT))
                .anyMatch(SENSITIVE_HEADERS::contains);
    }
}
