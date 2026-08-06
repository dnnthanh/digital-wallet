package com.dnnthanh.wallet.be.kyc.adapter.out.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.kyc.exception.KycErrorCode;
import com.dnnthanh.wallet.be.kyc.infrastructure.KycAuthProperties;
import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

@ExtendWith(MockitoExtension.class)
class AuthApiCurrentAuthorizationAdapterTest {
    @Mock private CurrentBearerTokenPort bearerTokenPort;

    private MockRestServiceServer server;
    private AuthApiCurrentAuthorizationAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        adapter =
                new AuthApiCurrentAuthorizationAdapter(
                        builder, new KycAuthProperties("http://auth.test"), bearerTokenPort);
        when(bearerTokenPort.authorizationHeader()).thenReturn("Bearer access-token");
    }

    @Test
    void relaysBearerAndCombinesEffectivePermissionsAndScopes() {
        server.expect(requestTo("http://auth.test/api/v1/me/permissions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"permissions\":[\"kyc:review\",\"kyc:self:read\"]},\"metadata\":null,\"error\":null}",
                                MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://auth.test/api/v1/me/scopes"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"scopes\":[\"/bank\",\"/bank/demo-branch\"]},\"metadata\":null,\"error\":null}",
                                MediaType.APPLICATION_JSON));

        CurrentAuthorization authorization = adapter.currentAuthorization();

        assertThat(authorization.permissions()).containsExactlyInAnyOrder("kyc:review", "kyc:self:read");
        assertThat(authorization.scopes()).containsExactlyInAnyOrder("/bank", "/bank/demo-branch");
        server.verify();
    }

    @Test
    void dependencyFailureNeverBecomesAuthorizationSuccess() {
        server.expect(requestTo("http://auth.test/api/v1/me/permissions"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(adapter::currentAuthorization)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(KycErrorCode.KYC_AUTHORIZATION_UNAVAILABLE));
        server.verify();
    }
}
