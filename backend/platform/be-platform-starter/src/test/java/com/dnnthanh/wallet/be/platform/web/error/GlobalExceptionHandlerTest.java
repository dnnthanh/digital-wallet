package com.dnnthanh.wallet.be.platform.web.error;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.exception.PlatformErrorCode;
import com.dnnthanh.wallet.be.platform.i18n.MessageResolver;
import com.dnnthanh.wallet.be.platform.trace.TraceContextAccessor;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

class GlobalExceptionHandlerTest {
    private final MessageResolver messageResolver = mock(MessageResolver.class);
    private final TraceContextAccessor traceContext = mock(TraceContextAccessor.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final ApiErrorFactory errorFactory = new ApiErrorFactory(messageResolver, traceContext);
    private final GlobalExceptionHandler handler =
            new GlobalExceptionHandler(errorFactory, messageResolver, traceContext);

    @BeforeEach
    void setUp() {
        when(request.getMethod()).thenReturn("POST");
        when(request.getRequestURI()).thenReturn("/api/v1/test");
        when(traceContext.currentTraceId()).thenReturn("trace-123");
        when(messageResolver.resolve(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY, Locale.ENGLISH))
                .thenReturn("Invalid idempotency key");
        when(messageResolver.resolve(PlatformErrorCode.INTERNAL_ERROR, Locale.ENGLISH))
                .thenReturn("Internal error");
    }

    @Test
    void mapsTypedIdempotencyValidationToBadRequest() {
        var response =
                handler.handleBusiness(
                        new BusinessException(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY),
                        request,
                        Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code())
                .isEqualTo(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY.getCode());
    }

    @Test
    void mapsProgrammerPreconditionExceptionsExplicitlyToLocalizedInternalError() {
        var response =
                handler.handleInvariantViolation(
                        new NullPointerException("required dependency"), request, Locale.ENGLISH);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().error().code())
                .isEqualTo(PlatformErrorCode.INTERNAL_ERROR.getCode());
        assertThat(response.getBody().error().message()).isEqualTo("Internal error");
    }
}
