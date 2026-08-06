package com.dnnthanh.wallet.be.auth.adapter.out.external.keycloak.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.infrastructure.config.KeycloakAuthorizationProperties;
import com.dnnthanh.wallet.be.platform.security.ServiceTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class KeycloakAuthorizationAdapterTest {
    @Test
    void shouldUseServiceBearerTokenAndResolveEffectiveAuthorization() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        ServiceTokenProvider tokenProvider = mock(ServiceTokenProvider.class);
        when(tokenProvider.token()).thenReturn("service-token");
        KeycloakAuthorizationAdapter adapter =
                new KeycloakAuthorizationAdapter(
                        builder,
                        tokenProvider,
                        new KeycloakAuthorizationProperties(
                                "http://keycloak:8080", "digital-wallet"),
                        new KeycloakAuthorizationMapper());

        server.expect(
                        requestTo(
                                "http://keycloak:8080/admin/realms/digital-wallet/users/user-1/role-mappings/realm/composite"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer service-token"))
                .andRespond(
                        withSuccess(
                                "[{\"name\":\"role:wallet-user\"},{\"name\":\"permission:self:read\"}]",
                                MediaType.APPLICATION_JSON));
        server.expect(
                        requestTo(
                                "http://keycloak:8080/admin/realms/digital-wallet/users/user-1/groups"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(AUTHORIZATION, "Bearer service-token"))
                .andRespond(
                        withSuccess(
                                "[{\"path\":\"/bank/demo-branch\"}]", MediaType.APPLICATION_JSON));

        EffectiveAuthorization authorization = adapter.resolve("user-1");

        assertThat(authorization.permissions()).containsExactly("self:read");
        assertThat(authorization.scopes()).containsExactly("/bank/demo-branch");
        server.verify();
    }
}
