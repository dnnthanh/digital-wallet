#!/usr/bin/env bash
set -euo pipefail
: "${KEYCLOAK_DB_PASSWORD:?KEYCLOAK_DB_PASSWORD is required}"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
  --set=keycloak_password="$KEYCLOAK_DB_PASSWORD" <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'keycloak') THEN
    CREATE ROLE keycloak LOGIN;
  END IF;
END
$$;
ALTER ROLE keycloak PASSWORD :'keycloak_password';
SELECT 'CREATE DATABASE keycloak_db OWNER keycloak'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'keycloak_db')\gexec
SQL
