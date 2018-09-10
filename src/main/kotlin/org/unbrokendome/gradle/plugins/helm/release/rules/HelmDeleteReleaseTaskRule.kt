package org.unbrokendome.gradle.plugins.helm.release.rules

import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.tasks.TaskContainer
import org.unbrokendome.gradle.plugins.helm.command.tasks.HelmDelete
import org.unbrokendome.gradle.plugins.helm.release.HelmReleasesPlugin
import org.unbrokendome.gradle.plugins.helm.release.dsl.HelmRelease
import org.unbrokendome.gradle.plugins.helm.rules.AbstractRule
import org.unbrokendome.gradle.plugins.helm.util.capitalizeWords


/**
 * A rule that creates a [HelmDelete] task for a release.
 */
internal class HelmDeleteReleaseTaskRule(
        private val tasks: TaskContainer,
        private val releases: NamedDomainObjectContainer<HelmRelease>)
    : AbstractRule() {


    internal companion object {

        const val TaskNamePrefix = "helmDelete"

        fun getTaskName(releaseName: String) =
                TaskNamePrefix + releaseName.capitalizeWords()
    }


    override fun getDescription(): String =
            "Pattern: $TaskNamePrefix<Release>"


    override fun apply(taskName: String) {
        if (taskName.startsWith(TaskNamePrefix)) {

            releases.find { getTaskName(it.name) == taskName }
                    ?.let { release ->

                        tasks.create(taskName, HelmDelete::class.java) { task ->
                            task.description = "Deletes the ${release.name} release."

                            task.releaseName.set(release.releaseName)
                            task.dryRun.set(release.dryRun)
                            task.purge.set(release.purge)

                            task.dependsOn(HelmReleasesPlugin.initServerTaskName)
                        }
                    }
        }
    }
}


/**
 * The name of the [HelmDelete] task associated with this release.
 */
val HelmRelease.deleteTaskName: String
    get() = HelmDeleteReleaseTaskRule.getTaskName(name)