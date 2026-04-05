#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
PATCH_DIR="$ROOT_DIR/flashback-playback-rate-patch"

if [[ ! -f "$ROOT_DIR/build/libs/flashback-0.39.3.jar" ]]; then
  echo "[INFO] Flashback jar not found, building main project first..."
  "$ROOT_DIR/gradlew" -p "$ROOT_DIR" jar
fi

echo "[INFO] Building standalone patch mod..."
"$ROOT_DIR/gradlew" -p "$PATCH_DIR" build

echo "[DONE] Output jars:"
ls -1 "$PATCH_DIR/build/libs"
