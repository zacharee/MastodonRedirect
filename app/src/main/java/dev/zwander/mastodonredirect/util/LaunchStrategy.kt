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

const val LAUNCH_ACTION = "dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"

private val manualLaunchStrategies = mapOf(
    Megalodon.key to Megalodon,
    Fedilab.key to Fedilab,
    SubwayTooter.key to SubwayTooter,
    Moshidon.key to Moshidon,
    Elk.key to Elk,
    Tooot.key to Tooot,
)

fun Context.getAllLaunchStrategies(): Map<String, LaunchStrategy> {
    return manualLaunchStrategies + discoverStrategies()
}

@Composable
fun rememberSortedLaunchStrategies(): List<LaunchStrategy> {
    val context = LocalContext.current

    return remember {
        (manualLaunchStrategies.values + context.discoverStrategies().values).sortedBy {
            with(it) { context.label }.lowercase()
        }
    }
}

fun Context.discoverStrategies(): Map<String, LaunchStrategy> {
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
    return manualLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
}

fun Context.getLaunchStrategyForPackage(pkg: String): LaunchStrategy? {
    return try {
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

sealed class LaunchStrategy(
    val key: String,
    @StringRes open val labelRes: Int,
) {
    open val Context.label: String
        get() = resources.getString(labelRes)

    abstract fun Context.createIntents(url: String): List<Intent>
}

data object Megalodon : LaunchStrategy("MEGALODON", R.string.megalodon) {
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

data object Fedilab : LaunchStrategy("FEDILAB", R.string.fedilab) {
    override fun Context.createIntents(url: String): List<Intent> {
        val baseIntent = Intent(Intent.ACTION_VIEW)
        baseIntent.data = Uri.parse(url)

        return listOf(
            Intent(baseIntent).apply {
                `package` = "app.fedilab.android"
                component = ComponentName(
                    "app.fedilab.android",
                    "app.fedilab.android.activities.MainActivity"
                )

            },
            Intent(baseIntent).apply {
                `package` = "fr.gouv.etalab.mastodon"
                component = ComponentName(
                    "fr.gouv.etalab.mastodon",
                    "app.fedilab.android.activities.MainActivity"
                )
            }
        )
    }
}

data object SubwayTooter : LaunchStrategy("SUBWAY_TOOTER", R.string.subway_tooter) {
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

data object Moshidon : LaunchStrategy("MOSHIDON", R.string.moshidon) {
    override fun Context.createIntents(url: String): List<Intent> {
        return listOf(
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, url)
                `package` = "org.joinmastodon.android.moshinda"
                component = ComponentName(
                    "org.joinmastodon.android.moshinda",
                    "org.joinmastodon.android.ExternalShareActivity"
                )
            },
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_TEXT, url)
                `package` = "org.joinmastodon.android.moshinda.nightly"
                component = ComponentName(
                    "org.joinmastodon.android.moshinda.nightly",
                    "org.joinmastodon.android.ExternalShareActivity"
                )
            },
        )
    }
}

data object Elk : LaunchStrategy("ELK", R.string.elk) {
    override fun Context.createIntents(url: String): List<Intent> {
        return listOf(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://elk.zone/$url")),
        )
    }
}

data object Tooot : LaunchStrategy("TOOOT", R.string.tooot) {
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

data class DiscoveredLaunchStrategy(
    val packageName: String,
    val components: List<ComponentName>,
    override val labelRes: Int,
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
