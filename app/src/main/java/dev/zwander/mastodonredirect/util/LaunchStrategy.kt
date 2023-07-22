package dev.zwander.mastodonredirect.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import dev.zwander.mastodonredirect.R

/**
 * This file contains the supported launch strategies and related utilities.
 *
 * To add your own strategy, create a new data object extending [LaunchStrategyGroup].
 * Then, create a nested data object extension [LaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 */

const val LAUNCH_ACTION = "dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"

/**
 * If you create a new [LaunchStrategyGroup], add it to this list.
 */
private val groupedLaunchStrategies = listOf(
    Megalodon,
    SubwayTooter,
    Tooot,
    Elk,
    Moshidon,
    Fedilab,
)

private val flattenedLaunchStrategies = groupedLaunchStrategies.flatMap { strategy ->
    strategy.children.map { it.key to it }
}.toMap()

@Composable
fun rememberSortedLaunchStrategies(): List<LaunchStrategyGroup> {
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

fun Context.discoverStrategies(): Map<String, LaunchStrategy> {
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

fun Context.getLaunchStrategyForKey(key: String): LaunchStrategy? {
    return flattenedLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
}

fun Context.getLaunchStrategyForPackage(pkg: String): LaunchStrategy? {
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

sealed class BaseLaunchStrategy(
    @StringRes open val labelRes: Int,
) {
    open val Context.label: String
        get() = resources.getString(labelRes)
}

sealed class LaunchStrategy(
    val key: String,
    @StringRes labelRes: Int,
) : BaseLaunchStrategy(labelRes) {
    abstract fun Context.createIntents(url: String): List<Intent>
}

sealed class LaunchStrategyGroup(@StringRes labelRes: Int) : BaseLaunchStrategy(labelRes) {
    abstract val children: List<LaunchStrategy>
}

data object Megalodon : LaunchStrategyGroup(R.string.megalodon) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            MegalodonStable,
        )

    data object MegalodonStable : LaunchStrategy("MEGALODON", R.string.stable) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, url)
                    `package` = "org.joinmastodon.android.sk"
                    component = ComponentName(
                        "org.joinmastodon.android.sk",
                        "org.joinmastodon.android.ExternalShareActivity"
                    )
                },
            )
        }
    }
}

data object SubwayTooter : LaunchStrategyGroup(R.string.subway_tooter) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            SubwayTooterStable,
        )

    data object SubwayTooterStable : LaunchStrategy("SUBWAY_TOOTER", R.string.stable) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW).apply {
                    `package` = "jp.juggler.subwaytooter"
                    component = ComponentName(
                        "jp.juggler.subwaytooter",
                        "jp.juggler.subwaytooter.ActCallback"
                    )
                    data = Uri.parse(url)
                },
            )
        }
    }
}

data object Tooot : LaunchStrategyGroup(R.string.tooot) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            ToootStable,
        )

    data object ToootStable : LaunchStrategy("TOOOT", R.string.stable) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    `package` = "com.xmflsct.app.tooot"
                    component =
                        ComponentName("com.xmflsct.app.tooot", "com.xmflsct.app.tooot.MainActivity")
                }
            )
        }
    }
}

data object Fedilab : LaunchStrategyGroup(R.string.fedilab) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            FedilabBase.FedilabGoogle,
            FedilabBase.FedilabFDroid,
        )

    sealed class FedilabBase(
        key: String,
        @StringRes labelRes: Int,
        private val pkg: String,
        private val componentName: String,
    ) : LaunchStrategy(key, labelRes) {
        override fun Context.createIntents(url: String): List<Intent> {
            val baseIntent = Intent(Intent.ACTION_VIEW)
            baseIntent.data = Uri.parse(url)

            return listOf(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse(url)
                    `package` = pkg
                    component = ComponentName(pkg, componentName)
                },
            )
        }

        data object FedilabGoogle : FedilabBase(
            "FEDILAB",
            R.string.google_play,
            "app.fedilab.android",
            "app.fedilab.android.activities.MainActivity",
        )

        data object FedilabFDroid : FedilabBase(
            "FEDILAB_FDROID",
            R.string.f_droid,
            "fr.gouv.etalab.mastodon",
            "app.fedilab.android.activities.MainActivity",
        )
    }
}

data object Moshidon : LaunchStrategyGroup(R.string.moshidon) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            MoshidonBase.MoshidonStable,
            MoshidonBase.MoshidonNightly,
        )

    sealed class MoshidonBase(
        key: String,
        @StringRes labelRes: Int,
        private val pkg: String,
        private val componentName: String,
    ) : LaunchStrategy(key, labelRes) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, url)
                    `package` = pkg
                    component = ComponentName(pkg, componentName)
                },
            )
        }

        data object MoshidonStable : MoshidonBase(
            "MOSHIDON",
            R.string.stable,
            "org.joinmastodon.android.moshinda",
            "org.joinmastodon.android.ExternalShareActivity",
        )

        data object MoshidonNightly : MoshidonBase(
            "MOSHIDON_NIGHTLY",
            R.string.nightly,
            "org.joinmastodon.android.moshinda.nightly",
            "org.joinmastodon.android.ExternalShareActivity",
        )
    }
}

data object Elk : LaunchStrategyGroup(R.string.elk) {
    override val children: List<LaunchStrategy>
        get() = listOf(
            ElkBase.ElkStable,
            ElkBase.ElkCanary,
        )

    sealed class ElkBase(
        key: String,
        @StringRes labelRes: Int,
        private val baseUrl: String,
    ) : LaunchStrategy(key, labelRes) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse("$baseUrl/$url")),
            )
        }

        data object ElkStable : ElkBase("ELK", R.string.stable, "https://elk.zone")
        data object ElkCanary : ElkBase("ELK_CANARY", R.string.canary, "https://main.elk.zone")
    }
}

data class DiscoveredGroup(
    override val children: List<LaunchStrategy>,
) : LaunchStrategyGroup(R.string.discovered)

data class DiscoveredLaunchStrategy(
    val packageName: String,
    val components: List<ComponentName>,
    @StringRes override val labelRes: Int,
) : LaunchStrategy(packageName, labelRes) {
    override val Context.label: String
        get() = packageManager.getResourcesForApplication(packageName).getString(labelRes)

    override fun Context.createIntents(url: String): List<Intent> {
        return components.map { cmp ->
            Intent(LAUNCH_ACTION).apply {
                `package` = packageName
                component = cmp
                data = Uri.parse(url)
            }
        }
    }
}
