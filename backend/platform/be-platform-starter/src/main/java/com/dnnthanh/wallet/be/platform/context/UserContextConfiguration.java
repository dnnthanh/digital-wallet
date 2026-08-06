package com.dnnthanh.wallet.be.platform.context;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.context.annotation.RequestScope;

@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class UserContextConfiguration {
    @Bean
    @RequestScope(proxyMode = ScopedProxyMode.NO)
    public UserContext userContext() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Objects.isNull(authentication)
                || !authentication.isAuthenticated()
                || !(authentication instanceof JwtAuthenticationToken jwtAuthentication))
            return new UserContext("anonymous", "anonymous", UserContext.ActorType.USER, Set.of());
        Jwt jwt = jwtAuthentication.getToken();
        Set<String> roles = new HashSet<>();
        Object realmAccess = jwt.getClaim("realm_access");
        if (realmAccess instanceof java.util.Map<?, ?> map
                && map.get("roles") instanceof java.util.Collection<?> values)
            values.forEach(value -> roles.add(String.valueOf(value)));
        UserContext.ActorType actorType =
                roles.contains("SERVICE_ACCOUNT")
                        ? UserContext.ActorType.SERVICE
                        : UserContext.ActorType.USER;
        return new UserContext(
                jwt.getSubject(),
                jwt.getClaimAsString("preferred_username"),
                actorType,
                Set.copyOf(roles));
    }
}
