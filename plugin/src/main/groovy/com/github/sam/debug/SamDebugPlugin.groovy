package com.github.sam.debug

import org.gradle.api.Plugin
import org.gradle.api.Project

class SamDebugPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create('samDebug', SamDebugExtension)

        project.tasks.register('installSamDebugScript', InstallSamDebugScriptTask) {
            group = 'SAM Debug'
            description = 'Copies run-sam-debug.sh into .aws-sam/ in the project root'
            outputDir.set(project.layout.projectDirectory.dir(extension.outputDir.get()))
        }

        // Wire extension changes after evaluation so user config is picked up
        project.afterEvaluate {
            project.tasks.named('installSamDebugScript', InstallSamDebugScriptTask) {
                outputDir.set(project.layout.projectDirectory.dir(extension.outputDir.get()))
            }
        }
    }
}
