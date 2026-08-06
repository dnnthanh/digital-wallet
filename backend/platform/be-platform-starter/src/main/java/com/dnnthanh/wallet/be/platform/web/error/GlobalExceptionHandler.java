package com.dnnthanh.wallet.be.platform.web.error;

import com.dnnthanh.wallet.be.platform.api.ApiError;
import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.exception.ErrorCode;
import com.dnnthanh.wallet.be.platform.exception.PlatformErrorCode;
import com.dnnthanh.wallet.be.platform.i18n.MessageResolver;
import com.dnnthanh.wallet.be.platform.trace.TraceContextAccessor;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice(basePackages = "com.dnnthanh.wallet.be")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@RequiredArgsConstructor
@Slf4j
public class GlobalExceptionHandler {
    private final ApiErrorFactory errorFactory;
    private final MessageResolver messageResolver;
    private final TraceContextAccessor traceContext;

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusiness(
            BusinessException exception, HttpServletRequest request, Locale locale) {
        return response(
                exception.getErrorCode(),
                request,
                locale,
                exception.getMessageArguments(),
                null,
                exception.getDetails());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidation(
            MethodArgumentNotValidException exception, HttpServletRequest request, Locale locale) {
        List<ApiError.FieldError> fields =
                exception.getBindingResult().getFieldErrors().stream()
                        .map(error -> fieldError(error, locale))
                        .toList();
        return response(PlatformErrorCode.VALIDATION_FAILED, request, locale, null, fields, null);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodValidation(
            HandlerMethodValidationException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.VALIDATION_FAILED, request, locale);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiResponse<Void>> handleConstraintViolation(
            ConstraintViolationException exception, HttpServletRequest request, Locale locale) {
        List<ApiError.FieldError> fields =
                exception.getConstraintViolations().stream()
                        .map(
                                v ->
                                        new ApiError.FieldError(
                                                v.getPropertyPath().toString(), v.getMessage()))
                        .toList();
        return response(PlatformErrorCode.VALIDATION_FAILED, request, locale, null, fields, null);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(
            Exception exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.BAD_REQUEST, request, locale);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(
            AuthenticationException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.UNAUTHORIZED, request, locale);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleForbidden(
            AccessDeniedException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.FORBIDDEN, request, locale);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(
            NoResourceFoundException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.NOT_FOUND, request, locale);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request,
            Locale locale) {
        return response(PlatformErrorCode.METHOD_NOT_ALLOWED, request, locale);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnsupportedMediaType(
            HttpMediaTypeNotSupportedException exception,
            HttpServletRequest request,
            Locale locale) {
        return response(PlatformErrorCode.UNSUPPORTED_MEDIA_TYPE, request, locale);
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotAcceptable(
            HttpMediaTypeNotAcceptableException exception,
            HttpServletRequest request,
            Locale locale) {
        return response(PlatformErrorCode.NOT_ACCEPTABLE, request, locale);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<Void>> handlePayloadTooLarge(
            MaxUploadSizeExceededException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.PAYLOAD_TOO_LARGE, request, locale);
    }

    @ExceptionHandler({
        DataIntegrityViolationException.class,
        OptimisticLockingFailureException.class,
        PessimisticLockingFailureException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleDataConflict(
            RuntimeException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.DATA_CONFLICT, request, locale);
    }

    @ExceptionHandler(QueryTimeoutException.class)
    public ResponseEntity<ApiResponse<Void>> handleQueryTimeout(
            QueryTimeoutException exception, HttpServletRequest request, Locale locale) {
        return response(PlatformErrorCode.DEPENDENCY_TIMEOUT, request, locale);
    }

    @ExceptionHandler({
        IllegalArgumentException.class,
        IllegalStateException.class,
        NullPointerException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleInvariantViolation(
            RuntimeException exception, HttpServletRequest request, Locale locale) {
        log.error(
                "Platform invariant violation method={} path={} traceId={}",
                request.getMethod(),
                request.getRequestURI(),
                traceContext.currentTraceId(),
                exception);
        return response(PlatformErrorCode.INTERNAL_ERROR, request, locale);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleUnexpected(
            Exception exception, HttpServletRequest request, Locale locale) {
        log.error(
                "Unhandled API exception method={} path={} traceId={}",
                request.getMethod(),
                request.getRequestURI(),
                traceContext.currentTraceId(),
                exception);
        return response(PlatformErrorCode.INTERNAL_ERROR, request, locale);
    }

    private ResponseEntity<ApiResponse<Void>> response(
            ErrorCode errorCode, HttpServletRequest request, Locale locale) {
        return response(errorCode, request, locale, null, null, null);
    }

    private ResponseEntity<ApiResponse<Void>> response(
            ErrorCode errorCode,
            HttpServletRequest request,
            Locale locale,
            Object[] messageArguments,
            List<ApiError.FieldError> fieldErrors,
            java.util.Map<String, Object> details) {
        ApiError error =
                errorFactory.create(
                        errorCode, request, locale, messageArguments, fieldErrors, details);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(ApiResponse.failure(error));
    }

    private ApiError.FieldError fieldError(FieldError error, Locale locale) {
        return new ApiError.FieldError(error.getField(), messageResolver.resolve(error, locale));
    }
}
