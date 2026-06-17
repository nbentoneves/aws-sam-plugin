#!/bin/zsh
set -euo pipefail

# Resolve the directory where this script lives (the project root)
SCRIPT_DIR="${0:A:h}"
SOURCE_SCRIPT="$SCRIPT_DIR/run-sam-debug.sh"

usage() {
  cat <<'EOF'
Usage:
  ./link-run-sam-debug.sh

You will be prompted to enter:
  1. Absolute path to the repository root where the symlink should be created

The source script (run-sam-debug.sh) is automatically resolved from the
directory where this script is located.

Creates:
  <repository-root>/.aws-sam/run-sam-debug.sh -> <this-project>/run-sam-debug.sh
EOF
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi

if [[ ! -f "$SOURCE_SCRIPT" ]]; then
  echo "Error: run-sam-debug.sh not found in project root: $SOURCE_SCRIPT"
  exit 1
fi

read "REPO_ROOT?Enter the absolute path to the repository root: "

TARGET_DIR="$REPO_ROOT/.aws-sam"
TARGET_LINK="$TARGET_DIR/run-sam-debug.sh"

if [[ "$REPO_ROOT" != /* ]]; then
  echo "Error: repository root path must be absolute: $REPO_ROOT"
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
