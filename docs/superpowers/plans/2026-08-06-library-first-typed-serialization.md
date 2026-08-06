# Library-First Typed Serialization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Enforce reuse-first engineering, typed Redis/Kafka object flows, and `spring.application.name`-based Redis namespacing in DW-001.

**Architecture:** Keep the existing E-commerce-aligned `be-platform-starter` and `be-platform-cache-starter`. Redis object caching goes through Spring Cache with per-cache typed Jackson serializers; Kafka publishes typed envelope/payload objects and relies on centralized Spring Kafka Jackson serialization. Operational namespace comes from YAML via `spring.application.name`.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Data Redis, Spring Cache, Spring Kafka, Jackson, Lombok, MapStruct, JUnit, Testcontainers, repository verifier.

## Global Constraints

- Prefer JDK/Spring/platform/existing managed libraries before custom helpers.
- No `StringRedisTemplate` object cache path.
- No ordinary Kafka JSON as manual `String`/`byte[]`/`Map<String,Object>` business payload.
- Redis keys remain readable strings; values are typed objects.
- Redis namespace derives from `spring.application.name`.
- Every runnable backend service must define `spring.application.name` in `application.yml`.
- Outbox JSON conversion is centralized at infrastructure boundary.
- No hard-coded `wallet::` namespace.

---

### Task 1: Lock conventions and verifier rules

**Files:**
- Modify: `AGENTS.MD`
- Modify: `.agent/CONVENTIONS.MD`
- Modify: `.agent/specs/DW-001-platform-foundation.md`
- Modify: `verification/verify_repository.py`

**Produces:** Repository-wide rules and CI checks for library-first, typed Redis/Kafka, application-name configuration.

- [ ] Add explicit library lookup order and examples for Lombok, MapStruct, Jackson and existing platform utilities.
- [ ] Add Redis/Kafka typed-object rules and documented exception boundaries.
- [ ] Add hardcoding/configuration rule requiring operational values in YAML/`@ConfigurationProperties`.
- [ ] Extend verifier to reject `StringRedisTemplate` object-cache production code, `wallet::` hardcoding, and missing `spring.application.name` in runnable services.
- [ ] Run repository verifier and confirm it fails on the current `JsonRedisCache`/hard-coded prefix implementation.

### Task 2: Refactor Redis to typed object cache and application namespace

**Files:**
- Delete: `backend/platform/be-platform-cache-starter/src/main/java/com/dnnthanh/wallet/be/platform/cache/JsonRedisCache.java`
- Delete: `backend/platform/be-platform-cache-starter/src/main/java/com/dnnthanh/wallet/be/platform/cache/RedisKeySpec.java`
- Delete/replace: `backend/platform/be-platform-cache-starter/src/test/java/com/dnnthanh/wallet/be/platform/cache/JsonRedisCacheIntegrationTest.java`
- Modify: `backend/platform/be-platform-cache-starter/src/main/java/com/dnnthanh/wallet/be/platform/cache/WalletRedisCacheAutoConfiguration.java`
- Create: `backend/platform/be-platform-cache-starter/src/test/resources/application.yml`
- Create: `backend/platform/be-platform-cache-starter/src/test/java/com/dnnthanh/wallet/be/platform/cache/WalletRedisCacheIntegrationTest.java`

**Produces:** `RedisCacheManager` storing typed objects through `JacksonJsonRedisSerializer<T>` and key prefix `<spring.application.name>::<cache>::`.

- [ ] Write integration test expecting a typed record to round-trip through `WalletCacheManager` and asserting Redis key prefix starts with test `spring.application.name`.
- [ ] Run cache-starter tests and verify RED against current hard-coded namespace/object path.
- [ ] Change auto-configuration to require/read `spring.application.name` from Spring `Environment`/properties and compute prefix from it.
- [ ] Remove low-level string JSON object-cache helper and its spec.
- [ ] Run cache-starter and full Maven verify GREEN.

### Task 3: Make Kafka event payload contracts typed

**Files:**
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/event/DomainEvent.java` or replace its use with generic `EventEnvelope<T>`.
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/kafka/BaseDomainEventConsumer.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/kafka/DomainEventProducer.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/event/OutboxEventFactory.java` / `outbox/OutboxPayloadCodec.java` as needed.
- Create/modify tests under `backend/platform/be-platform-starter/src/test/java/.../event` and `.../kafka`.

**Produces:** Business-facing Kafka API based on explicit payload classes/records and `EventEnvelope<T>`; raw JSON conversion remains centralized only in serializer/outbox infrastructure.

- [ ] Add failing contract test using `SamplePayload` and typed `EventEnvelope<SamplePayload>`.
- [ ] Add failing test proving consumer handler receives typed payload, not a `Map` field lookup API.
- [ ] Refactor consumer/producer contracts to generic typed envelope payloads.
- [ ] Remove normal `Map<String,Object>` payload path.
- [ ] Keep outbox encode/decode in `OutboxPayloadCodec` only.
- [ ] Run platform-starter tests GREEN.

### Task 4: Centralize Kafka JSON serializer/deserializer defaults

**Files:**
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/kafka/PlatformKafkaSerializationConfiguration.java` if code configuration is needed; otherwise document/configure service YAML defaults through a reusable template.
- Create: `backend/platform/be-platform-starter/src/test/resources/application.yml` with `spring.application.name` and Spring Kafka Jackson serializer/deserializer defaults.
- Create/modify Kafka serialization tests.

**Produces:** Ordinary JSON Kafka messages are serialized/deserialized by Spring Kafka Jackson infrastructure; business code never handles `byte[]`/JSON strings.

- [ ] Write failing serializer round-trip/configuration test.
- [ ] Configure `JacksonJsonSerializer` and `JacksonJsonDeserializer` centrally/reusably.
- [ ] Run targeted tests GREEN.

### Task 5: Align parent POM library processors

**Files:**
- Modify: `backend/pom.xml`

**Produces:** Lombok + MapStruct annotation processors aligned with the reference project.

- [ ] Add explicit annotation processor paths/configuration for Lombok and MapStruct using existing managed versions.
- [ ] Run Maven compile/verify and ensure no new dependency is added unnecessarily.

### Task 6: Full verification and PR update

**Files:**
- Modify: `.agent/reports/DW-001-platform-foundation-verification.md`
- Update PR #2 metadata only after code head is stable.

- [ ] Run repository verifier.
- [ ] Run `./mvnw -B -ntp -f backend/pom.xml verify` through GitHub Actions.
- [ ] Confirm Compose and Dockerfile validation still pass.
- [ ] Inspect all GitHub Actions jobs on current head; fix root cause if any fail.
- [ ] Update verification evidence and PR description with final current-head run.
