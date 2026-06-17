#!/bin/zsh
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./link-run-sam-debug.sh <absolute-path-to-run-sam-debug.sh> <absolute-path-to-repository-root>

Example:
  ./link-run-sam-debug.sh /Users/me/dev/wtr-aws-sam-debug/run-sam-debug.sh /Users/me/dev/my-service

Creates:
  /Users/me/dev/my-service/.aws-sam/run-sam-debug.sh -> /Users/me/dev/wtr-aws-sam-debug/run-sam-debug.sh
EOF
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -ne 2 ]]; then
  usage
  exit 1
fi

SOURCE_SCRIPT="$1"
REPO_ROOT="$2"
TARGET_DIR="$REPO_ROOT/.aws-sam"
TARGET_LINK="$TARGET_DIR/run-sam-debug.sh"

if [[ "$SOURCE_SCRIPT" != /* ]]; then
  echo "Error: source script path must be absolute: $SOURCE_SCRIPT"
  exit 1
fi

if [[ "$REPO_ROOT" != /* ]]; then
  echo "Error: repository root path must be absolute: $REPO_ROOT"
  exit 1
fi

if [[ ! -f "$SOURCE_SCRIPT" ]]; then
  echo "Error: source script not found: $SOURCE_SCRIPT"
  exit 1
fi

if [[ ! -d "$REPO_ROOT" ]]; then
  echo "Error: repository root not found: $REPO_ROOT"
  exit 1
fi

mkdir -p "$TARGET_DIR"

if [[ -e "$TARGET_LINK" || -L "$TARGET_LINK" ]]; then
  if [[ -L "$TARGET_LINK" ]]; then
    rm "$TARGET_LINK"
  else
    echo "Error: target exists and is not a symlink: $TARGET_LINK"
    exit 1
  fi
fi

ln -s "$SOURCE_SCRIPT" "$TARGET_LINK"

echo "Created symlink: $TARGET_LINK -> $SOURCE_SCRIPT"
