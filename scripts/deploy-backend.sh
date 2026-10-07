#!/bin/sh

set -eu

IMAGE_TAG="$1"
IMAGE_NAME="tech-support-backend"
NAMESPACE="tech-support"
DEPLOYMENT="backend"
CONTAINER="backend"

TAR_FILE="/tmp/${IMAGE_NAME}-${IMAGE_TAG}.tar"
IMAGE="${IMAGE_NAME}:${IMAGE_TAG}"

echo "Deploying ${IMAGE}"

if [ ! -f "$TAR_FILE" ]; then
    echo "ERROR: image archive not found: $TAR_FILE"
    exit 1
fi

echo "Importing image into k3s..."
sudo k3s ctr images import "$TAR_FILE"

echo "Checking imported image..."
sudo k3s ctr images list | grep "${IMAGE_TAG}"

echo "Updating Kubernetes Deployment..."
kubectl set image \
    deployment/"$DEPLOYMENT" \
    "$CONTAINER"="$IMAGE" \
    -n "$NAMESPACE"

echo "Waiting for rollout..."
kubectl rollout status \
    deployment/"$DEPLOYMENT" \
    -n "$NAMESPACE" \
    --timeout=5m

echo "Checking deployed image..."
DEPLOYED_IMAGE=$(kubectl get deployment "$DEPLOYMENT" \
    -n "$NAMESPACE" \
    -o jsonpath='{.spec.template.spec.containers[0].image}')

if [ "$DEPLOYED_IMAGE" != "$IMAGE" ]; then
    echo "ERROR: expected ${IMAGE}, but Kubernetes uses ${DEPLOYED_IMAGE}"
    exit 1
fi

echo "Running smoke test..."
curl --fail --silent --show-error \
    https://88-218-67-241.sslip.io/api/version

echo
echo "Deployment successful: ${IMAGE}"

echo "Removing archive..."
sudo rm -f "$TAR_FILE"