package com.github.sam.debug

import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Runs sam local invoke in remote debug mode and depends on local Docker and debugger interaction.')
abstract class SamLocalDebugTask extends AbstractSamTask {

    @Input
    final Property<String> eventFile = project.objects.property(String)

    @Input
    final Property<Integer> debugPort = project.objects.property(Integer)

    @Input
    final ListProperty<String> invokeArgs = project.objects.listProperty(String)

    @TaskAction
    void debug() {
        validateFunctionName()
        validateCommandAvailable('sam')
        validateCommandAvailable('docker')
        validateRequiredFile(templateFile.get(), 'Template file')
        validateRequiredFile(eventFile.get(), 'Event file')

        def builtTemplateFile = resolveBuiltTemplateFile()
        if (!builtTemplateFile.isFile()) {
            throw new IllegalStateException("Built template not found: ${builtTemplateFile}")
        }

        logger.lifecycle('Starting SAM local debug')
        logger.lifecycle("- Function: ${functionName.get()}")
        logger.lifecycle("- Event: ${eventFile.get()}")
        logger.lifecycle("- Debug port: ${debugPort.get()}")
        logger.lifecycle("- Source template: ${templateFile.get()}")
        logger.lifecycle("Attach IntelliJ Remote JVM Debug to localhost:${debugPort.get()}")
        logger.lifecycle("Using built template: ${builtTemplateFile}")

        def command = [
            'sam', 'local', 'invoke', functionName.get(),
            '-t', builtTemplateFile.absolutePath,
            '-e', resolveProjectFile(eventFile.get()).absolutePath,
            '-d', debugPort.get().toString()
        ] + invokeArgs.get()

        runInZsh(command.collect { shellQuote(it) }.join(' '))
    }
}
