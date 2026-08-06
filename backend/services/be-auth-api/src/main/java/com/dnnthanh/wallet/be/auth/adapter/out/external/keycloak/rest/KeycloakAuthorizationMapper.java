package com.dnnthanh.wallet.be.auth.adapter.out.external.keycloak.rest;

import com.dnnthanh.wallet.be.auth.application.model.EffectiveAuthorization;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class KeycloakAuthorizationMapper {
    private static final String PERMISSION_ROLE_PREFIX = "permission:";

    public EffectiveAuthorization map(
            String userId,
            List<KeycloakRoleRepresentation> effectiveRoles,
            List<KeycloakGroupRepresentation> groups) {
        Set<String> permissions = new TreeSet<>();
        effectiveRoles.stream()
                .filter(Objects::nonNull)
                .map(KeycloakRoleRepresentation::name)
                .filter(Objects::nonNull)
                .filter(name -> StringUtils.startsWith(name, PERMISSION_ROLE_PREFIX))
                .map(name -> StringUtils.removeStart(name, PERMISSION_ROLE_PREFIX))
                .filter(StringUtils::isNotBlank)
                .forEach(permissions::add);

        Set<String> scopes = new TreeSet<>();
        groups.stream()
                .filter(Objects::nonNull)
                .map(KeycloakGroupRepresentation::path)
                .filter(StringUtils::isNotBlank)
                .forEach(scopes::add);

        return new EffectiveAuthorization(userId, permissions, scopes);
    }
}
