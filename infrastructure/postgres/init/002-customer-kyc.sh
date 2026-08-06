#!/usr/bin/env bash
set -euo pipefail
: "${KYC_DB_PASSWORD:?KYC_DB_PASSWORD is required}"
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres \
  --set=kyc_password="$KYC_DB_PASSWORD" <<'SQL'
DO $$
BEGIN
  IF NOT EXISTS (SELECT FROM pg_catalog.pg_roles WHERE rolname = 'customer_kyc') THEN
    CREATE ROLE customer_kyc LOGIN;
  END IF;
END
$$;
ALTER ROLE customer_kyc PASSWORD :'kyc_password';
SELECT 'CREATE DATABASE customer_kyc_db OWNER customer_kyc'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'customer_kyc_db')\gexec
SQL
