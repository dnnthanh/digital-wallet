package com.dnnthanh.wallet.be.platform.context;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

class UserContextConfigurationTest {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldClassifyServiceAccountAsServiceActor() {
        Instant now = Instant.now();
        Jwt jwt =
                Jwt.withTokenValue("test-token")
                        .header("alg", "none")
                        .subject("service-account-be-auth-api")
                        .issuedAt(now)
                        .expiresAt(now.plusSeconds(300))
                        .claim("preferred_username", "service-account-be-auth-api")
                        .claim("realm_access", Map.of("roles", List.of("SERVICE_ACCOUNT")))
                        .build();
        JwtAuthenticationToken authentication =
                new JwtAuthenticationToken(
                        jwt, List.of(new SimpleGrantedAuthority("ROLE_AUTHENTICATED")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserContext context = new UserContextConfiguration().userContext();

        assertThat(context.actorType()).isEqualTo(UserContext.ActorType.SERVICE);
        assertThat(context.roles()).containsExactly("SERVICE_ACCOUNT");
    }
}
