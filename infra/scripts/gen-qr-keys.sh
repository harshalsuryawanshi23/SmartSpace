#!/bin/bash
set -e

mkdir -p run/keys
# Generate Ed25519 private key
openssl genpkey -algorithm ed25519 -out run/keys/qr_private.pem
# Extract public key
openssl pkey -in run/keys/qr_private.pem -pubout -out run/keys/qr_public.pem

echo "Generated Ed25519 QR signing keys in run/keys/"
