#!/usr/bin/env bash
set -euo pipefail

: "${BUILD_JDK11_IMAGE:?Set BUILD_JDK11_IMAGE}"
: "${RUNTIME_JRE11_IMAGE:?Set RUNTIME_JRE11_IMAGE}"

docker build \
  --build-arg BUILD_JDK11_IMAGE="${BUILD_JDK11_IMAGE}" \
  --build-arg RUNTIME_JRE11_IMAGE="${RUNTIME_JRE11_IMAGE}" \
  -t e2e-event-generator:0.1.0 .
