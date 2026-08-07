# DW-003 Customer KYC Verification

## Scope

Verification evidence for DW-003 — Customer KYC on branch `feature/customer-kyc`, targeting `develop` via PR #5.

## Verified functional head

Functional implementation head verified before documentation/diagnostic cleanup:

- Commit: `bc6d47ab6d6d0f8200fee3baac7535873fe49d0d`
- GitHub Actions workflow: `CI`
- Run: `31176105959` (`run_number: 356`)
- Conclusion: `success`

Successful jobs:

- `repository-quality`
- `backend-format`
- `backend-test`
- `compose-validation`
- `dockerfile-validation`
- `runtime-smoke`

The `runtime-smoke` job successfully:

- built and started PostgreSQL, Redis, Keycloak, `be-auth-api`, and `be-customer-kyc-api`;
- waited for Auth API and KYC actuator health endpoints;
- verified Keycloak `private_key_jwt` service authentication;
- queried Auth API profile, permissions, and scopes using the obtained access token;
- shut down the runtime stack cleanly.

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

A temporary `Backend Test Diagnostics` workflow was introduced only to upload Maven Surefire/Failsafe reports while the main CI job log endpoint was not returning usable Maven failure output. It is removed as part of final cleanup after the main CI became green.

## Final verification rule

The reporting/cleanup commits change the branch head. Therefore the PR must only be considered ready after the normal `CI` workflow for the final cleanup head reaches terminal `success` for every required job, including `runtime-smoke`.

No merge into `master` is part of DW-003 delivery.
