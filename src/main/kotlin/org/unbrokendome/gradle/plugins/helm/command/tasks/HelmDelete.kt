package org.unbrokendome.gradle.plugins.helm.command.tasks

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.unbrokendome.gradle.plugins.helm.util.property


/**
 * Deletes a release from the cluster. Corresponds to the `helm delete` CLI command.
 */
open class HelmDelete : AbstractHelmServerCommandTask() {

    /**
     * The name of the release to be deleted.
     */
    @get:Input
    val releaseName: Property<String> =
            project.objects.property()


    /**
     * If `true`, simulate a delete.
     */
    @get:Internal
    val dryRun: Property<Boolean> =
            project.objects.property()


    /**
     * If `true`, remove the release from the store and make its name free for later use.
     */
    @get:Internal
    val purge: Property<Boolean> =
            project.objects.property()


    init {
        @Suppress("LeakingThis")
        inputs.dir(actualHelmHome)
    }


    @TaskAction
    fun deleteRelease() {
        execHelm("delete") {
            flag("--purge", purge)
            flag("--dry-run", dryRun)
            args(releaseName)
        }
    }
}
