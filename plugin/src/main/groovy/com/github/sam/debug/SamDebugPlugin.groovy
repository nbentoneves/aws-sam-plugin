package com.github.sam.debug

import org.gradle.api.Plugin
import org.gradle.api.Project

class SamDebugPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('samDebug', SamDebugExtension)

        project.tasks.register('samBuild', SamBuildTask) {
            group = 'SAM Debug'
            description = 'Builds the AWS SAM application using the configured template file.'
        }

        project.tasks.register('samDebug', SamLocalDebugTask) {
            group = 'SAM Debug'
            description = 'Builds and invokes the configured AWS SAM function locally with remote JVM debug enabled.'
        }

        project.afterEvaluate {
            project.tasks.named('samBuild', SamBuildTask) {
                functionName.convention(extension.functionName)
                templateFile.convention(extension.templateFile)
                buildArgs.convention(extension.buildArgs)
            }

            project.tasks.named('samDebug', SamLocalDebugTask) {
                functionName.convention(extension.functionName)
                eventFile.convention(extension.eventFile)
                debugPort.convention(extension.debugPort)
                templateFile.convention(extension.templateFile)
                buildArgs.convention(extension.buildArgs)
                invokeArgs.convention(extension.invokeArgs)
            }
        }
    }
}
