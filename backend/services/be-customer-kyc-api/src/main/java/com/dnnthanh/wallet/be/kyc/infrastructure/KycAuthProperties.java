package com.dnnthanh.wallet.be.kyc.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.kyc")
public record KycAuthProperties(String authApiBaseUrl) {}
