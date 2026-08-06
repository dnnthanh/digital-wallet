package com.dnnthanh.wallet.be.platform.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

@Configuration(proxyBeanMethods = false)
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class PlatformPageableConfiguration {
    @Bean
    PageableHandlerMethodArgumentResolverCustomizer
            pageableHandlerMethodArgumentResolverCustomizer() {
        return resolver -> resolver.setMaxPageSize(100);
    }
}
