#!/bin/sh
set -eu

export KUBECONFIG="$HOME/.kube/config"

image="${1:?Передайте полный адрес образа с тегом}"
expected_sha="${image##*:}"

echo "Deploying frontend: $image"

kubectl set image deployment/frontend \
    frontend="$image" \
    -n tech-support

kubectl rollout status deployment/frontend \
    -n tech-support \
    --timeout=5m

actual_image="$(kubectl get deployment frontend \
    -n tech-support \
    -o jsonpath='{.spec.template.spec.containers[?(@.name=="frontend")].image}')"

if [ "$actual_image" != "$image" ]; then
    echo "ERROR: expected $image, got $actual_image" >&2
    exit 1
fi

echo "Checking frontend version"
version_ok=false

for attempt in 1 2 3 4 5 6 7 8 9 10 11 12; do
    if actual_sha="$(curl --fail --silent --show-error \
        --connect-timeout 5 \
        --max-time 15 \
        https://88-218-67-241.sslip.io/version.txt)" \
        && [ "$actual_sha" = "$expected_sha" ]; then
        echo "Frontend version: $actual_sha"
        version_ok=true
        break
    fi

    echo "Waiting for frontend version $expected_sha ($attempt/12)"
    sleep 5
done

if [ "$version_ok" != true ]; then
    echo "ERROR: frontend version verification failed" >&2
    exit 1
fi

echo "Checking API through frontend nginx"
curl --fail --silent --show-error \
    --retry 12 \
    --retry-delay 5 \
    --retry-max-time 90 \
    --connect-timeout 5 \
    --max-time 15 \
    https://88-218-67-241.sslip.io/api/version

printf '\nDeployment successful: %s\n' "$actual_image"