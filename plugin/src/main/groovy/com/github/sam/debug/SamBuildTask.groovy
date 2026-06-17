package com.github.sam.debug

import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'Runs the external SAM CLI and depends on local environment and Docker state.')
abstract class SamBuildTask extends AbstractSamTask {

    @TaskAction
    void build() {
        validateFunctionName()
        validateCommandAvailable('sam')
        validateCommandAvailable('docker')
        validateRequiredFile(templateFile.get(), 'Template file')

        logger.lifecycle('Starting SAM build')
        logger.lifecycle("- Function: ${functionName.get()}")
        logger.lifecycle("- Source template: ${templateFile.get()}")

        def command = ['sam', 'build', '-t', templateFile.get()] + buildArgs.get()
        runInZsh(command.collect { shellQuote(it) }.join(' '))

        def builtTemplateFile = resolveBuiltTemplateFile()
        if (!builtTemplateFile.isFile()) {
            throw new IllegalStateException("Built template not found: ${builtTemplateFile}")
        }

        logger.lifecycle("Using built template: ${builtTemplateFile}")
    }
}
