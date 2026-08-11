package com.dnnthanh.wallet.be.walletaccount.adapter.out.kyc;

import com.dnnthanh.wallet.be.platform.exception.BusinessException;
import com.dnnthanh.wallet.be.walletaccount.application.model.VerifiedKyc;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.walletaccount.application.port.out.KycVerificationPort;
import com.dnnthanh.wallet.be.walletaccount.exception.WalletAccountErrorCode;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class KycVerificationRestAdapter implements KycVerificationPort {
    private static final String VERIFIED = "VERIFIED";

    private final RestClient restClient;
    private final CurrentBearerTokenPort bearerTokenPort;

    public KycVerificationRestAdapter(
            @Qualifier("walletKycRestClient") RestClient restClient,
            CurrentBearerTokenPort bearerTokenPort) {
        this.restClient = restClient;
        this.bearerTokenPort = bearerTokenPort;
    }

    @Override
    public VerifiedKyc requireVerified(String expectedUserId) {
        try {
            KycApiResponse response =
                    restClient
                            .get()
                            .uri("/private/api/v1/kyc/me")
                            .header(
                                    HttpHeaders.AUTHORIZATION,
                                    bearerTokenPort.authorizationHeader())
                            .retrieve()
                            .body(KycApiResponse.class);
            KycResponseData data = Objects.requireNonNull(response).data();
            if (Objects.isNull(data)) {
                throw unavailable();
            }
            if (!StringUtils.equals(expectedUserId, data.userId())) {
                throw unavailable();
            }
            if (!StringUtils.equals(VERIFIED, data.status())) {
                throw new BusinessException(WalletAccountErrorCode.WALLET_KYC_REQUIRED);
            }
            if (StringUtils.isBlank(data.scopePath())) {
                throw unavailable();
            }
            return new VerifiedKyc(data.userId(), data.scopePath());
        } catch (HttpClientErrorException.NotFound exception) {
            throw new BusinessException(WalletAccountErrorCode.WALLET_KYC_REQUIRED);
        } catch (BusinessException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw unavailable();
        }
    }

    private static BusinessException unavailable() {
        return new BusinessException(WalletAccountErrorCode.WALLET_KYC_UNAVAILABLE);
    }
}
