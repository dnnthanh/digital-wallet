# Digital Wallet / Banking Platform

Production-oriented learning project for a digital wallet/banking platform.

> **Important:** the E-commerce project is a reference for engineering conventions only. This repository owns the Digital Wallet domain and must not import marketplace concepts unless a wallet use case explicitly requires them.

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
