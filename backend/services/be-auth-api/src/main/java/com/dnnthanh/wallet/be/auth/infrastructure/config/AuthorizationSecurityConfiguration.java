package com.dnnthanh.wallet.be.auth.infrastructure.config;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.application.service.CurrentIdentityServiceImplement;
import com.dnnthanh.wallet.be.platform.security.CurrentAuthorization;
import com.dnnthanh.wallet.be.platform.security.WalletAuthorization;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
public class AuthorizationSecurityConfiguration {
    @Bean(name = "walletAuthorization")
    @RequestScope
    public WalletAuthorization walletAuthorization(
            CurrentIdentityServiceImplement identityService) {
        EffectiveAuthorization authorization = identityService.currentAuthorization();
        return new WalletAuthorization(
                new CurrentAuthorization(authorization.permissions(), authorization.scopes()));
    }
}
