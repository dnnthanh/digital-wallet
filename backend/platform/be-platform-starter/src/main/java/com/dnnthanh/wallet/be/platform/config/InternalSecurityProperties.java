package com.dnnthanh.wallet.be.platform.config;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "wallet.internal-security")
public class InternalSecurityProperties {
    private String tokenUri;
    private String clientId;
    private String privateKeyLocation;
    private String keyId = "be-auth-api-key-1";
    private Duration assertionTtl = Duration.ofSeconds(30);
    private String clientAssertionAudience;
    private String requiredAudience;
    private String requiredServiceRole;
}
