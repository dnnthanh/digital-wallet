package com.dnnthanh.wallet.be.platform.api;

import java.util.Objects;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

/** Wraps normal JSON controller responses in the shared API envelope. */
@RestControllerAdvice(basePackages = "com.dnnthanh.wallet.be")
public class ApiResponseBodyAdvice implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(
            MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        Class<?> type = returnType.getParameterType();
        return Objects.isNull(returnType.getMethodAnnotation(RawResponse.class))
                && type != String.class
                && type != byte[].class
                && !Resource.class.isAssignableFrom(type)
                && !StreamingResponseBody.class.isAssignableFrom(type);
    }

    @Override
    public Object beforeBodyWrite(
            Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        if (!MediaType.APPLICATION_JSON.isCompatibleWith(selectedContentType)) return body;
        if (body instanceof ApiResponse<?>) return body;
        if (body instanceof Page<?> page)
            return ApiResponse.success(page.getContent(), PageMetadata.from(page));
        return ApiResponse.success(body);
    }
}
