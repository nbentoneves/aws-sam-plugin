#!/bin/zsh
set -euo pipefail

usage() {
  cat <<'EOF'
Usage:
  ./run-sam-debug.sh <FunctionLogicalId> [event-file] [debug-port] [template-file]

Examples:
  ./run-sam-debug.sh Function
  ./run-sam-debug.sh Function events/event.json
  ./run-sam-debug.sh Function events/event.json 5005
  ./run-sam-debug.sh Function events/event.json 5005 template.yaml

Defaults:
  event-file    events/event.json
  debug-port    5005
  template-file template.yaml

Environment variables:
  SAM_BUILD_ARGS   Extra args for 'sam build'
  SAM_INVOKE_ARGS  Extra args for 'sam local invoke'
EOF
}

if [[ ${1:-} == "-h" || ${1:-} == "--help" ]]; then
  usage
  exit 0
fi

if [[ $# -lt 1 ]]; then
  usage
  exit 1
fi

FUNCTION_NAME="$1"
EVENT_FILE="${2:-events/event.json}"
DEBUG_PORT="${3:-5005}"
TEMPLATE_FILE="${4:-template.yaml}"
BUILT_TEMPLATE_FILE=".aws-sam/build/${TEMPLATE_FILE:t}"
SAM_BUILD_ARGS="${SAM_BUILD_ARGS:-}"
SAM_INVOKE_ARGS="${SAM_INVOKE_ARGS:-}"

if ! command -v sam >/dev/null 2>&1; then
  echo "Error: sam CLI not found in PATH"
  exit 1
fi

if ! command -v docker >/dev/null 2>&1; then
  echo "Error: docker not found in PATH"
  exit 1
fi

if [[ ! -f "$TEMPLATE_FILE" ]]; then
  echo "Error: template file not found: $TEMPLATE_FILE"
  exit 1
fi

if [[ ! -f "$EVENT_FILE" ]]; then
  echo "Error: event file not found: $EVENT_FILE"
  exit 1
fi

cat <<EOF
Starting SAM local debug
- Function: $FUNCTION_NAME
- Event: $EVENT_FILE
- Debug port: $DEBUG_PORT
- Source template: $TEMPLATE_FILE
EOF

echo
echo "Building SAM application..."
sam build -t "$TEMPLATE_FILE" ${=SAM_BUILD_ARGS}

if [[ ! -f "$BUILT_TEMPLATE_FILE" ]]; then
  echo "Error: built template not found: $BUILT_TEMPLATE_FILE"
  exit 1
fi

echo
echo "Starting Lambda in debug mode..."
echo "Attach IntelliJ Remote JVM Debug to localhost:$DEBUG_PORT"
echo "Using built template: $BUILT_TEMPLATE_FILE"
echo

sam local invoke "$FUNCTION_NAME" \
  -t "$BUILT_TEMPLATE_FILE" \
  -e "$EVENT_FILE" \
  -d "$DEBUG_PORT" \
  ${=SAM_INVOKE_ARGS}
