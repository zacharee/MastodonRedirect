package dev.zwander.shared.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.shared.DiscoveredGroup
import dev.zwander.shared.DiscoveredLaunchStrategy
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup
import kotlin.reflect.KClass

abstract class BaseLaunchStrategyUtils(
    applicationId: String,
    private val baseGroupClass : KClass<out LaunchStrategyRootGroup>,
    val defaultLaunchStrategy: LaunchStrategy,
) {
    protected open val launchAction = "$applicationId.intent.action.OPEN_FEDI_LINK"

    protected open val groupedLaunchStrategies by lazy {
        baseGroupClass.sealedSubclasses
            .mapNotNull { it.objectInstance }.filter { it.autoAdd }
    }

    protected open val flattenedLaunchStrategies by lazy {
        groupedLaunchStrategies.flatMap { strategy ->
            strategy.children.map { it.key to it }
        }.toMap()
    }

    @Composable
    fun rememberSortedLaunchStrategies(): List<LaunchStrategyRootGroup> {
        val context = LocalContext.current

        return remember {
            val sortedPredefined = groupedLaunchStrategies.sortedBy {
                with(it) { context.label }.lowercase()
            }

            val discoveredValues = context.discoverStrategies().values

            if (discoveredValues.isEmpty()) {
                sortedPredefined
            } else {
                sortedPredefined + DiscoveredGroup(
                    discoveredValues.sortedBy {
                        with(it) { context.label }.lowercase()
                    }
                )
            }
        }
    }

    open fun Context.getLaunchStrategyForKey(key: String?): LaunchStrategy? {
        if (key == null) {
            return null
        }

        return flattenedLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
    }

    open fun Context.discoverStrategies(): Map<String, LaunchStrategy> {
        return packageManager.queryIntentActivities(
            Intent(launchAction),
            PackageManager.MATCH_ALL,
        ).groupBy { it.resolvePackageName }
            .mapNotNull { (pkg, infos) ->
                val strategy = createDiscoveredLaunchStrategy(pkg, infos)

                strategy?.let { pkg to strategy }
            }
            .toMap()
    }

    open fun Context.getLaunchStrategyForPackage(pkg: String?): LaunchStrategy? {
        if (pkg == null) {
            return null
        }

        return try {
            val infos = packageManager.queryIntentActivities(
                Intent(launchAction).apply {
                    `package` = pkg
                },
                PackageManager.MATCH_ALL,
            ).ifEmpty { return null }

            createDiscoveredLaunchStrategy(pkg, infos)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    open fun createViewIntent(pkg: String, component: ComponentName, url: String): Intent {
        return Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)

            `package` = pkg
            this.component = component
        }
    }

    open fun createShareIntent(pkg: String, component: ComponentName, url: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, url)

            `package` = pkg
            this.component = component
        }
    }

    private fun createDiscoveredLaunchStrategy(pkg: String, infos: List<ResolveInfo>): DiscoveredLaunchStrategy? {
        if (infos.isEmpty()) {
            return null
        }

        return DiscoveredLaunchStrategy(
            packageName = pkg,
            components = infos.map { it.activityInfo.componentNameCompat },
            labelRes = infos.first().activityInfo.applicationInfo.labelRes,
            launchAction = launchAction,
        )
    }
}