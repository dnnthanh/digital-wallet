# Library-First, Typed Serialization, and Configuration Design

## Status
Accepted by project owner on 2026-08-06.

## Goal
Make reuse-first engineering, typed Redis/Kafka data, and configuration-over-hardcoding mandatory across Digital Wallet, while preserving the E-commerce reference project's platform conventions.

## Decisions

### 1. Library/platform reuse is mandatory before custom code
Before writing a helper, serializer, mapper, constructor boilerplate, logging wrapper, collection/string utility, or infrastructure abstraction, an agent must check in this order:

1. JDK / standard library;
2. Spring / Spring Boot facilities already in the project;
3. existing Digital Wallet `backend/platform/*` modules;
4. dependencies already managed by the parent POM (for example Lombok, MapStruct, Jackson, Resilience4j);
5. only then create new project code or propose a new dependency.

Do not create duplicate `StringUtils`, `JsonUtils`, `CollectionUtils`, manual constructor/logging boilerplate, manual DTO mapping, or parallel cache/Kafka abstractions when an existing library/platform facility already solves the problem.

### 2. Redis business values are typed objects
Business/application/adapters must not manually serialize an object to JSON `String` and store it through `StringRedisTemplate`/`RedisTemplate<String, String>`.

Approved flow:

`typed object -> WalletCacheManager/Spring Cache -> RedisCacheManager -> JacksonJsonRedisSerializer<T> -> Redis`

Redis keys remain readable strings. Redis values are typed JSON objects managed centrally by the cache starter.

Low-level raw/string Redis access is allowed only for a use case whose Redis data model is inherently scalar/raw (for example a rate-limit counter, distributed primitive, or interoperability requirement), and that exception must be explicit in the feature spec/ADR. It must not be used as a shortcut for object caching.

### 3. Redis namespace comes from `spring.application.name`
No Java literal such as `"wallet::"` is used as the cache namespace.

Every runnable backend service must define `spring.application.name` in its `application.yml`. The cache starter reads that property and builds readable keys as:

`<spring.application.name>::<cache-name>::<business-key>`

Example:

`be-auth-api::authorization-snapshots::user-123`

Operational values such as service names, URLs, timeouts, credentials, topic overrides, and environment-specific prefixes belong in YAML/environment-backed configuration, preferably bound through `@ConfigurationProperties`, not repeated as Java literals.

### 4. Kafka business messages are typed objects
Producer/application code publishes typed event/envelope objects through `KafkaTemplate<String, Object>` or a typed platform producer. It must not call `ObjectMapper.writeValueAsString(...)` or manually create `byte[]` for ordinary JSON events.

Kafka JSON serialization/deserialization is centralized using Spring Kafka Jackson serializer/deserializer configuration. Broker payloads are bytes physically, but business consumers receive typed Java classes/records.

Do not use `Map<String, Object>` as the normal business event payload. Event contracts use explicit records/classes, for example `EventEnvelope<TransferCompletedPayload>`.

Raw `String`, `byte[]`, or map payload consumers are allowed only at a deliberate protocol/legacy boundary documented in the feature spec/ADR.

### 5. Outbox JSON conversion is an infrastructure boundary
An outbox table may persist JSON text/JSONB because it is a storage format. Serialization/deserialization must be centralized in `OutboxPayloadCodec`; application/domain code works with typed event objects. No feature should duplicate `ObjectMapper` conversion logic around outbox persistence.

### 6. CI must enforce the rule
Repository verification must fail when ordinary backend production code introduces:

- `StringRedisTemplate` or `RedisTemplate<String, String>` for object caching;
- manual Redis object JSON conversion with `ObjectMapper.writeValueAsString/readValue` outside approved codec/serializer infrastructure;
- Kafka listeners/producers built around raw `String`/`byte[]`/`Map<String,Object>` payloads without an explicit approved boundary;
- reintroduction of hard-coded `wallet::` cache namespace;
- runnable services missing `spring.application.name` in `application.yml`.

The guard should be specific enough not to reject centralized serializer/codec code or legitimate scalar Redis primitives.

## DW-001 refactor

- remove the current `JsonRedisCache` / `StringRedisTemplate` object-cache path;
- retain `WalletCacheManager`, `RedisCacheSpec`, `RedisCacheManager`, and typed Jackson value serializers;
- derive Redis prefix from `spring.application.name`;
- make Kafka event payloads typed instead of `Map<String,Object>`;
- centralize Kafka serializer/deserializer defaults;
- align Lombok/MapStruct annotation processing with the E-commerce reference parent POM;
- update `AGENTS.MD`, `.agent/CONVENTIONS.MD`, DW-001 spec, and repository verification rules.

## Non-goals

- Do not serialize Redis keys as JSON objects.
- Do not create a new platform module solely for this refactor.
- Do not add dependencies when the existing managed libraries solve the requirement.
- Do not turn all constants into configuration; stable protocol constants may remain code constants when they are genuinely invariant.
