package com.dnnthanh.wallet.be.auth.adapter.out.external.keycloak.rest;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationDirectoryPort;
import com.dnnthanh.wallet.be.auth.infrastructure.config.KeycloakAuthorizationProperties;
import com.dnnthanh.wallet.be.platform.security.ServiceTokenProvider;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class KeycloakAuthorizationAdapter implements AuthorizationDirectoryPort {
    private final RestClient restClient;
    private final ServiceTokenProvider serviceTokenProvider;
    private final KeycloakAuthorizationProperties properties;
    private final KeycloakAuthorizationMapper mapper;

    public KeycloakAuthorizationAdapter(
            RestClient.Builder restClientBuilder,
            ServiceTokenProvider serviceTokenProvider,
            KeycloakAuthorizationProperties properties,
            KeycloakAuthorizationMapper mapper) {
        this.restClient = restClientBuilder.clone().baseUrl(properties.baseUrl()).build();
        this.serviceTokenProvider = serviceTokenProvider;
        this.properties = properties;
        this.mapper = mapper;
    }

    @Override
    public EffectiveAuthorization resolve(String userId) {
        String accessToken = serviceTokenProvider.token();
        KeycloakRoleRepresentation[] roles =
                restClient
                        .get()
                        .uri(
                                "/admin/realms/{realm}/users/{userId}/role-mappings/realm/composite",
                                properties.realm(),
                                userId)
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .retrieve()
                        .body(KeycloakRoleRepresentation[].class);
        KeycloakGroupRepresentation[] groups =
                restClient
                        .get()
                        .uri(
                                "/admin/realms/{realm}/users/{userId}/groups",
                                properties.realm(),
                                userId)
                        .headers(headers -> headers.setBearerAuth(accessToken))
                        .retrieve()
                        .body(KeycloakGroupRepresentation[].class);
        return mapper.map(userId, list(roles), list(groups));
    }

    private static <T> List<T> list(T[] values) {
        return Objects.isNull(values) ? List.of() : Arrays.asList(values);
    }
}
