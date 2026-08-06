package com.dnnthanh.wallet.be.platform.security;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Immutable effective authorization snapshot for the current principal. */
public record CurrentAuthorization(Set<String> permissions, Set<String> scopes) {
    public CurrentAuthorization {
        permissions = immutableSorted(permissions);
        scopes = immutableSorted(scopes);
    }

    public static CurrentAuthorization empty() {
        return new CurrentAuthorization(Set.of(), Set.of());
    }

    private static Set<String> immutableSorted(Set<String> values) {
        return Collections.unmodifiableSet(new TreeSet<>(values));
    }
}
