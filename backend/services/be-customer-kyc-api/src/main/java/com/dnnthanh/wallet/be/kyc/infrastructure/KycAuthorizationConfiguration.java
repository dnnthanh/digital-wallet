package com.dnnthanh.wallet.be.kyc.infrastructure;

import com.dnnthanh.wallet.be.kyc.adapter.out.auth.AuthApiCurrentAuthorizationAdapter;
import com.dnnthanh.wallet.be.kyc.adapter.out.auth.RequestKycAuthorization;
import com.dnnthanh.wallet.be.kyc.adapter.out.security.HmacDocumentFingerprintAdapter;
import com.dnnthanh.wallet.be.kyc.application.port.out.CurrentBearerTokenPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.DocumentFingerprintPort;
import com.dnnthanh.wallet.be.kyc.application.port.out.KycAuthorizationPort;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.platform.security.WalletAuthorization;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class KycAuthorizationConfiguration {
    @Bean
    AuthApiCurrentAuthorizationAdapter authApiCurrentAuthorizationAdapter(
            RestClient.Builder builder,
            KycAuthProperties properties,
            CurrentBearerTokenPort bearerTokenPort) {
        return new AuthApiCurrentAuthorizationAdapter(builder, properties, bearerTokenPort);
    }

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.NO)
    CurrentAuthorization currentAuthorization(AuthApiCurrentAuthorizationAdapter adapter) {
        return adapter.currentAuthorization();
    }

    @Bean(name = "walletAuthorization")
    @RequestScope(proxyMode = ScopedProxyMode.NO)
    WalletAuthorization walletAuthorization(CurrentAuthorization currentAuthorization) {
        return new WalletAuthorization(currentAuthorization);
    }

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.INTERFACES)
    KycAuthorizationPort kycAuthorizationPort(CurrentAuthorization currentAuthorization) {
        return new RequestKycAuthorization(currentAuthorization);
    }

    @Bean
    DocumentFingerprintPort documentFingerprintPort(KycSecurityProperties properties) {
        return new HmacDocumentFingerprintAdapter(properties.documentHmacSecret());
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
