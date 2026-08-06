package com.dnnthanh.wallet.be.auth.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "wallet.auth.keycloak")
public record KeycloakAuthorizationProperties(String baseUrl, String realm) {}
