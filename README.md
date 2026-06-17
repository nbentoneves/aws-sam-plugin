# wtr-aws-sam-debug

A collection of shell scripts to simplify running and debugging AWS SAM Lambda functions locally using `sam local invoke` with remote JVM debug support.

---

## Prerequisites

- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html) installed and available on `PATH`
- [Docker](https://www.docker.com/get-started/) installed and running
- A SAM-compatible project with a `template.yaml` and an event JSON file

---

## Scripts

### `run-sam-debug.sh`

Builds a SAM application and invokes a Lambda function locally with remote JVM debug enabled. Once running, you can attach an IntelliJ (or any JDWP-compatible) remote debugger to the specified port.

#### Usage

```zsh
./run-sam-debug.sh <FunctionLogicalId> [event-file] [debug-port] [template-file]
```

#### Arguments

| Argument | Required | Default | Description |
|---|---|---|---|
| `FunctionLogicalId` | ✅ Yes | — | The logical ID of the Lambda function as defined in your SAM template |
| `event-file` | No | `events/event.json` | Path to the JSON event file used to invoke the function |
| `debug-port` | No | `5005` | Local port to expose for remote JVM debug |
| `template-file` | No | `template.yaml` | Path to the SAM template file |

#### Environment Variables

| Variable | Description |
|---|---|
| `SAM_BUILD_ARGS` | Extra arguments appended to the `sam build` command |
| `SAM_INVOKE_ARGS` | Extra arguments appended to the `sam local invoke` command |

#### Examples

```zsh
# Minimal — uses all defaults
./run-sam-debug.sh Function

# Custom event file
./run-sam-debug.sh Function events/my-event.json

# Custom event file and debug port
./run-sam-debug.sh Function events/my-event.json 5006

# All arguments explicit
./run-sam-debug.sh Function events/my-event.json 5005 template.yaml

# Pass extra SAM build args via environment variable
SAM_BUILD_ARGS="--use-container" ./run-sam-debug.sh Function
```

#### What It Does

1. Validates that `sam` and `docker` are available on `PATH`
2. Validates that the template file and event file exist
3. Runs `sam build -t <template-file>` (plus any `SAM_BUILD_ARGS`)
4. Runs `sam local invoke` against the built template with debug mode enabled on the specified port
5. Prints a reminder to attach your IntelliJ Remote JVM Debug configuration to `localhost:<debug-port>`

#### Attaching the IntelliJ Debugger

Once the script outputs `Attach IntelliJ Remote JVM Debug to localhost:<port>`, create or use a **Remote JVM Debug** run configuration in IntelliJ:

- **Host:** `localhost`
- **Port:** the debug port (default `5005`)
- **Debugger mode:** Attach to remote JVM

The Lambda container waits for the debugger to connect before executing, so attach promptly after the script starts.

---

### `link-run-sam-debug.sh`

Creates a symlink of `run-sam-debug.sh` inside the `.aws-sam/` directory of a target repository. This allows each service repository to reference a single shared copy of `run-sam-debug.sh` without duplicating it.

#### Usage

```zsh
./link-run-sam-debug.sh <absolute-path-to-run-sam-debug.sh> <absolute-path-to-repository-root>
```

> **Both paths must be absolute.**

#### Arguments

| Argument | Required | Description |
|---|---|---|
| `absolute-path-to-run-sam-debug.sh` | ✅ Yes | Absolute path to the `run-sam-debug.sh` script in this repository |
| `absolute-path-to-repository-root` | ✅ Yes | Absolute path to the root of the service repository where the symlink should be created |

#### Example

```zsh
./link-run-sam-debug.sh \
  /Users/me/dev/wtr-aws-sam-debug/run-sam-debug.sh \
  /Users/me/dev/my-service
```

This creates:

```
/Users/me/dev/my-service/.aws-sam/run-sam-debug.sh -> /Users/me/dev/wtr-aws-sam-debug/run-sam-debug.sh
```

#### What It Does

1. Validates that both provided paths are absolute
2. Validates that the source script file and target repository directory exist
3. Creates the `.aws-sam/` directory inside the target repository if it does not already exist
4. If a symlink already exists at the target location, removes it and recreates it pointing to the new source
5. If a regular file (not a symlink) exists at the target location, exits with an error to avoid accidental overwrites

#### Typical Workflow

1. Clone this repository once to a stable location on your machine
2. For each service repository you want to debug locally, run `link-run-sam-debug.sh` once
3. From within the service repository, invoke `.aws-sam/run-sam-debug.sh` (or the symlink) as needed

```zsh
# Step 1 — clone this repo (one-time)
git clone <this-repo-url> ~/dev/wtr-aws-sam-debug

# Step 2 — link into a service repo (once per service)
~/dev/wtr-aws-sam-debug/link-run-sam-debug.sh \
  ~/dev/wtr-aws-sam-debug/run-sam-debug.sh \
  ~/dev/my-service

# Step 3 — run and debug from the service repo
cd ~/dev/my-service
.aws-sam/run-sam-debug.sh MyFunction
```

---

## Repository Structure

```
wtr-aws-sam-debug/
├── run-sam-debug.sh          # Core script: builds and locally invokes a Lambda with debug enabled
└── link-run-sam-debug.sh     # Helper script: symlinks run-sam-debug.sh into a target repository
```
