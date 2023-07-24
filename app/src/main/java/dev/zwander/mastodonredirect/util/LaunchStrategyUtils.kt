package dev.zwander.mastodonredirect.util

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

object LaunchStrategyUtils {
    const val LAUNCH_ACTION = "dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"

    internal val groupedLaunchStrategies = LaunchStrategyRootGroup::class.sealedSubclasses
        .mapNotNull { it.objectInstance }.filter { it.autoAdd }.also {
            Log.e("MastodonRedirect", "$it")
        }

    private val flattenedLaunchStrategies = groupedLaunchStrategies.flatMap { strategy ->
        strategy.children.map { it.key to it }.also {
            Log.e("MastodonRedirect", "children $it")
        }
    }.toMap()

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

    fun Context.getLaunchStrategyForKey(key: String): LaunchStrategy? {
        return flattenedLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
    }

    private fun Context.discoverStrategies(): Map<String, LaunchStrategy> {
        @Suppress("DEPRECATION")
        return packageManager.queryIntentActivities(
            Intent(LAUNCH_ACTION),
            0
        ).groupBy { it.resolvePackageName }
            .map { (pkg, infos) ->
                pkg to DiscoveredLaunchStrategy(
                    packageName = pkg,
                    components = infos.map { it.componentInfo.componentName },
                    labelRes = infos.first().componentInfo.applicationInfo.labelRes,
                )
            }
            .toMap()
    }

    private fun Context.getLaunchStrategyForPackage(pkg: String): LaunchStrategy? {
        return try {
            @Suppress("DEPRECATION")
            val infos = packageManager.queryIntentActivities(
                Intent(LAUNCH_ACTION).apply {
                    `package` = pkg
                },
                0
            ).ifEmpty { return null }

            DiscoveredLaunchStrategy(
                packageName = pkg,
                components = infos.map { it.componentInfo.componentName },
                labelRes = infos.first().componentInfo.applicationInfo.labelRes,
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
