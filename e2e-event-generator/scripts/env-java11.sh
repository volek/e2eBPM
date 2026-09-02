#!/usr/bin/env bash
# Source before build/run: source scripts/env-java11.sh
export JAVA_HOME="${JAVA_HOME:-$HOME/.local/share/jdk-11-temurin}"
export PATH="$JAVA_HOME/bin:$PATH"
java -version
