#!/usr/bin/env bash
set -euo pipefail
: "${WALLET_DB_PASSWORD:?WALLET_DB_PASSWORD is required}"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
  --set=wallet_password="$WALLET_DB_PASSWORD" <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'wallet_account') THEN
    CREATE ROLE wallet_account LOGIN;
  END IF;
END
$$;
ALTER ROLE wallet_account PASSWORD :'wallet_password';
SELECT 'CREATE DATABASE wallet_account_db OWNER wallet_account'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'wallet_account_db')\gexec
SQL
