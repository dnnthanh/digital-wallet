package com.dnnthanh.wallet.be.kyc.infrastructure;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "wallet.kyc.security")
public record KycSecurityProperties(@NotBlank String documentHmacSecret) {}
