package com.dnnthanh.wallet.be.platform.web.security;

import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import com.dnnthanh.wallet.be.platform.exception.PlatformErrorCode;
import com.dnnthanh.wallet.be.platform.stereotype.Adapter;
import com.dnnthanh.wallet.be.platform.web.error.ApiErrorFactory;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

@Adapter
@RequiredArgsConstructor
public class ApiAccessDeniedHandler implements AccessDeniedHandler {
    private final ApiErrorFactory errorFactory;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException)
            throws IOException {
        response.setStatus(PlatformErrorCode.FORBIDDEN.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.failure(
                        errorFactory.create(
                                PlatformErrorCode.FORBIDDEN, request, request.getLocale())));
    }
}
