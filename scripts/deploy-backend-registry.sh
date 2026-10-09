#!/bin/sh
set -eu

export KUBECONFIG="$HOME/.kube/config"

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
    --retry 12 \
    --retry-delay 5 \
    --retry-max-time 90 \
    --connect-timeout 5 \
    --max-time 15 \
    https://88-218-67-241.sslip.io/api/version

printf '\nDeployment successful: %s\n' "$actual_image"