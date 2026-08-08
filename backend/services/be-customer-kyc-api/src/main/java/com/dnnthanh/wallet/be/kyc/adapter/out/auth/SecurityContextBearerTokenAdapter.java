package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope
public class SecurityContextBearerTokenAdapter implements CurrentBearerTokenPort {
    @Override
    public String authorizationHeader() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtAuthentication) {
            return "Bearer " + jwtAuthentication.getToken().getTokenValue();
        }
        throw new BusinessException(KycErrorCode.KYC_AUTHORIZATION_UNAVAILABLE);
    }
}
