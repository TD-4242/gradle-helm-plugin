package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.util.GFileUtils
import org.unbrokendome.gradle.plugins.helm.command.valuesOptions
import org.unbrokendome.gradle.plugins.helm.util.*


/**
 * Runs a series of tests to verify that a chart is well-formed.
 * Corresponds to the `helm unittest` helm plugin CLI command.
 */
open class HelmUnittest : AbstractHelmCommandTask() {

    /**
     * The directory that contains the sources for the Helm chart.
     */
    @get:[InputDirectory SkipWhenEmpty]
    @Suppress("LeakingThis")
    val chartDir: DirectoryProperty =
            project.objects.directoryProperty()


    /**
     * If set to `true`, fail on warnings emitted by the unittester.
     */
    @get:[Input Optional]
    val strict: Property<Boolean> =
            project.objects.property()


    /**
     * Values to be used by the unittester.
     */
    @get:Input
    val values: MapProperty<String, Any> =
            project.objects.mapProperty()


    /**
     * A collection of YAML files containing values to be used by the unittester.
     */
    @get:InputFiles
    val valueFiles: ConfigurableFileCollection =
            project.layout.configurableFiles()


    /**
     * If set, the task will create an empty marker file at this path after a successful call to `helm unittest`.
     *
     * This is necessary for Gradle's up-to-date checking because `helm unittest` itself doesn't output any
     * files.
     */
    @get:[OutputFile Optional]
    val outputMarkerFile: RegularFileProperty =
            project.objects.fileProperty()


    init {
        @Suppress("LeakingThis")
        inputs.dir(actualHelmHome)
    }

    @TaskAction
    fun installUnittest() {

        execHelm("plugin install") {
            valuesOptions(values, valueFiles)
            args(unittestPluginURL)
        }
    }
        

    @TaskAction
    fun unittest() {

        execHelm("unittest") {
            flag("--strict", strict)
            valuesOptions(values, valueFiles)
            args(chartDir)
        }

        outputMarkerFile.ifPresent {
            GFileUtils.touch(it.asFile)
        }
    }
}
