package dev.zwander.shared

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import kotlin.reflect.KClass

/**
 * [LaunchStrategy] and [LaunchStrategyRootGroup]
 * should have sealed classes that extend them in respective
 * modules that can then in turn be extended for concrete strategies.
 */

abstract class BaseLaunchStrategy(
    @StringRes open val labelRes: Int,
) {
    open val Context.label: String
        get() = resources.getString(labelRes)
}

abstract class LaunchStrategy(
    val key: String,
    @StringRes labelRes: Int,
    val sequentialLaunch: Boolean = true,
    val intentCreator: LaunchIntentCreator,
) : BaseLaunchStrategy(labelRes) {
    abstract val sourceUrl: String?

    fun Context.createIntents(url: String): List<Intent> {
        return with (intentCreator) { createIntents(url) }
    }

    fun Context.isInstalled(): Boolean {
        val intents = createIntents("https://")

        return intents.any {
            packageManager.queryIntentActivities(it, 0).isNotEmpty()
        }
    }
}

/**
 * The base class for adding an app group. Even if there's only one variant of the app to add,
 * this should be used.
 */
abstract class LaunchStrategyRootGroup(@StringRes labelRes: Int, val autoAdd: Boolean = true) : BaseLaunchStrategy(labelRes) {
    open val children: List<LaunchStrategy> by lazy {
        if (autoAdd) processNestedClasses(this::class) else listOf()
    }

    /**
     * Recursively traverse the group instance class and find individual launch strategy objects.
     */
    private fun processNestedClasses(parent: KClass<out BaseLaunchStrategy>): List<LaunchStrategy> {
        return parent.nestedClasses.mapNotNull {
            if (it.objectInstance != null) {
                (it.objectInstance as? LaunchStrategy)?.let { obj -> listOf(obj) }
            } else {
                @Suppress("UNCHECKED_CAST")
                (it as? KClass<LaunchStrategy>)?.let { casted -> processNestedClasses(casted) }
            }
        }.flatten()
    }
}

data class DiscoveredGroup(
    override val children: List<LaunchStrategy>,
) : LaunchStrategyRootGroup(labelRes = R.string.discovered, autoAdd = false)

data class DiscoveredLaunchStrategy(
    val packageName: String,
    val components: List<ComponentName>,
    val launchAction: String,
    @StringRes override val labelRes: Int = 0,
    private val _label: String,
) : LaunchStrategy(
    key = packageName,
    labelRes = labelRes,
    sequentialLaunch = false,
    intentCreator = LaunchIntentCreator.DiscoveredIntentCreator(
        components = components,
        launchAction = launchAction,
    ),
) {
    override val sourceUrl: String? = null

    override val Context.label: String
        get() = _label
}
