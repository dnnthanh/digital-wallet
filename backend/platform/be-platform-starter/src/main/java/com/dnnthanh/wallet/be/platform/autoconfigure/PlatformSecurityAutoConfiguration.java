package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.config.InternalSecurityProperties;
import com.dnnthanh.wallet.be.platform.context.UserContextConfiguration;
import com.dnnthanh.wallet.be.platform.i18n.SpringMessageResolver;
import com.dnnthanh.wallet.be.platform.security.SecurityConfiguration;
import com.dnnthanh.wallet.be.platform.security.ServiceTokenProvider;
import com.dnnthanh.wallet.be.platform.web.error.ApiErrorFactory;
import com.dnnthanh.wallet.be.platform.web.error.GlobalExceptionHandler;
import com.dnnthanh.wallet.be.platform.web.security.ApiAccessDeniedHandler;
import com.dnnthanh.wallet.be.platform.web.security.ApiAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/** Shared servlet security, identity and stable API error handling for wallet services. */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(InternalSecurityProperties.class)
@Import({
    SecurityConfiguration.class,
    UserContextConfiguration.class,
    SpringMessageResolver.class,
    ApiErrorFactory.class,
    GlobalExceptionHandler.class,
    ApiAuthenticationEntryPoint.class,
    ApiAccessDeniedHandler.class,
    ServiceTokenProvider.class
})
public class PlatformSecurityAutoConfiguration {}
