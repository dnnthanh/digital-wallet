package com.dnnthanh.wallet.be.auth.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KeycloakRealmContractTest {
    @Test
    void shouldKeepAuthRealmSecretsExternalAndModelRolePermissionAndScopeHierarchy()
            throws IOException {
        Path realmPath =
                Path.of("..", "..", "..", "infrastructure", "keycloak", "realm-digital-wallet.json")
                        .normalize();
        String realm = Files.readString(realmPath);

        assertThat(realm).contains("${AUTH_KEYCLOAK_CLIENT_SECRET}");
        assertThat(realm).contains("${KEYCLOAK_DEMO_USER_PASSWORD}");
        assertThat(realm).contains("\"clientId\" : \"be-auth-api\"");
        assertThat(realm).contains("\"serviceAccountsEnabled\" : true");
        assertThat(realm).contains("\"fullScopeAllowed\" : true");
        assertThat(realm).contains("permission:self:read");
        assertThat(realm).contains("role:wallet-user");
        assertThat(realm).contains("\"name\" : \"bank\"");
        assertThat(realm).contains("\"name\" : \"demo-branch\"");
        assertThat(realm).doesNotContain("change-me");
    }
}
