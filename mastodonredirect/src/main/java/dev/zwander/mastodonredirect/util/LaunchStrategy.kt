@file:Suppress("unused")

package dev.zwander.mastodonredirect.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.mastodonredirect.R
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup

/**
 * This file contains the supported launch strategies.
 *
 * To add your own strategy, create a new data object extending [MastodonLaunchStrategyRootGroup].
 * Then, create a nested data object extending [MastodonLaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 * Newly added strategies will be automatically included in the UI.
 */

sealed class MastodonLaunchStrategy(
    key: String,
    @StringRes labelRes: Int,
) : LaunchStrategy(key, labelRes)

sealed class MastodonLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd)

@Keep
data object Megalodon : MastodonLaunchStrategyRootGroup(R.string.megalodon) {
    @Keep
    data object MegalodonStable :
        MastodonLaunchStrategy("MEGALODON", dev.zwander.shared.R.string.stable) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_SEND).apply {
                    putExtra(Intent.EXTRA_TEXT, url)
                    `package` = "org.joinmastodon.android.sk"
                    component = ComponentName(
                        "org.joinmastodon.android.sk",
                        "org.joinmastodon.android.ExternalShareActivity",
                    )
                },
            )
        }
    }
}

@Keep
data object SubwayTooter : MastodonLaunchStrategyRootGroup(R.string.subway_tooter) {
    @Keep
    data object SubwayTooterStable :
        MastodonLaunchStrategy("SUBWAY_TOOTER", dev.zwander.shared.R.string.stable) {
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
data object Tooot : MastodonLaunchStrategyRootGroup(R.string.tooot) {
    @Keep
    data object ToootStable : MastodonLaunchStrategy("TOOOT", dev.zwander.shared.R.string.stable) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    `package` = "com.xmflsct.app.tooot"
                    component = ComponentName(
                        "com.xmflsct.app.tooot",
                        "com.xmflsct.app.tooot.MainActivity",
                    )
                },
            )
        }
    }
}

@Keep
data object Fedilab : MastodonLaunchStrategyRootGroup(R.string.fedilab) {
    sealed class FedilabBase(
        key: String,
        @StringRes labelRes: Int,
        private val pkg: String,
        private val componentName: String,
    ) : MastodonLaunchStrategy(key, labelRes) {
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
            dev.zwander.shared.R.string.google_play,
            "app.fedilab.android",
            "app.fedilab.android.activities.MainActivity",
        )

        @Keep
        data object FedilabFDroid : FedilabBase(
            "FEDILAB_FDROID",
            dev.zwander.shared.R.string.f_droid,
            "fr.gouv.etalab.mastodon",
            "app.fedilab.android.activities.MainActivity",
        )
    }
}

@Keep
data object Moshidon : MastodonLaunchStrategyRootGroup(R.string.moshidon) {
    sealed class MoshidonBase(
        key: String,
        @StringRes labelRes: Int,
        private val pkg: String,
        private val componentName: String,
    ) : MastodonLaunchStrategy(key, labelRes) {
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
            dev.zwander.shared.R.string.stable,
            "org.joinmastodon.android.moshinda",
            "org.joinmastodon.android.ExternalShareActivity",
        )

        @Keep
        data object MoshidonNightly : MoshidonBase(
            "MOSHIDON_NIGHTLY",
            dev.zwander.shared.R.string.nightly,
            "org.joinmastodon.android.moshinda.nightly",
            "org.joinmastodon.android.ExternalShareActivity",
        )
    }
}

@Keep
data object Elk : MastodonLaunchStrategyRootGroup(R.string.elk) {
    sealed class ElkBase(
        key: String,
        @StringRes labelRes: Int,
        private val baseUrl: String,
    ) : MastodonLaunchStrategy(key, labelRes) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse("$baseUrl/$url")),
            )
        }

        @Keep
        data object ElkStable :
            ElkBase("ELK", dev.zwander.shared.R.string.stable, "https://elk.zone")

        @Keep
        data object ElkCanary :
            ElkBase("ELK_CANARY", dev.zwander.shared.R.string.canary, "https://main.elk.zone")
    }
}
