package com.github.sam.debug

import javax.inject.Inject
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ProjectLayout
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = 'SAM task base class supports external process execution and is not intended to be cacheable by default.')
abstract class AbstractSamTask extends DefaultTask {

    @Input
    final Property<String> functionName = project.objects.property(String)

    @Input
    final Property<String> templateFile = project.objects.property(String)

    @Input
    final ListProperty<String> buildArgs = project.objects.listProperty(String)

    @Inject
    protected abstract ExecOperations getExecOperations()

    @Inject
    protected abstract ProjectLayout getProjectLayout()

    protected File resolveProjectFile(String relativePath) {
        projectLayout.projectDirectory.file(relativePath).asFile
    }

    protected File resolveBuiltTemplateFile() {
        def template = resolveProjectFile(templateFile.get())
        projectLayout.projectDirectory.file(".aws-sam/build/${template.name}").asFile
    }

    protected String shellQuote(String value) {
        return "'" + value.replace("'", "'\\''") + "'"
    }

    protected void runInZsh(String shellCommand) {
        execOperations.exec {
            workingDir project.projectDir
            commandLine '/bin/zsh', '-lc', shellCommand
        }
    }

    protected void validateCommandAvailable(String executable) {
        def result = execOperations.exec {
            workingDir project.projectDir
            commandLine '/bin/zsh', '-lc', "command -v ${executable} >/dev/null 2>&1"
            ignoreExitValue = true
        }
        if (result.exitValue != 0) {
            throw new GradleException("${executable} CLI not found in PATH for /bin/zsh login shell")
        }
    }

    protected void validateRequiredFile(String path, String label) {
        def file = resolveProjectFile(path)
        if (!file.isFile()) {
            throw new GradleException("${label} not found: ${file}")
        }
    }

    protected void validateFunctionName() {
        if (!functionName.present || functionName.get().isBlank()) {
            throw new GradleException('samDebug.functionName must be configured')
        }
    }
}
