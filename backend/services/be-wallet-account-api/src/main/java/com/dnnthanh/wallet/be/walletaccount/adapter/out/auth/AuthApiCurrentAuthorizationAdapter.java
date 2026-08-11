package com.dnnthanh.wallet.be.walletaccount.adapter.out.auth;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import java.util.Objects;
import java.util.Set;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class AuthApiCurrentAuthorizationAdapter {
    private final RestClient restClient;
    private final CurrentBearerTokenPort bearerTokenPort;

    public AuthApiCurrentAuthorizationAdapter(
            @Qualifier("walletAuthRestClient") RestClient restClient,
            CurrentBearerTokenPort bearerTokenPort) {
        this.restClient = restClient;
        this.bearerTokenPort = bearerTokenPort;
    }

    public CurrentAuthorization currentAuthorization() {
        try {
            String authorization = bearerTokenPort.authorizationHeader();
            AuthApiResponse<AuthPermissionsResponse> permissionsResponse =
                    restClient
                            .get()
                            .uri("/private/api/v1/me/permissions")
                            .header(HttpHeaders.AUTHORIZATION, authorization)
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});
            AuthApiResponse<AuthScopesResponse> scopesResponse =
                    restClient
                            .get()
                            .uri("/private/api/v1/me/scopes")
                            .header(HttpHeaders.AUTHORIZATION, authorization)
                            .retrieve()
                            .body(new ParameterizedTypeReference<>() {});

            AuthPermissionsResponse permissions =
                    Objects.requireNonNull(permissionsResponse).data();
            AuthScopesResponse scopes = Objects.requireNonNull(scopesResponse).data();
            if (Objects.isNull(permissions) || Objects.isNull(scopes)) {
                throw unavailable();
            }
            return new CurrentAuthorization(
                    Objects.requireNonNullElse(permissions.permissions(), Set.of()),
                    Objects.requireNonNullElse(scopes.scopes(), Set.of()));
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw unavailable();
        }
    }

    private static BusinessException unavailable() {
        return new BusinessException(WalletAccountErrorCode.WALLET_AUTHORIZATION_UNAVAILABLE);
    }
}
