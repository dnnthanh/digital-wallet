#!/usr/bin/env sh
set -eu
: "${SERVICE_MODULE:?SERVICE_MODULE must be set to a backend service module name}"
JAR="/opt/digital-wallet/services/${SERVICE_MODULE}/app.jar"
if [ ! -f "$JAR" ]; then
  echo "Backend runtime JAR not found for SERVICE_MODULE=${SERVICE_MODULE}: ${JAR}" >&2
  exit 64
fi
exec java ${JAVA_OPTS:-} -jar "$JAR"
