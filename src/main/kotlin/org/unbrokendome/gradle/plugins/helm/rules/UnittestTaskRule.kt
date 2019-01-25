package org.unbrokendome.gradle.plugins.helm.rules

import org.gradle.api.tasks.TaskContainer
import org.unbrokendome.gradle.plugins.helm.command.tasks.HelmUnittest
import org.unbrokendome.gradle.plugins.helm.dsl.HelmChart
import org.unbrokendome.gradle.plugins.helm.dsl.lint
import org.unbrokendome.gradle.plugins.helm.tasks.HelmFilterSources


/**
 * A rule that creates a [HelmUnittest] task for each chart.
 */
internal class UnittestTaskRule(
        private val tasks: TaskContainer,
        private val charts: Iterable<HelmChart>)
    : AbstractRule() {

    internal companion object {
        fun getTaskName(chartName: String) =
                "helmUnittest${chartName.capitalize()}Chart"
    }

    private val regex = Regex(getTaskName("(\\p{Upper}.*)"))


    override fun getDescription(): String =
            "Pattern: ${getTaskName("<Chart>")}"


    override fun apply(taskName: String) {
        if (regex.matches(taskName)) {
            charts.find { it.lintTaskName == taskName }
                    ?.let { chart ->

                        val filterSourcesTask = tasks.getByName(chart.filterSourcesTaskName) as HelmFilterSources

                        tasks.create(taskName, HelmUnittest::class.java) { task ->
                            task.description = "Unittest the ${chart.name} chart."

                            task.chartDir.set(filterSourcesTask.targetDir)

                            chart.lint.let { chartUnittest ->
                                task.onlyIf { chartUnittest.enabled.get() }
                                task.strict.set(chartUnittest.strict)
                                task.values.putAll(chartUnittest.values)
                                task.valueFiles.from(chartUnittest.valueFiles)
                            }

                            task.dependsOn(chart.updateDependenciesTaskName)
                        }
                    }
        }
    }
}


/**
 * The name of the [HelmUnittest] task for this chart.
 */
val HelmChart.lintTaskName
    get() = UnittestTaskRule.getTaskName(name)
