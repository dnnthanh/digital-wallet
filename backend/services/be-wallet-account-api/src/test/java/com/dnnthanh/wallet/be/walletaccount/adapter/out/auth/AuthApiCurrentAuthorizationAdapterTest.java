package com.dnnthanh.wallet.be.walletaccount.adapter.out.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
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
        adapter = new AuthApiCurrentAuthorizationAdapter(builder.baseUrl("http://auth.test").build(), bearerTokenPort);
        when(bearerTokenPort.authorizationHeader()).thenReturn("Bearer access-token");
    }

    @Test
    void resolvesEffectivePermissionsAndScopesWithCurrentBearer() {
        server.expect(requestTo("http://auth.test/private/api/v1/me/permissions"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(withSuccess("{\"data\":{\"permissions\":[\"WALLET_SELF_READ\"]}}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://auth.test/private/api/v1/me/scopes"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(withSuccess("{\"data\":{\"scopes\":[\"/bank/demo-branch\"]}}", MediaType.APPLICATION_JSON));

        var authorization = adapter.currentAuthorization();

        assertThat(authorization.permissions()).containsExactly("WALLET_SELF_READ");
        assertThat(authorization.scopes()).containsExactly("/bank/demo-branch");
        server.verify();
    }

    @Test
    void authDependencyFailureFailsClosed() {
        server.expect(requestTo("http://auth.test/private/api/v1/me/permissions"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertThatThrownBy(adapter::currentAuthorization)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception ->
                                assertThat(exception.getErrorCode())
                                        .isEqualTo(WalletAccountErrorCode.WALLET_AUTHORIZATION_UNAVAILABLE));
        server.verify();
    }
}
