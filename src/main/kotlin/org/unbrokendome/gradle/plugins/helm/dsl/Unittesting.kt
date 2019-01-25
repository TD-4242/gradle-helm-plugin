package org.unbrokendome.gradle.plugins.helm.dsl

import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.unbrokendome.gradle.plugins.helm.util.mapProperty
import org.unbrokendome.gradle.plugins.helm.util.property
import javax.inject.Inject


/**
 * Defines options for Unittesting Helm charts using the `helm unittest` helm plugin.
 */
interface Unittesting {

    /**
     * If `true` (the default), run the unittester.
     */
    val enabled: Property<Boolean>

    /**
     * Values to supply to the unittester.
     */
    val values: MapProperty<String, Any>

    /**
     * Value files to supply to the unittester.
     */
    val valueFiles: ConfigurableFileCollection
}


/**
 * Extension of [Unittesting] that supports setting values from a parent `Unittesting` instance.
 */
private interface UnittestingInternal : Unittesting, Hierarchical<Unittesting>


/**
 * Default implementation of [Unittesting].
 */
private open class DefaultUnittesting
@Inject constructor(objectFactory: ObjectFactory, projectLayout: ProjectLayout) : UnittestingInternal {

    override val enabled: Property<Boolean> =
            objectFactory.property<Boolean>()
                    .convention(true)

    override val strict: Property<Boolean> =
            objectFactory.property()

    override val values: MapProperty<String, Any> =
            objectFactory.mapProperty()

    override val valueFiles: ConfigurableFileCollection =
            projectLayout.configurableFiles()

    override fun setParent(parent: Unittesting) {
        enabled.set(parent.enabled)
        strict.set(parent.strict)
        values.putAll(parent.values)
        valueFiles.from(parent.valueFiles)
    }
}


/**
 * Creates a new [Unittesting] object using the given [ObjectFactory].
 *
 * @param objectFactory the Gradle [ObjectFactory] to create the object
 * @param parent the optional parent [Unittesting] object
 * @return the created [Unittesting] object
 */
internal fun createUnittesting(objectFactory: ObjectFactory, parent: Unittesting? = null): Unittesting =
        objectFactory.newInstance(DefaultUnittesting::class.java)
                .apply {
                    parent?.let(this::setParent)
                }
