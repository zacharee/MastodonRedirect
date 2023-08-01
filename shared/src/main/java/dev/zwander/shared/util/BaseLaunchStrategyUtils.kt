package dev.zwander.shared.util

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.shared.DiscoveredGroup
import dev.zwander.shared.DiscoveredLaunchStrategy
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup
import kotlin.reflect.KClass

abstract class BaseLaunchStrategyUtils(
    val launchAction: String,
    private val baseGroupClass : KClass<out LaunchStrategyRootGroup>,
) {
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
            (groupedLaunchStrategies).sortedBy {
                with(it) { context.label }.lowercase()
            } + context.discoverStrategies().values.let { discoveredStrategies ->
                if (discoveredStrategies.isNotEmpty()) {
                    listOf(
                        DiscoveredGroup(
                            discoveredStrategies.sortedBy {
                                with(it) { context.label }.lowercase()
                            }
                        ),
                    )
                } else {
                    listOf()
                }
            }
        }
    }

    open fun Context.getLaunchStrategyForKey(key: String): LaunchStrategy? {
        return flattenedLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
    }

    open fun Context.discoverStrategies(): Map<String, LaunchStrategy> {
        @Suppress("DEPRECATION")
        return packageManager.queryIntentActivities(
            Intent(launchAction),
            0
        ).groupBy { it.resolvePackageName }
            .map { (pkg, infos) ->
                pkg to DiscoveredLaunchStrategy(
                    packageName = pkg,
                    components = infos.map { it.componentInfo.componentName },
                    labelRes = infos.first().componentInfo.applicationInfo.labelRes,
                    launchAction = launchAction,
                )
            }
            .toMap()
    }

    open fun Context.getLaunchStrategyForPackage(pkg: String): LaunchStrategy? {
        return try {
            @Suppress("DEPRECATION")
            val infos = packageManager.queryIntentActivities(
                Intent(launchAction).apply {
                    `package` = pkg
                },
                0
            ).ifEmpty { return null }

            DiscoveredLaunchStrategy(
                packageName = pkg,
                components = infos.map { it.componentInfo.componentName },
                labelRes = infos.first().componentInfo.applicationInfo.labelRes,
                launchAction = launchAction,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}