@file:Suppress("unused")

package dev.zwander.mastodonredirect.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.mastodonredirect.R
import dev.zwander.mastodonredirect.util.LaunchStrategyUtils.LAUNCH_ACTION
import kotlin.reflect.KClass

/**
 * This file contains the supported launch strategies.
 *
 * To add your own strategy, create a new data object extending [LaunchStrategyRootGroup].
 * Then, create a nested data object extension [LaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 * Newly added strategies will be automatically included in the UI.
 */

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

/**
 * The base class for adding an app group. Even if there's only one variant of the app to add,
 * this should be used.
 */
sealed class LaunchStrategyRootGroup(@StringRes labelRes: Int, val autoAdd: Boolean = true) : BaseLaunchStrategy(labelRes) {
    open val children: List<LaunchStrategy> by lazy { processNestedClasses(this::class) }

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

@Keep
data object Megalodon : LaunchStrategyRootGroup(R.string.megalodon) {
    @Keep
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

@Keep
data object SubwayTooter : LaunchStrategyRootGroup(R.string.subway_tooter) {
    @Keep
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

@Keep
data object Tooot : LaunchStrategyRootGroup(R.string.tooot) {
    @Keep
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

@Keep
data object Fedilab : LaunchStrategyRootGroup(R.string.fedilab) {
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

        @Keep
        data object FedilabGoogle : FedilabBase(
            "FEDILAB",
            R.string.google_play,
            "app.fedilab.android",
            "app.fedilab.android.activities.MainActivity",
        )

        @Keep
        data object FedilabFDroid : FedilabBase(
            "FEDILAB_FDROID",
            R.string.f_droid,
            "fr.gouv.etalab.mastodon",
            "app.fedilab.android.activities.MainActivity",
        )
    }
}

@Keep
data object Moshidon : LaunchStrategyRootGroup(R.string.moshidon) {
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

        @Keep
        data object MoshidonStable : MoshidonBase(
            "MOSHIDON",
            R.string.stable,
            "org.joinmastodon.android.moshinda",
            "org.joinmastodon.android.ExternalShareActivity",
        )

        @Keep
        data object MoshidonNightly : MoshidonBase(
            "MOSHIDON_NIGHTLY",
            R.string.nightly,
            "org.joinmastodon.android.moshinda.nightly",
            "org.joinmastodon.android.ExternalShareActivity",
        )
    }
}

@Keep
data object Elk : LaunchStrategyRootGroup(R.string.elk) {
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

        @Keep
        data object ElkStable : ElkBase("ELK", R.string.stable, "https://elk.zone")
        @Keep
        data object ElkCanary : ElkBase("ELK_CANARY", R.string.canary, "https://main.elk.zone")
    }
}

data class DiscoveredGroup(
    override val children: List<LaunchStrategy>,
) : LaunchStrategyRootGroup(labelRes = R.string.discovered, autoAdd = false)

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
