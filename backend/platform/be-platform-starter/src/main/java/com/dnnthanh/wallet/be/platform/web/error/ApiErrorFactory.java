package com.dnnthanh.wallet.be.platform.web.error;

import com.dnnthanh.wallet.be.platform.api.ApiError;
import com.dnnthanh.wallet.be.platform.exception.ErrorCode;
import com.dnnthanh.wallet.be.platform.i18n.MessageResolver;
import com.dnnthanh.wallet.be.platform.trace.TraceContextAccessor;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Component
@RequiredArgsConstructor
public class ApiErrorFactory {
    private final MessageResolver messageResolver;
    private final TraceContextAccessor traceContext;

    public ApiError create(ErrorCode errorCode, HttpServletRequest request) {
        return create(errorCode, request, request.getLocale(), null, null, null);
    }

    public ApiError create(ErrorCode errorCode, HttpServletRequest request, Locale locale) {
        return create(errorCode, request, locale, null, null, null);
    }

    public ApiError create(
            ErrorCode errorCode,
            HttpServletRequest request,
            Locale locale,
            Object[] messageArguments,
            List<ApiError.FieldError> fieldErrors,
            Map<String, Object> details) {
        String message =
                ArrayUtils.isEmpty(messageArguments)
                        ? messageResolver.resolve(errorCode, locale)
                        : messageResolver.resolve(errorCode, locale, messageArguments);

        return new ApiError(
                errorCode.getCode(),
                message,
                traceContext.currentTraceId(),
                LocalDateTime.now(),
                request.getRequestURI(),
                CollectionUtils.isEmpty(fieldErrors) ? null : List.copyOf(fieldErrors),
                MapUtils.isEmpty(details) ? null : Map.copyOf(details));
    }
}
