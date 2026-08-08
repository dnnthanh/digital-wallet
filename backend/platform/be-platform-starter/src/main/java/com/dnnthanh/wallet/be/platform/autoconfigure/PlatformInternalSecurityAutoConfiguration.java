package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.wallet.be.platform.security.RsaPrivateKeyClientAssertionProvider;
import com.dnnthanh.wallet.be.platform.security.ServiceTokenProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Non-web service identity support for HTTP APIs, Kafka workers and scheduled jobs. */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(InternalSecurityProperties.class)
@Import({RsaPrivateKeyClientAssertionProvider.class, ServiceTokenProvider.class})
public class PlatformInternalSecurityAutoConfiguration {}
