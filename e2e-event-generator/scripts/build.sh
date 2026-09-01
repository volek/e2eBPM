#!/usr/bin/env bash
set -euo pipefail

./gradlew clean check bootJar

echo "Artifact: build/libs/e2e-event-generator.jar"
