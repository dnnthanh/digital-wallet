package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.platform.api.ApiResponse;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AuthApiCurrentAuthorizationAdapter {
    private final RestClient restClient;
    private final CurrentBearerTokenPort bearerTokenPort;

    public CurrentAuthorization currentAuthorization() {
        try {
            return new CurrentAuthorization(
                    Set.copyOf(loadPermissions()), Set.copyOf(loadScopes()));
        } catch (RuntimeException failure) {
            throw new BusinessException(KycErrorCode.KYC_AUTHORIZATION_UNAVAILABLE);
        }
    }

    private Set<String> loadPermissions() {
        ApiResponse<AuthPermissionsResponse> response =
                restClient
                        .get()
                        .uri("/private/api/v1/me/permissions")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenPort.authorizationHeader())
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
        return Objects.requireNonNull(Objects.requireNonNull(response).data()).permissions();
    }

    private Set<String> loadScopes() {
        ApiResponse<AuthScopesResponse> response =
                restClient
                        .get()
                        .uri("/private/api/v1/me/scopes")
                        .header(HttpHeaders.AUTHORIZATION, bearerTokenPort.authorizationHeader())
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {});
        return Objects.requireNonNull(Objects.requireNonNull(response).data()).scopes();
    }
}
