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
    SubwayTooter.key to SubwayTooter,
    Elk.key to Elk,
    Tooot.key to Tooot,
    Fedilab.FedilabGoogle.key to Fedilab.FedilabGoogle,
    Fedilab.FedilabFDroid.key to Fedilab.FedilabFDroid,
    Moshidon.MoshidonStable.key to Moshidon.MoshidonStable,
    Moshidon.MoshidonNightly.key to Moshidon.MoshidonNightly,
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
    return manualLaunchStrategies[key] ?: getLaunchStrategyForPackage(key)
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

sealed class Fedilab(
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

    data object FedilabGoogle : Fedilab(
        "FEDILAB",
        R.string.fedilab_play,
        "app.fedilab.android",
        "app.fedilab.android.activities.MainActivity",
    )

    data object FedilabFDroid : Fedilab(
        "FEDILAB_FDROID",
        R.string.fedilab_fdroid,
        "fr.gouv.etalab.mastodon",
        "app.fedilab.android.activities.MainActivity",
    )
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

sealed class Moshidon(
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

    data object MoshidonStable : Moshidon(
        "MOSHIDON",
        R.string.moshidon,
        "org.joinmastodon.android.moshinda",
        "org.joinmastodon.android.ExternalShareActivity",
    )

    data object MoshidonNightly : Moshidon(
        "MOSHIDON_NIGHTLY",
        R.string.moshidon_nightly,
        "org.joinmastodon.android.moshinda.nightly",
        "org.joinmastodon.android.ExternalShareActivity",
    )
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
