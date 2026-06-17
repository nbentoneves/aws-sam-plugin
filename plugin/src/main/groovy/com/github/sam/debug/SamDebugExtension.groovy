package com.github.sam.debug

class SamDebugExtension {
    /**
     * Logical ID of the Lambda function in the SAM template.
     */
    String functionName

    /**
     * Path to the event JSON file relative to the project root.
     */
    String eventFile = 'events/event.json'

    /**
     * Remote debug port exposed by sam local invoke.
     */
    int debugPort = 5005

    /**
     * Path to the SAM template file relative to the project root.
     */
    String templateFile = 'template.yaml'

    /**
     * Extra arguments appended to the `sam build` command.
     */
    List<String> buildArgs = []

    /**
     * Extra arguments appended to the `sam local invoke` command.
     */
    List<String> invokeArgs = []
}
