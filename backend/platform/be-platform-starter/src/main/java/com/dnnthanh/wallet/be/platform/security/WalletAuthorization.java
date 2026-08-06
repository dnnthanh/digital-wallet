package com.dnnthanh.wallet.be.platform.security;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;

/** Reusable permission and hierarchical-scope checks for method-security expressions. */
@RequiredArgsConstructor
public class WalletAuthorization {
    private final CurrentAuthorization currentAuthorization;

    public boolean hasPermission(String permission) {
        return StringUtils.isNotBlank(permission)
                && currentAuthorization.permissions().contains(permission);
    }

    public boolean hasScope(String requiredScope) {
        if (StringUtils.isBlank(requiredScope)) return false;
        return currentAuthorization.scopes().stream()
                .filter(Objects::nonNull)
                .anyMatch(
                        grantedScope ->
                                requiredScope.equals(grantedScope)
                                        || requiredScope.startsWith(grantedScope + "/"));
    }

    public boolean hasPermissionInScope(String permission, String requiredScope) {
        return hasPermission(permission) && hasScope(requiredScope);
    }
}
