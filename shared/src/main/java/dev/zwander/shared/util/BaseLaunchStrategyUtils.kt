package dev.zwander.shared.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.shared.DiscoveredGroup
import dev.zwander.shared.DiscoveredLaunchStrategy
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup
import kotlin.reflect.KClass
import androidx.core.net.toUri

abstract class BaseLaunchStrategyUtils(
    applicationId: String,
    baseGroupClass : KClass<out LaunchStrategyRootGroup>,
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
        ).groupBy { it.activityInfo.packageName }
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

    open fun createViewIntent(
        pkg: String,
        componentPkg: String = pkg,
        component: String,
        url: String,
        scheme: String = "https",
        newTask: Boolean = true,
    ): Intent {
        val originalUri = url.toUri()
        val newUri = url.replace("${originalUri.scheme}://", "${scheme}://").toUri()

        return Intent(Intent.ACTION_VIEW, newUri).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            addCategory(Intent.CATEGORY_BROWSABLE)
            if (newTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            `package` = pkg
            this.component = ComponentName(
                componentPkg, component
            )
        }
    }

    open fun createShareIntent(
        pkg: String,
        componentPkg: String = pkg,
        component: String,
        url: String,
        type: String = "*/*",
        newTask: Boolean = true,
    ): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            addCategory(Intent.CATEGORY_DEFAULT)
            if (newTask) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            putExtra(Intent.EXTRA_TEXT, url)
            this.type = type

            `package` = pkg
            this.component = ComponentName(
                componentPkg, component
            )
        }
    }

    private fun Context.createDiscoveredLaunchStrategy(pkg: String, infos: List<ResolveInfo>): DiscoveredLaunchStrategy? {
        if (infos.isEmpty()) {
            return null
        }

        return DiscoveredLaunchStrategy(
            packageName = pkg,
            components = infos.map { it.activityInfo.componentNameCompat },
            launchAction = launchAction,
            _label = infos.first().activityInfo.applicationInfo
                .loadLabel(packageManager).toString(),
        )
    }
}