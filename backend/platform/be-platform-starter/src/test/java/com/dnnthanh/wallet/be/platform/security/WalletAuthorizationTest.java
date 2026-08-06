package com.dnnthanh.wallet.be.platform.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.Test;

class WalletAuthorizationTest {
    @Test
    void shouldRequirePermissionAndRespectScopeTreeBoundaries() {
        WalletAuthorization authorization =
                new WalletAuthorization(
                        new CurrentAuthorization(
                                Set.of("transfer:create"), Set.of("/bank/demo-branch")));

        assertThat(authorization.hasPermission("transfer:create")).isTrue();
        assertThat(authorization.hasPermission("transfer:approve")).isFalse();
        assertThat(authorization.hasScope("/bank/demo-branch")).isTrue();
        assertThat(authorization.hasScope("/bank/demo-branch/teller-01")).isTrue();
        assertThat(authorization.hasScope("/bank/demo-branch-two")).isFalse();
        assertThat(
                        authorization.hasPermissionInScope(
                                "transfer:create", "/bank/demo-branch/teller-01"))
                .isTrue();
        assertThat(
                        authorization.hasPermissionInScope(
                                "transfer:approve", "/bank/demo-branch/teller-01"))
                .isFalse();
    }

    @Test
    void shouldDenyWhenCurrentAuthorizationIsEmpty() {
        WalletAuthorization authorization = new WalletAuthorization(CurrentAuthorization.empty());

        assertThat(authorization.hasPermission("transfer:create")).isFalse();
        assertThat(authorization.hasScope("/bank/demo-branch")).isFalse();
    }
}
