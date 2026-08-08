# DW-003 Customer KYC Verification

## Scope

Verification evidence for DW-003 — Customer KYC on branch `feature/customer-kyc`, targeting `develop` via PR #5.

## Latest verified code head

The latest code-changing head is:

- Commit: `f7908b5bdfe1546ea3f52794962494d3c759b2f4`
- GitHub Actions workflow: `CI`
- Run: `31238487676` (`run_number: 366`)
- Conclusion: `success`

Successful jobs:

- `repository-quality`
- `backend-format`
- `backend-test`
- `compose-validation`
- `dockerfile-validation`
- `runtime-smoke`

The `backend-format` job ran Spotless and confirmed that formatter output was already committed.

The `runtime-smoke` job successfully:

- built and started PostgreSQL, Redis, Keycloak, `be-auth-api`, and `be-customer-kyc-api`;
- waited for Auth API and KYC actuator health endpoints;
- verified Keycloak `private_key_jwt` service authentication;
- queried Auth API profile, permissions, and scopes using the obtained access token;
- shut down the runtime stack cleanly.

## Authorization-expression cleanup

KYC controller authorization no longer repeats inline SpEL permission strings. The four endpoint authorization expressions are centralized in `KycAuthorizationExpressions` and used as compile-time constants:

- `KYC_SELF_READ`
- `KYC_SELF_WRITE`
- `KYC_SELF_SUBMIT`
- `KYC_REVIEW`

For example, reviewer endpoints now use `@PreAuthorize(KYC_REVIEW)`. The change preserves the existing permission values while removing controller magic strings and following the module's named-constant convention.

## CI failures found and fixed during verification

1. Spotless changed KYC sources.
   - Applied and committed formatter output.

2. KYC security integration test could not compile `AutoConfigureMockMvc`.
   - Added Spring Boot 4 focused MVC test support with `spring-boot-starter-webmvc-test`.

3. Hibernate schema validation ran without KYC Liquibase migrations.
   - Replaced direct `liquibase-core` usage with `spring-boot-starter-liquibase` so Spring Boot 4 Liquibase auto-configuration executes migrations before JPA validation.

4. Mockito strict stubbing detected an unused reviewer stub.
   - Removed the unused test stub.

5. KYC outbox persistence could not resolve `OutboxPayloadCodec` as a Spring bean.
   - Added `PlatformOutboxAutoConfiguration` to the shared platform starter and registered it in `AutoConfiguration.imports` using `@ConditionalOnMissingBean`.

6. PostgreSQL JDBC could not infer a SQL type for `java.time.Instant` in raw `JdbcTemplate` inserts.
   - Converted application-domain `Instant` values to `java.sql.Timestamp` at the JDBC adapter boundary for KYC outbox and review-audit inserts.

## Test/report diagnostics

A temporary `Backend Test Diagnostics` workflow was introduced only to upload Maven Surefire/Failsafe reports while the main CI job log endpoint was not returning usable Maven failure output. It was removed after the normal CI became green.

## Final verification rule

This report update changes only verification documentation and therefore creates a newer branch head than the latest code-changing commit. The normal `CI` workflow for the final branch head must still reach terminal `success` for every required job, including `runtime-smoke`; the exact final-head run is recorded in the PR description after verification.

No merge into `master` is part of DW-003 delivery.
