package com.dnnthanh.wallet.be.auth.application.service;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.application.model.MyProfile;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyPermissionsQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyProfileQuery;
import com.dnnthanh.wallet.be.auth.application.port.in.GetMyScopesQuery;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationCachePort;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationDirectoryPort;
import com.dnnthanh.wallet.be.platform.context.UserContext;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.RequestScope;

@Service
@RequestScope
@RequiredArgsConstructor
public class CurrentIdentityServiceImplement
        implements GetMyProfileQuery, GetMyPermissionsQuery, GetMyScopesQuery {
    private final UserContext userContext;
    private final AuthorizationDirectoryPort authorizationDirectory;
    private final AuthorizationCachePort authorizationCache;

    @Override
    public MyProfile getMyProfile() {
        return new MyProfile(
                userContext.userId(),
                userContext.username(),
                userContext.actorType(),
                userContext.roles());
    }

    @Override
    public Set<String> getMyPermissions() {
        return currentAuthorization().permissions();
    }

    @Override
    public Set<String> getMyScopes() {
        return currentAuthorization().scopes();
    }

    public EffectiveAuthorization currentAuthorization() {
        Optional<EffectiveAuthorization> cachedAuthorization = findCachedAuthorization();
        if (cachedAuthorization.isPresent()) return cachedAuthorization.orElseThrow();

        EffectiveAuthorization resolved = authorizationDirectory.resolve(userContext.userId());
        cacheAuthorization(resolved);
        return resolved;
    }

    private Optional<EffectiveAuthorization> findCachedAuthorization() {
        try {
            return authorizationCache.find(userContext.userId());
        } catch (RuntimeException cacheFailure) {
            return Optional.empty();
        }
    }

    private void cacheAuthorization(EffectiveAuthorization authorization) {
        try {
            authorizationCache.put(userContext.userId(), authorization);
        } catch (RuntimeException cacheFailure) {
            // Redis is an optimization only; Keycloak remains the source of truth.
        }
    }
}
