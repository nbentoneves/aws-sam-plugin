# aws-sam-debug

A Gradle plugin and supporting shell scripts to simplify building and debugging AWS SAM Lambda functions locally using `sam build` and `sam local invoke` with remote JVM debug support. [cite:51][cite:57]

---

## Prerequisites

- [AWS SAM CLI](https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html) installed and available on `PATH`
- [Docker](https://www.docker.com/get-started/) installed and running
- A SAM-compatible Gradle project with a `template.yaml` and an event JSON file
- Java available for running Gradle

---

## Gradle Plugin

The Gradle plugin is now the recommended way to build and debug a SAM Lambda locally. It provides native Gradle tasks instead of requiring repositories to copy and invoke a shell script manually. [cite:53][cite:57]

### Plugin ID

```groovy
com.github.sam.debug
```

### Apply the plugin

```groovy
plugins {
    id 'com.github.sam.debug' version '1.0.0'
}
```

### Configure the plugin

```groovy
samDebug {
    functionName = 'Function'
    eventFile = 'events/event.json'
    debugPort = 5005
    templateFile = 'template.yaml'
    buildArgs = ['--use-container']
    invokeArgs = []
}
```

### Configuration

| Property | Required | Default | Description |
|---|---|---|---|
| `functionName` | ✅ Yes | — | Logical ID of the Lambda function in the SAM template [cite:52] |
| `eventFile` | No | `events/event.json` | Path to the JSON event file relative to the project root [cite:52] |
| `debugPort` | No | `5005` | Local port exposed for remote JVM debug [cite:52] |
| `templateFile` | No | `template.yaml` | Path to the SAM template file relative to the project root [cite:52] |
| `buildArgs` | No | `[]` | Extra arguments appended to `sam build` [cite:52] |
| `invokeArgs` | No | `[]` | Extra arguments appended to `sam local invoke` [cite:52] |

### Tasks

| Task | Description |
|---|---|
| `./gradlew samBuild` | Runs `sam build -t <templateFile>` from the project root and verifies the built template exists under `.aws-sam/build/` [cite:55] |
| `./gradlew samDebug` | Depends on `samBuild`, then runs `sam local invoke` against the built template with debug mode enabled on the configured port [cite:53][cite:57] |

### Example usage

```bash
./gradlew samBuild
./gradlew samDebug
```

### IntelliJ remote debugger

Once `samDebug` starts, attach an IntelliJ **Remote JVM Debug** configuration to the configured port. The plugin logs a reminder to attach to `localhost:<debugPort>` before the Lambda executes. [cite:57]

Use the following IntelliJ settings:

- **Host:** `localhost`
- **Port:** `5005` by default, or your configured `debugPort`
- **Debugger mode:** Attach to remote JVM

---

## Legacy scripts

The repository still contains shell scripts that implement the original workflow. The `run-sam-debug.sh` script accepts a function name, event file, debug port, and template file, then runs `sam build` followed by `sam local invoke` with `-d <debugPort>`. [cite:11]

The new Gradle plugin mirrors that same behaviour in Gradle-native tasks, so repositories can configure and run the workflow with `./gradlew samBuild` and `./gradlew samDebug` instead of invoking a copied script manually. [cite:52][cite:55][cite:57]

### `run-sam-debug.sh`

```zsh
./run-sam-debug.sh <FunctionLogicalId> [event-file] [debug-port] [template-file]
```

### `link-run-sam-debug.sh`

```zsh
./link-run-sam-debug.sh
```

This helper script remains available only for repositories still using the script-based setup. It resolves `run-sam-debug.sh` from this project and creates a symlink under `.aws-sam/` in a target repository. [cite:9][cite:10]

---

## Plugin development

The Gradle plugin project lives under the `plugin/` directory and is configured as a Gradle plugin with id `com.github.sam.debug`. [cite:51]

Typical local development commands:

```bash
cd plugin
./gradlew build
./gradlew publishToMavenLocal
```

After publishing to the local Maven repository, a consumer Gradle project can reference the plugin from local development infrastructure before it is published to the Gradle Plugin Portal.
