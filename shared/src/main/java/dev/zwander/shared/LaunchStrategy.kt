package dev.zwander.shared

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import dev.zwander.shared.util.LifecycleEffect
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

    @Composable
    fun rememberIsInstalled(): Boolean {
        val context = LocalContext.current

        var isInstalled by remember {
            mutableStateOf(context.isInstalled())
        }

        LifecycleEffect(Lifecycle.State.RESUMED) {
            isInstalled = context.isInstalled()
        }

        return isInstalled
    }

    open fun Context.isInstalled(): Boolean {
        val pkg = createIntents("https://").firstOrNull()?.`package` ?: return false

        return try {
            packageManager.getApplicationInfo(pkg, 0)
            true
        } catch (e: Exception) {
            false
        }
    }
}

/**
 * The base class for adding an app group. Even if there's only one variant of the app to add,
 * this should be used.
 */
abstract class LaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    val autoAdd: Boolean = true,
    val enabled: Boolean = true,
) : BaseLaunchStrategy(labelRes) {
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
