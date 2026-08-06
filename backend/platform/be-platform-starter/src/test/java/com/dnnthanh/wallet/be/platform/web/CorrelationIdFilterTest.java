package com.dnnthanh.wallet.be.platform.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationIdFilterTest {
    @Test
    void echoesProvidedCorrelationId() throws Exception {
        var request = new MockHttpServletRequest();
        request.addHeader(CorrelationIdFilter.HEADER_NAME, "corr-123");
        var response = new MockHttpServletResponse();
        FilterChain chain =
                (req, res) -> assertThat(CorrelationContext.currentId()).contains("corr-123");
        new CorrelationIdFilter().doFilter(request, response, chain);
        assertThat(response.getHeader(CorrelationIdFilter.HEADER_NAME)).isEqualTo("corr-123");
        assertThat(CorrelationContext.currentId()).isEmpty();
    }
}
