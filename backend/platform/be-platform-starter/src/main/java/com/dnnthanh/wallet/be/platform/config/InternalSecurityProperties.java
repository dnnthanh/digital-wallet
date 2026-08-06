package com.dnnthanh.wallet.be.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "wallet.internal-security")
public class InternalSecurityProperties {
    private String tokenUri;
    private String clientId;
    private String clientSecret;
    private String requiredAudience;
    private String requiredServiceRole;
}
