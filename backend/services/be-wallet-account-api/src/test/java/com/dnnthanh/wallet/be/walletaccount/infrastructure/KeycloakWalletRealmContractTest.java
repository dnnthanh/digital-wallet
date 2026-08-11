package com.dnnthanh.wallet.be.walletaccount.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KeycloakWalletRealmContractTest {
    @Test
    void realmDefinesWalletSelfPermissionsAndCompositeRole() throws IOException {
        Path realmPath =
                Path.of("..", "..", "..", "infrastructure", "keycloak", "realm-digital-wallet.json")
                        .normalize();
        String realm = Files.readString(realmPath);

        assertThat(realm).contains("permission:WALLET_SELF_CREATE");
        assertThat(realm).contains("permission:WALLET_SELF_READ");
        assertThat(realm).contains("role:wallet-user");
        assertThat(realm).contains("${KEYCLOAK_DEMO_USER_PASSWORD}");
        assertThat(realm).doesNotContain("permission:wallet:self:create");
        assertThat(realm).doesNotContain("permission:wallet:self:read");
        assertThat(realm).doesNotContain("WALLET_DB_PASSWORD");
        assertThat(realm).doesNotContain("change-me");
    }
}
