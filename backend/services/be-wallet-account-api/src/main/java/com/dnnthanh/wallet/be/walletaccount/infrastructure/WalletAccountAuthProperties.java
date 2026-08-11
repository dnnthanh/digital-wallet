package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import java.net.URI;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.account")
public record WalletAccountAuthProperties(URI authApiBaseUrl) {}
