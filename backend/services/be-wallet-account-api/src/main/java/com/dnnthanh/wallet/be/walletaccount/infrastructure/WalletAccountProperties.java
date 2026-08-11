package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import java.net.URI;
import java.util.Set;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.account")
public record WalletAccountProperties(URI kycBaseUrl, Set<String> supportedCurrencies) {}
