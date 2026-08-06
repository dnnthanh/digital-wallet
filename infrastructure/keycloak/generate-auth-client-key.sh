#!/bin/sh
set -eu

KEY_DIR="${AUTH_CLIENT_KEY_DIR:-/keys}"
PRIVATE_KEY="${KEY_DIR}/client-private.pem"

mkdir -p "${KEY_DIR}"
if [ ! -s "${PRIVATE_KEY}" ]; then
  umask 077
  openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out "${PRIVATE_KEY}"
fi
chown 10001:10001 "${PRIVATE_KEY}"
chmod 0400 "${PRIVATE_KEY}"
