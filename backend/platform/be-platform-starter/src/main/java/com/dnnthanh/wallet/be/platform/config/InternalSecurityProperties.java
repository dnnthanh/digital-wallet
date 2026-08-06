package com.dnnthanh.wallet.be.platform.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "wallet.internal-security")
@Getter
@Setter
public class InternalSecurityProperties {
    private String tokenUri;
    private String clientId;
    private String clientSecret;
}
