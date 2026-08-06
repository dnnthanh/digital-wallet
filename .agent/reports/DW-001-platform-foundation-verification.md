# DW-001 Verification Evidence

## Structural correction

The first DW-001 implementation invented a parallel `backend/be-platform-foundation` skeleton. After review against the uploaded E-commerce source, DW-001 was reworked to use the reference repository family directly:

- `backend/platform/be-platform-starter`;
- `backend/platform/be-platform-cache-starter`;
- future `backend/services/be-*` deployables;
- generic backend runtime/service Dockerfiles;
- split Compose infrastructure/backend fragments.

Reference technical base code was adapted into the Digital Wallet namespace for API envelopes, user context, exception/i18n, mapping contracts, stereotypes, Kafka/outbox, tracing/logging/security and cache conventions. Marketplace-specific seller/cart authorization concepts were intentionally not copied.

## Library-first / typed-data correction

Follow-up reviews aligned DW-001 with the stricter E-commerce coding rules:

- `AGENTS.MD` and `.agent/CONVENTIONS.MD` require JDK/Spring/platform/existing managed libraries to be checked before custom helpers or dependencies;
- shared backend utilities include Apache Commons Lang, Apache Commons Collections and Commons Codec;
- concrete conventions cover `StringUtils`, `ArrayUtils`, Spring `CollectionUtils`, Commons `MapUtils`, `Objects`, `Validate`, Lombok and MapStruct;
- object caching no longer uses `StringRedisTemplate` or manual JSON strings;
- Redis values are typed through `WalletCacheManager` -> `RedisCacheManager` -> `JacksonJsonRedisSerializer<T>`;
- Redis key namespace comes from `spring.application.name`, producing `<application-name>::<cache-name>::<business-key>`;
- ordinary Kafka business messages use typed `EventEnvelope<T>` payloads rather than `Map<String, Object>`;
- Spring Kafka JSON serde defaults are centralized in `classpath:platform-kafka.yml` using `JacksonJsonSerializer` / `JacksonJsonDeserializer`;
- JSON/JSONB conversion for the transactional outbox remains an explicit infrastructure boundary in `OutboxPayloadCodec`.

## Formatting correction

The exact formatter configuration was taken from the uploaded E-commerce reference rather than approximated:

- Spotless Maven Plugin `2.44.5`;
- `google-java-format 1.28.0`;
- `AOSP` style;
- unused import removal;
- trailing whitespace cleanup;
- final newline enforcement;
- `spotless:check` bound to Maven `verify`.

## Exception semantics

Historically, `IdempotencyKey` threw a plain `IllegalArgumentException`, which reached the generic server-error path. Client-visible idempotency validation was corrected to use `BusinessException(PlatformErrorCode.INVALID_IDEMPOTENCY_KEY)` and therefore returns stable localized HTTP 400 semantics.

DW-001 now also explicitly distinguishes programmer/configuration invariants from normal client-invalid data:

- expected request/business validation uses Bean Validation or typed `BusinessException` / `ErrorCode` and i18n;
- `IllegalArgumentException`, `IllegalStateException`, and `NullPointerException` that represent internal precondition/invariant failures are handled explicitly by `GlobalExceptionHandler.handleInvariantViolation`;
- the invariant handler logs the technical cause server-side and returns localized `PlatformErrorCode.INTERNAL_ERROR` / HTTP 500;
- internal diagnostic text from `Validate` / `Objects.requireNonNull` never becomes the client response message;
- there is intentionally no blanket unchecked-exception-to-HTTP-400 mapping.

## Null / invariant / enum / i18n cleanup evidence

The review that triggered this cleanup identified a concrete manual pattern in `SpringMessageResolver`:

```java
codes == null || codes.length == 0
```

The platform was cleaned consistently instead of changing only that line:

- pure reference-null predicates use `Objects.isNull` / `Objects.nonNull` or `Objects.requireNonNullElse*`;
- string blank/default/trim semantics use `StringUtils`;
- array emptiness uses `ArrayUtils.isEmpty` and empty-array constants;
- collection/map null-safe operations use Spring/Commons collection utilities where appropriate;
- `SpringMessageResolver` now combines `ArrayUtils`, `StringUtils`, and `Objects` rather than manual null/length checks;
- `CodeEnum` defines `name()` as a structural contract instead of runtime-checking `instanceof Enum`;
- `I18nCodeEnum` defines `getDeclaringClass()` and builds keys from `I18nConstants`, removing hard-coded runtime enum guard exceptions;
- `PlatformInvariantMessages` centralizes platform internal precondition/diagnostic text;
- `CacheInvariantMessages` centralizes cache-starter precondition text;
- `OutboxMessage`, `OutboxPayloadCodec`, Kafka base classes, gRPC deadline configuration, correlation context, cache code and tracing infrastructure no longer scatter unchecked-precondition string literals.

