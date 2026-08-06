package com.dnnthanh.wallet.be.auth.application.model;

import com.dnnthanh.wallet.be.platform.context.UserContext;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

/** Stable self-service identity projection for the authenticated principal. */
public record MyProfile(
        String userId, String username, UserContext.ActorType actorType, Set<String> roles) {
    public MyProfile {
        roles = Collections.unmodifiableSet(new TreeSet<>(roles));
    }
}
