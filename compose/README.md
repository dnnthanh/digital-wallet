# Compose layout

This follows the E-commerce reference repository layout.

- `infrastructure.yml`: PostgreSQL, Redis, Kafka KRaft and Keycloak.
- `backend/*.yml`: one file per backend deployable as DW features add services.
- `backend/all.yml`: aggregate backend compose fragment.
- root `docker-compose.yml`: local entry point composed from infrastructure + backend fragments.

Use `./compose-up.sh config` to validate and `./compose-up.sh up -d` to start the local stack after creating `.env` from `.env.example`.
