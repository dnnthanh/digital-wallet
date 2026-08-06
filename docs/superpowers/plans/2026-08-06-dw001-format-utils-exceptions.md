# DW-001 Formatting, Utility Reuse, and Exception Semantics Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Align DW-001 code style and utility usage with the E-commerce reference while ensuring invalid client input maps to stable API errors instead of accidental HTTP 500 responses.

**Architecture:** Reinstate Spotless as a Maven verify gate, add the reference utility dependencies at the parent level, and refactor repetitive string/collection/map checks to existing JDK/Spring/Apache Commons utilities where they improve clarity. Keep programmer/configuration invariant exceptions separate from client-visible business validation; client-visible validation uses `BusinessException` with an explicit `ErrorCode`.

**Tech Stack:** Java 25, Spring Boot 4.1, Spotless Maven Plugin 2.44.5, Apache Commons Lang 3, Apache Commons Collections 4, Commons Codec, Lombok, JUnit/AssertJ.

## Global Constraints

- Follow the E-commerce reference formatting gate and library-first convention.
- Prefer JDK/Spring/platform facilities before adding custom helpers.
- Use `StringUtils` for null-safe blank/default/trim string operations when it improves clarity.
- Use existing collection/map utilities for null-safe emptiness checks rather than repeating `x == null || x.isEmpty()`.
- Do not globally translate every `IllegalArgumentException` to HTTP 400.
- Client/request validation that can reach an HTTP boundary uses a stable `BusinessException` / `ErrorCode`.
- Internal programmer/configuration invariants may remain unchecked invariant exceptions, but must not be mislabeled as client errors.
- Spotless check is mandatory in Maven `verify` and CI.

---

### Task 1: Add utility dependencies and formatting gate

**Files:**
- Modify: `backend/pom.xml`
- Modify: `AGENTS.MD`
- Modify: `.agent/CONVENTIONS.MD`
- Modify: `verification/verify_repository.py`

**Produces:** Parent-managed Apache Commons utilities and mandatory Spotless formatting verification.

- [ ] Add `commons-lang3`, `commons-collections4`, and `commons-codec` dependencies inherited by backend modules.
- [ ] Configure `spotless-maven-plugin` at `verify` using the reference-style Java formatter and unused-import cleanup.
- [ ] Document concrete utility choices (`StringUtils`, collection/map utils, Commons Codec) and exception semantics.
- [ ] Extend repository verification to require the utility dependencies and Spotless plugin.

### Task 2: Lock client-visible idempotency validation semantics with tests

**Files:**
- Modify: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/idempotency/IdempotencyKeyTest.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/exception/PlatformErrorCode.java`
- Modify: `backend/platform/be-platform-starter/src/main/resources/messages.properties`
- Modify: `backend/platform/be-platform-starter/src/main/resources/messages_vi.properties`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/idempotency/IdempotencyKey.java`

**Produces:** Invalid idempotency keys throw a typed platform `BusinessException` with HTTP 400 semantics.

- [ ] Change the test first to require `BusinessException` + explicit idempotency error code for blank/oversized values.
- [ ] Verify RED on CI/test compile/run against the current implementation.
- [ ] Implement the explicit platform error code and localized messages.
- [ ] Refactor `IdempotencyKey` to `StringUtils` and typed exception semantics.
- [ ] Verify GREEN.

### Task 3: Refactor repetitive utility patterns

**Files:**
- Modify affected Java sources under `backend/platform/*/src/main/java`.

**Produces:** Null-safe string/collection/map handling uses existing utilities where clearer, without introducing new helper classes.

- [ ] Replace manual null/blank correlation-id checks with `StringUtils`.
- [ ] Replace repeated null/empty collection/map checks with existing Spring/Commons utilities when appropriate.
- [ ] Use `Validate` only for internal constructor/configuration preconditions where `IllegalArgumentException` is intentionally a programmer/configuration invariant.
- [ ] Keep infrastructure serialization failures distinct from request validation rather than globally mapping them to 400.

### Task 4: Apply formatter and enforce clean code

**Files:**
- Modify all Java files reported by Spotless on DW-001.

**Produces:** All Java sources/tests satisfy the same automatic format gate.

- [ ] Run the Maven formatting check to capture RED formatting violations.
- [ ] Apply the formatter-equivalent changes to all reported Java sources/tests.
- [ ] Run Maven verify again until Spotless is clean.

### Task 5: Full verification

**Files:**
- Modify: `.agent/reports/DW-001-platform-foundation-verification.md`

- [ ] Run repository-quality verification.
- [ ] Run Java 25 Maven verify including Spotless and Testcontainers.
- [ ] Confirm Compose and Dockerfile jobs remain green.
- [ ] Update PR #2 description only after a stable current-head CI run is green.
