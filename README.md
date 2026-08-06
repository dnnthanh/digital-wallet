# Digital Wallet / Banking Platform

Production-oriented learning project for a digital wallet/banking platform.

> **Important:** the E-commerce project is a reference for engineering conventions and repository structure only. This repository owns the Digital Wallet domain and must not import marketplace concepts unless a wallet use case explicitly requires them.

## Structural baseline

Digital Wallet intentionally stays in the same engineering family as the E-commerce reference project:

- shared backend starters under `backend/platform/`;
- runnable bounded-context deployables under `backend/services/be-*`;
- Java 25 / Spring Boot 4.1 Maven parent and common quality tooling;
- generic backend `Dockerfile.service` / `Dockerfile.runtime` patterns;
- root and split Docker Compose topology under `compose/`;
- Hexagonal/DDD package boundaries, Testcontainers and GitHub Actions quality gates.

Banking-specific ADRs may extend the reference structure. In particular, Digital Wallet adds gRPC as a first-class option for selected internal synchronous core-money calls.

## Core capabilities

- platform-foundation
- customer-kyc
- wallet-account
- double-entry-ledger
- balance-projection
- transfer
- funding-payout
- hold-capture-reversal
- limit-fraud
- reconciliation
- scheduled-settlement
- statement-export
- notification
- operations
- performance-resilience-comparison

## Communication model

- REST/JSON: public/mobile/admin APIs, external provider APIs, webhooks.
- gRPC/Protobuf: internal synchronous core-money calls where latency and typed contracts matter.
- Kafka: domain events, projections, notification, asynchronous recovery.

## Authentication and authorization

Keycloak/OIDC is the authority for authentication, roles, composite permissions, and authorization scope/group hierarchy. JWTs stay compact; effective permissions and scope trees are resolved through dedicated authorization APIs/caches.

## Branch policy

- `master`: production-ready only.
- `develop`: integration/core branch.
- `feature/*`, `fix/*`, `chore/*`: branch from `develop`.

Start with [AGENTS.MD](AGENTS.MD), then `.agent/PLAN.MD`, `.agent/CONVENTIONS.MD`, and the relevant feature spec under `.agent/specs/`.
