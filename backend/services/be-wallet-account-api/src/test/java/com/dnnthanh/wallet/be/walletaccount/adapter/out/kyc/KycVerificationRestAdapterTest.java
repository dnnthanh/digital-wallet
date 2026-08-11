package com.dnnthanh.wallet.be.walletaccount.adapter.out.kyc;

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
class KycVerificationRestAdapterTest {
    @Mock private CurrentBearerTokenPort bearerTokenPort;

    private MockRestServiceServer server;
    private KycVerificationRestAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.baseUrl("http://kyc.test").build();
        adapter = new KycVerificationRestAdapter(restClient, bearerTokenPort);
        when(bearerTokenPort.authorizationHeader()).thenReturn("Bearer access-token");
    }

    @Test
    void verifiedKycReturnsOwnedScopeAndRelaysBearer() {
        server.expect(requestTo("http://kyc.test/private/api/v1/kyc/me"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"userId\":\"user-1\",\"scopePath\":\"/bank/demo-branch\",\"status\":\"VERIFIED\"},\"metadata\":null,\"error\":null}",
                                MediaType.APPLICATION_JSON));

        var result = adapter.requireVerified("user-1");

        assertThat(result.userId()).isEqualTo("user-1");
        assertThat(result.scopePath()).isEqualTo("/bank/demo-branch");
        server.verify();
    }

    @Test
    void nonVerifiedKycIsRejectedAsBusinessPrerequisite() {
        server.expect(requestTo("http://kyc.test/private/api/v1/kyc/me"))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"userId\":\"user-1\",\"scopePath\":\"/bank/demo-branch\",\"status\":\"PENDING_REVIEW\"}}",
                                MediaType.APPLICATION_JSON));

        assertError(() -> adapter.requireVerified("user-1"), WalletAccountErrorCode.WALLET_KYC_REQUIRED);
        server.verify();
    }

    @Test
    void mismatchedSubjectFailsClosed() {
        server.expect(requestTo("http://kyc.test/private/api/v1/kyc/me"))
                .andRespond(
                        withSuccess(
                                "{\"data\":{\"userId\":\"other-user\",\"scopePath\":\"/bank/demo-branch\",\"status\":\"VERIFIED\"}}",
                                MediaType.APPLICATION_JSON));

        assertError(() -> adapter.requireVerified("user-1"), WalletAccountErrorCode.WALLET_KYC_UNAVAILABLE);
        server.verify();
    }

    @Test
    void dependencyFailureFailsClosed() {
        server.expect(requestTo("http://kyc.test/private/api/v1/kyc/me"))
                .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE));

        assertError(() -> adapter.requireVerified("user-1"), WalletAccountErrorCode.WALLET_KYC_UNAVAILABLE);
        server.verify();
    }

    private static void assertError(Runnable operation, WalletAccountErrorCode errorCode) {
        assertThatThrownBy(operation::run)
                .isInstanceOfSatisfying(
                        BusinessException.class,
                        exception -> assertThat(exception.getErrorCode()).isEqualTo(errorCode));
    }
}
