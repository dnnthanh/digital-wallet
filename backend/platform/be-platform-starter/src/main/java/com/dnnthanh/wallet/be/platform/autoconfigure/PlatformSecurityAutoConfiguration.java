package com.dnnthanh.wallet.be.platform.autoconfigure;

import com.dnnthanh.wallet.be.platform.context.UserContextConfiguration;
import com.dnnthanh.wallet.be.platform.i18n.SpringMessageResolver;
import com.dnnthanh.wallet.be.platform.security.SecurityConfiguration;
import com.dnnthanh.wallet.be.platform.web.error.ApiErrorFactory;
import com.dnnthanh.wallet.be.platform.web.error.GlobalExceptionHandler;
import com.dnnthanh.wallet.be.platform.web.security.ApiAccessDeniedHandler;
import com.dnnthanh.wallet.be.platform.web.security.ApiAuthenticationEntryPoint;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/** Shared servlet security, identity and stable API error handling for wallet services. */
@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Import({
    SecurityConfiguration.class,
    UserContextConfiguration.class,
    SpringMessageResolver.class,
    ApiErrorFactory.class,
    GlobalExceptionHandler.class,
    ApiAuthenticationEntryPoint.class,
    ApiAccessDeniedHandler.class
})
public class PlatformSecurityAutoConfiguration {}
