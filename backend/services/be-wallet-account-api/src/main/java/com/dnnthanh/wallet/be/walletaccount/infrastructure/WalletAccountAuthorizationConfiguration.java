package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.platform.security.WalletAuthorization;
import com.dnnthanh.wallet.be.walletaccount.adapter.out.auth.AuthApiCurrentAuthorizationAdapter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class WalletAccountAuthorizationConfiguration {
    @Bean
    @Qualifier("walletAuthRestClient")
    public RestClient walletAuthRestClient(
            RestClient.Builder builder, WalletAccountAuthProperties properties) {
        return builder.baseUrl(properties.authApiBaseUrl()).build();
    }

    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.NO)
    public CurrentAuthorization currentAuthorization(AuthApiCurrentAuthorizationAdapter adapter) {
        return adapter.currentAuthorization();
    }

    @Bean(name = "walletAuthorization")
    @RequestScope(proxyMode = ScopedProxyMode.NO)
    public WalletAuthorization walletAuthorization(CurrentAuthorization currentAuthorization) {
        return new WalletAuthorization(currentAuthorization);
    }
}
