package com.dnnthanh.wallet.be.auth.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationCachePort;
import com.dnnthanh.wallet.be.auth.application.port.out.AuthorizationDirectoryPort;
import com.dnnthanh.wallet.be.platform.context.UserContext;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CurrentIdentityServiceImplementTest {
    @Mock private AuthorizationDirectoryPort directory;
    @Mock private AuthorizationCachePort cache;

    @Test
    void shouldUseCachedAuthorizationWithoutCallingKeycloakDirectory() {
        UserContext userContext = userContext();
        EffectiveAuthorization cached =
                new EffectiveAuthorization(
                        "user-1", Set.of("self:read"), Set.of("/bank/demo-branch"));
        when(cache.find("user-1")).thenReturn(Optional.of(cached));
        CurrentIdentityServiceImplement service =
                new CurrentIdentityServiceImplement(userContext, directory, cache);

        assertThat(service.getMyPermissions()).containsExactly("self:read");
        assertThat(service.getMyScopes()).containsExactly("/bank/demo-branch");
        verifyNoInteractions(directory);
        verify(cache, never()).put("user-1", cached);
    }

    @Test
    void shouldResolveAndCacheWhenAuthorizationIsMissing() {
        UserContext userContext = userContext();
        EffectiveAuthorization resolved =
                new EffectiveAuthorization(
                        "user-1", Set.of("self:read"), Set.of("/bank/demo-branch"));
        when(cache.find("user-1")).thenReturn(Optional.empty());
        when(directory.resolve("user-1")).thenReturn(resolved);
        CurrentIdentityServiceImplement service =
                new CurrentIdentityServiceImplement(userContext, directory, cache);

        assertThat(service.getMyPermissions()).containsExactly("self:read");
        verify(directory).resolve("user-1");
        verify(cache).put("user-1", resolved);
    }

    @Test
    void shouldFallThroughToKeycloakWhenCacheReadFails() {
        UserContext userContext = userContext();
        EffectiveAuthorization resolved =
                new EffectiveAuthorization(
                        "user-1", Set.of("self:read"), Set.of("/bank/demo-branch"));
        when(cache.find("user-1")).thenThrow(new IllegalStateException("redis unavailable"));
        when(directory.resolve("user-1")).thenReturn(resolved);
        CurrentIdentityServiceImplement service =
                new CurrentIdentityServiceImplement(userContext, directory, cache);

        assertThat(service.getMyPermissions()).containsExactly("self:read");
        verify(directory).resolve("user-1");
    }

    @Test
    void shouldReturnStableCurrentProfileFromRequestContext() {
        CurrentIdentityServiceImplement service =
                new CurrentIdentityServiceImplement(userContext(), directory, cache);

        assertThat(service.getMyProfile().userId()).isEqualTo("user-1");
        assertThat(service.getMyProfile().username()).isEqualTo("wallet-demo");
        assertThat(service.getMyProfile().actorType()).isEqualTo(UserContext.ActorType.USER);
        assertThat(service.getMyProfile().roles()).containsExactly("role:wallet-user");
    }

    private UserContext userContext() {
        return new UserContext(
                "user-1", "wallet-demo", UserContext.ActorType.USER, Set.of("role:wallet-user"));
    }
}
