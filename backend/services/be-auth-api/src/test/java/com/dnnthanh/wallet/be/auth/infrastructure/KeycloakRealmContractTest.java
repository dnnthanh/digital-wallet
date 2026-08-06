package com.dnnthanh.wallet.be.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KeycloakRealmContractTest {
    @Test
    void shouldUseSignedJwtClientAuthenticationAndModelRolePermissionAndScopeHierarchy()
            throws IOException {
        Path realmPath =
                Path.of("..", "..", "..", "infrastructure", "keycloak", "realm-digital-wallet.json")
                        .normalize();
        String realm = Files.readString(realmPath);

        assertThat(realm).contains("${KEYCLOAK_DEMO_USER_PASSWORD}");
        assertThat(realm).contains("\"clientId\" : \"be-auth-api\"");
        assertThat(realm).contains("\"clientAuthenticatorType\" : \"client-jwt\"");
        assertThat(realm).contains("\"serviceAccountsEnabled\" : true");
        assertThat(realm).contains("\"fullScopeAllowed\" : true");
        assertThat(realm).contains("\"use.jwks.url\" : \"true\"");
        assertThat(realm)
                .contains(
                        "\"jwks.url\" : \"http://be-auth-api:8080/.well-known/wallet-client-jwks.json\"");
        assertThat(realm).doesNotContain("AUTH_KEYCLOAK_CLIENT_SECRET");
        assertThat(realm).doesNotContain("\"secret\"");
        assertThat(realm).contains("permission:self:read");
        assertThat(realm).contains("role:wallet-user");
        assertThat(realm).contains("\"name\" : \"bank\"");
        assertThat(realm).contains("\"name\" : \"demo-branch\"");
        assertThat(realm).doesNotContain("change-me");
    }
}
