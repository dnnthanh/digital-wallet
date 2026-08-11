package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import java.time.Clock;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class WalletAccountConfiguration {
    @Bean
    public Clock walletAccountClock() {
        return Clock.systemUTC();
    }

    @Bean
    @Qualifier("walletKycRestClient")
    public RestClient walletKycRestClient(
            RestClient.Builder builder, WalletAccountProperties properties) {
        return builder.baseUrl(properties.kycBaseUrl()).build();
    }
}
