package com.dnnthanh.wallet.be.platform.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

public final class CorrelationIdFilter extends OncePerRequestFilter {
    public static final String HEADER_NAME = "X-Correlation-Id";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String provided = request.getHeader(HEADER_NAME);
        String correlationId =
                StringUtils.defaultIfBlank(
                        StringUtils.trim(provided), UUID.randomUUID().toString());
        response.setHeader(HEADER_NAME, correlationId);
        try (var ignored = CorrelationContext.open(correlationId)) {
            filterChain.doFilter(request, response);
        }
    }
}
