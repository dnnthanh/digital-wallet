#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
unset COMPOSE_FILE || true
export COMPOSE_PARALLEL_LIMIT="${COMPOSE_PARALLEL_LIMIT:-2}"
exec docker compose --project-directory "$ROOT" -f "$ROOT/docker-compose.yml" "$@"
