package com.dnnthanh.wallet.be.kyc.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class KeycloakKycRealmContractTest {
    @Test
    void realmDefinesKycPermissionsAndCompositeRolesWithoutSecrets() throws IOException {
        Path realmPath =
                Path.of("..", "..", "..", "infrastructure", "keycloak", "realm-digital-wallet.json")
                        .normalize();
        String realm = Files.readString(realmPath);

        assertThat(realm).contains("permission:KYC_SELF_READ");
        assertThat(realm).contains("permission:KYC_SELF_WRITE");
        assertThat(realm).contains("permission:KYC_SELF_SUBMIT");
        assertThat(realm).contains("permission:KYC_REVIEW");
        assertThat(realm).contains("role:wallet-user");
        assertThat(realm).contains("role:kyc-reviewer");
        assertThat(realm).contains("${KEYCLOAK_DEMO_USER_PASSWORD}");
        assertThat(realm).doesNotContain("KYC_DOCUMENT_HMAC_SECRET");
        assertThat(realm).doesNotContain("change-me");
    }
}
