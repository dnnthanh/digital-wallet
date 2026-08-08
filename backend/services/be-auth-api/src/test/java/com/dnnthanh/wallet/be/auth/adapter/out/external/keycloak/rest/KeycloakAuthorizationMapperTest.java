package com.dnnthanh.wallet.be.auth.adapter.out.external.keycloak.rest;

import static org.assertj.core.api.Assertions.assertThat;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import java.util.List;
import org.junit.jupiter.api.Test;

class KeycloakAuthorizationMapperTest {
    @Test
    void shouldMapOnlyPermissionCompositeRolesAndGroupPaths() {
        KeycloakAuthorizationMapper mapper = new KeycloakAuthorizationMapper();

        EffectiveAuthorization authorization =
                mapper.map(
                        "user-1",
                        List.of(
                                new KeycloakRoleRepresentation("role:wallet-user"),
                                new KeycloakRoleRepresentation("permission:TRANSFER_CREATE"),
                                new KeycloakRoleRepresentation("permission:SELF_READ"),
                                new KeycloakRoleRepresentation("permission:SELF_READ")),
                        List.of(
                                new KeycloakGroupRepresentation("/bank/demo-branch"),
                                new KeycloakGroupRepresentation("/bank/demo-branch"),
                                new KeycloakGroupRepresentation("/bank")));

        assertThat(authorization.permissions()).containsExactly("SELF_READ", "TRANSFER_CREATE");
        assertThat(authorization.scopes()).containsExactly("/bank", "/bank/demo-branch");
    }
}
