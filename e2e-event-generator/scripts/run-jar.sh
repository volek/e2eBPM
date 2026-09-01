#!/usr/bin/env bash
set -euo pipefail

java -jar build/libs/e2e-event-generator.jar \
  --generator.command="${GENERATOR_COMMAND:-run}" \
  --generator.scenario="${GENERATOR_SCENARIO:-full-poc}"
