# DW-001 Null, i18n, and Invariant Cleanup Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make DW-001 consistently library-first for null/empty checks, remove runtime enum-type guards, centralize invariant messages, and guarantee precondition exceptions resolve through explicit localized server-error handling.

**Architecture:** User/client validation remains typed `BusinessException`/`ErrorCode` and i18n. Programmer/configuration/infrastructure preconditions may still use JDK `Objects.requireNonNull` or Commons `Validate`, but their diagnostic text comes from named constants and `IllegalArgumentException`/`IllegalStateException`/`NullPointerException` are explicitly mapped to localized `INTERNAL_ERROR` if they cross a servlet boundary. `CodeEnum`/`I18nCodeEnum` define structural methods rather than runtime `instanceof Enum` checks.

**Tech Stack:** Java 25, Spring Boot 4.1.0, Apache Commons Lang/Collections, JUnit 5, AssertJ, Mockito, Spotless AOSP, repository Python verifier.

## Global Constraints

- Preserve the E-commerce-derived repository structure and existing DW-001 public contracts.
- Prefer type-specific utilities: `StringUtils` for strings, `ArrayUtils` for arrays, Spring/Commons collection utilities for collections/maps, and `Objects.isNull/nonNull` for pure reference-null predicates.
- Do not blanket-map programmer/configuration invariant exceptions to HTTP 400.
- User-visible/client-invalid cases use typed `ErrorCode` + i18n.
- Internal diagnostic/precondition text must use named constants rather than repeated inline literals.
- Final PR branch must be re-squashed to one commit and all GitHub Actions gates must be green on the squash head.

---

### Task 1: Define RED contracts for invariant handling and enum behavior

**Files:**
- Modify: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/web/error/GlobalExceptionHandlerTest.java`
- Create: `backend/platform/be-platform-starter/src/test/java/com/dnnthanh/wallet/be/platform/model/CodeEnumContractTest.java`

**Interfaces:**
- Produces: `GlobalExceptionHandler.handleInvariantViolation(RuntimeException, HttpServletRequest, Locale)` contract.
- Produces: `CodeEnum.name()` and `I18nCodeEnum.getDeclaringClass()` structural contracts.

- [x] Write failing tests before production changes.
- [x] Run CI and verify test compilation fails because the new contracts do not exist.

### Task 2: Implement null-safe library-first i18n and enum contracts

**Files:**
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/i18n/SpringMessageResolver.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/i18n/MessageResolvable.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/model/CodeEnum.java`
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/i18n/I18nCodeEnum.java`
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/i18n/I18nConstants.java`

**Interfaces:**
- `CodeEnum.name(): String`
- `I18nCodeEnum.getDeclaringClass(): Class<?>`
- `I18nCodeEnum.getMessageKey()` builds `enum.<type>.<code>` without runtime enum guards.

- [x] Replace manual `codes == null || codes.length == 0` with `ArrayUtils` and `Objects` utilities.
- [x] Remove runtime enum `instanceof`/hard-coded `IllegalStateException` branches.
- [x] Keep enum message-key structure in named constants.
- [x] Run focused tests and Maven verify through GitHub Actions.

### Task 3: Centralize precondition diagnostics and explicit invariant exception handling

**Files:**
- Create: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/constant/PlatformInvariantMessages.java`
- Modify: platform classes using inline `Objects.requireNonNull`, `Validate`, or unchecked invariant messages.
- Create/modify cache invariant constants under `be-platform-cache-starter` as needed.
- Modify: `backend/platform/be-platform-starter/src/main/java/com/dnnthanh/wallet/be/platform/web/error/GlobalExceptionHandler.java`

**Interfaces:**
- `handleInvariantViolation(RuntimeException, HttpServletRequest, Locale)` returns `PlatformErrorCode.INTERNAL_ERROR` with localized message and logs the technical exception server-side.

- [x] Move internal precondition text to named constants.
- [x] Use `Objects.isNull/nonNull` for pure null predicates and type-specific utilities for string/array/collection semantics.
- [x] Explicitly handle `IllegalArgumentException`, `IllegalStateException`, and `NullPointerException` as localized HTTP 500 invariant failures.
- [x] Preserve typed business/client errors as their existing 4xx mappings.

### Task 4: Add repository guardrails and AI conventions

**Files:**
- Modify: `verification/verify_repository.py`
- Modify: `AGENTS.MD`
- Modify: `.agent/CONVENTIONS.MD`
- Modify: `.agent/specs/DW-001-platform-foundation.md`
- Modify: `.agent/reports/DW-001-platform-foundation-verification.md`

**Interfaces:**
- Repository verification rejects newly introduced manual null/array-empty patterns and hard-coded precondition exception text where a named utility/constant should be used.

- [x] Add verifier patterns without blocking legitimate `instanceof` or framework APIs.
- [x] Document external-i18n vs internal-invariant exception rules for future AI sessions.
- [x] Run repository-quality and Maven verification; pre-squash CI run `31080236286` passed all required jobs.

### Task 5: Re-squash and verify the PR head

**Files:** none beyond verified tree.

- [ ] Create one commit whose parent is current `develop` and whose tree is the verified DW-001 tree.
- [ ] Force-update `feature/platform-foundation` to the squash commit.
- [ ] Confirm PR #2 reports one commit.
- [ ] Run fresh GitHub Actions on the squash head and require `repository-quality`, `backend-test`, `compose-validation`, and `dockerfile-validation` all green.
