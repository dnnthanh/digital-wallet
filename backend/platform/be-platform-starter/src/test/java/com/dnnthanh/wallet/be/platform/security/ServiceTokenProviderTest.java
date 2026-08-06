package com.dnnthanh.wallet.be.platform.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class ServiceTokenProviderTest {
    @Test
    void shouldUseSignedClientAssertionWithoutClientSecret() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        InternalSecurityProperties properties = new InternalSecurityProperties();
        properties.setTokenUri("http://keycloak/token");
        properties.setClientId("be-auth-api");
        ClientAssertionProvider assertionProvider = mock(ClientAssertionProvider.class);
        when(assertionProvider.assertion()).thenReturn("signed-assertion");
        ServiceTokenProvider provider =
                new ServiceTokenProvider(builder, properties, assertionProvider);

        server.expect(requestTo("http://keycloak/token"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(
                        content()
                                .string(
                                        allOf(
                                                containsString("grant_type=client_credentials"),
                                                containsString("client_id=be-auth-api"),
                                                containsString("client_assertion=signed-assertion"),
                                                containsString("client_assertion_type="),
                                                not(containsString("client_secret")))))
                .andRespond(
                        withSuccess(
                                "{\"access_token\":\"service-token\",\"expires_in\":60}",
                                MediaType.APPLICATION_JSON));

        assertThat(provider.token()).isEqualTo("service-token");
        server.verify();
    }
}
