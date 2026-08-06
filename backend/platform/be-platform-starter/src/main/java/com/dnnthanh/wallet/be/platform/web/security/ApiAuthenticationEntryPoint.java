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
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import tools.jackson.databind.ObjectMapper;

@Adapter
@RequiredArgsConstructor
public class ApiAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final ApiErrorFactory errorFactory;
    private final ObjectMapper objectMapper;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException)
            throws IOException {
        response.setStatus(PlatformErrorCode.UNAUTHORIZED.getHttpStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                ApiResponse.failure(
                        errorFactory.create(
                                PlatformErrorCode.UNAUTHORIZED, request, request.getLocale())));
    }
}
