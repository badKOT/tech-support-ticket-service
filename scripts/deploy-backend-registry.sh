#!/bin/sh
set -eu

image="${1:?Передайте полный адрес образа с тегом}"

echo "Deploying backend: $image"

kubectl set image deployment/backend \
    backend="$image" \
    -n tech-support

kubectl rollout status deployment/backend \
    -n tech-support \
    --timeout=5m

actual_image="$(kubectl get deployment backend \
    -n tech-support \
    -o jsonpath='{.spec.template.spec.containers[?(@.name=="backend")].image}')"

if [ "$actual_image" != "$image" ]; then
    echo "ERROR: expected $image, got $actual_image" >&2
    exit 1
fi

echo "Checking /api/version"
curl --fail --silent --show-error \
    --connect-timeout 10 \
    --max-time 30 \
    https://88-218-67-241.sslip.io/api/version

printf '\nDeployment successful: %s\n' "$actual_image"