Malformed/unknown enums entering HTTP are treated as request-deserialization errors and resolve through the platform BAD_REQUEST/i18n path; raw Java enum exception text is not the API contract.

### TDD evidence

RED commit `d72284464783959d46f90ebfcb7c5be57106b152` added two contracts before production changes:

- `GlobalExceptionHandlerTest` required an explicit invariant handler to map a `NullPointerException` to localized `INTERNAL_ERROR` / HTTP 500;
- `CodeEnumContractTest` required code/i18n contracts to work without runtime enum-type checks.

CI run `31078977242` failed at test compilation because those contracts did not yet exist, confirming the RED state.

After implementation, CI run `31079545175` compiled successfully and executed **24 tests with 0 failures and 0 errors**. That run failed only Spotless on three formatting diffs, which were then corrected exactly to the AOSP formatter output.

A stricter repository-quality RED gate was then introduced. PR run `31079850052` intentionally failed and identified only the remaining gaps:

- missing `Objects.isNull` / named-constant guidance in agent docs;
- hard-coded unchecked-precondition messages in `OutboxMessage`;
- hard-coded unchecked-precondition messages in `OutboxPayloadCodec`.

Those exact gaps were corrected. The verifier now rejects future production occurrences of:

- manual simple `x == null` / `x != null` reference predicates;
- manual array `length == 0` emptiness checks;
- existing null+blank/null+empty duplication patterns;
- inline string literals in `Objects.requireNonNull`, common `Validate` preconditions, and direct IAE/ISE/NPE construction;
- `instanceof Enum` runtime guards in `CodeEnum` / `I18nCodeEnum`.

Pre-squash CI run `31080236286` on the completed code/spec/agent-rule tree passed all required jobs:

- `repository-quality`: success;
- `backend-test`: success;
- `compose-validation`: success;
- `dockerfile-validation`: success.

A fresh run on the final squash commit is still required before DW-001 is considered complete.

## Typed-data / regression evidence

- `WalletRedisCacheConfigurationTest` requires the Redis namespace to derive from `spring.application.name`.
- `WalletRedisCacheIntegrationTest` writes/reads a typed object through real Redis and verifies the readable application-name key prefix.
- `BaseDomainEventConsumerTest` requires a typed Kafka payload API without map field lookups.
- `KafkaJsonSerializationTest` verifies Spring Kafka Jackson JSON serialization/deserialization of a typed event envelope.
- `OutboxPayloadCodecTest` verifies typed payload round-trip at the approved outbox infrastructure boundary.
- `PlatformStereotypeTest` verifies technical stereotype metadata.

## Platform-owned distributed tracing evidence

Tracing lives in `be-platform-starter` so later services/jobs do not need feature-local propagation code.

- `PlatformObservabilityAutoConfiguration` is loaded through Spring Boot auto-configuration metadata.
- Every Spring-managed `KafkaTemplate` is configured with the shared `ObservationRegistry` and observation enabled.
- Every Spring Kafka listener container factory is configured with observation enabled, the shared registry, and per-record observation for batch listeners.
- `SchedulingConfigurer` installs the shared `ObservationRegistry` on `ScheduledTaskRegistrar`, so ordinary `@Scheduled` executions receive observations/root traces automatically.
- Boot-managed `ThreadPoolTaskExecutor` and `SimpleAsyncTaskExecutor` instances receive `ContextPropagatingTaskDecorator`.
- W3C `traceparent`/`tracestate` remains the authoritative framework-managed propagation mechanism; compatibility `trace-id` is platform-only diagnostics.
- Repository verification rejects service-level manual distributed-trace propagation and common instrumentation bypasses.

TDD for tracing began with RED commit `0b18e215af0ae1b9d9f746758b505b6053605ca7`, which failed because `PlatformObservabilityAutoConfiguration` did not yet exist. The resulting platform observability tests verify Kafka, scheduled execution, async propagation and starter auto-configuration metadata.

## Required final quality gates

The final PR head must pass all of these GitHub Actions jobs before DW-001 is considered complete:

- `repository-quality`;
- `backend-test` (Java 25 Maven verify + Spotless + unit/integration tests + Redis/PostgreSQL Testcontainers);
- `compose-validation`;
- `dockerfile-validation`.

No completion claim is valid unless those jobs are green on the current squash PR head.
