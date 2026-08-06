# ADR-002: Keycloak-Centered Authentication and Authorization

## Status
Accepted

## Decision
Keycloak owns authentication and authorization metadata. Realm/client composite roles model `role -> permissions`. Keycloak Groups model hierarchical authorization scope. JWTs contain minimal identity/role claims, not full permissions or large scope trees.

## Self-service APIs
- `GET /api/v1/me/profile`
- `GET /api/v1/me/permissions`
- `GET /api/v1/me/scopes`

Backend resolves effective permissions/scopes from Keycloak through an authorization port and may cache results with bounded TTL/invalidation. Backend business policies always re-enforce permission + scope.
