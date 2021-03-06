package org.unbrokendome.gradle.plugins.helm.rules

import org.gradle.api.tasks.TaskContainer
import org.unbrokendome.gradle.plugins.helm.HelmPlugin
import org.unbrokendome.gradle.plugins.helm.dsl.HelmChart
import org.unbrokendome.gradle.plugins.helm.tasks.HelmFilterSources
import org.unbrokendome.gradle.plugins.helm.command.tasks.HelmBuildOrUpdateDependencies


/**
 * A rule that creates a [HelmBuildOrUpdateDependencies] task for each chart.
 */
internal class BuildDependenciesTaskRule(
        private val tasks: TaskContainer,
        private val charts: Iterable<HelmChart>)
    : AbstractRule() {

    internal companion object {
        fun getTaskName(chartName: String) =
                "helmUpdate${chartName.capitalize()}ChartDependencies"
    }


    private val regex = Regex(getTaskName("(\\p{Upper}.*)"))


    override fun getDescription(): String =
            "Pattern: " + getTaskName("<Chart>")


    override fun apply(taskName: String) {
        if (regex.matches(taskName)) {
            charts.find { it.updateDependenciesTaskName == taskName }
                    ?.let { chart ->

                        val filterSourcesTask = tasks.getByName(chart.filterSourcesTaskName) as HelmFilterSources

                        tasks.create(taskName, HelmBuildOrUpdateDependencies::class.java) { task ->
                            task.description = "Builds or updates the dependencies for the ${chart.name} chart."
                            task.chartDir.set(filterSourcesTask.targetDir)
                            task.dependsOn(HelmPlugin.addRepositoriesTaskName)
                            task.dependsOn(filterSourcesTask)
                        }
                    }
        }
    }
}


/**
 * The name of the [HelmBuildOrUpdateDependencies] task for this chart.
 */
val HelmChart.updateDependenciesTaskName
    get() = BuildDependenciesTaskRule.getTaskName(name)
