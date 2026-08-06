package com.dnnthanh.wallet.be.auth.application.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Effective Keycloak authorization metadata resolved for one user. */
public record EffectiveAuthorization(String userId, Set<String> permissions, Set<String> scopes) {
    public EffectiveAuthorization {
        permissions = immutableSorted(permissions);
        scopes = immutableSorted(scopes);
    }

    private static Set<String> immutableSorted(Set<String> values) {
        return Collections.unmodifiableSet(new TreeSet<>(values));
    }
}
