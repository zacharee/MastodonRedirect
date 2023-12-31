@file:Suppress("unused")

package dev.zwander.mastodonredirect.util

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.mastodonredirect.R
import dev.zwander.shared.LaunchIntentCreator
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
    override val sourceUrl: String?,
    intentCreator: LaunchIntentCreator,
) : LaunchStrategy(key, labelRes, intentCreator = intentCreator)

sealed class MastodonLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
    enabled: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd, enabled)

@Keep
data object Megalodon : MastodonLaunchStrategyRootGroup(R.string.megalodon) {
    @Keep
    data object MegalodonStable : MastodonLaunchStrategy(
        "MEGALODON",
        dev.zwander.shared.R.string.main,
        "https://github.com/sk22/megalodon",
        LaunchIntentCreator.ComponentIntentCreator.ShareIntentCreator(
            pkg = "org.joinmastodon.android.sk",
            component = "org.joinmastodon.android.ExternalShareActivity",
        ),
    )
}

@Keep
data object SubwayTooter : MastodonLaunchStrategyRootGroup(R.string.subway_tooter) {
    @Keep
    data object SubwayTooterPlay : MastodonLaunchStrategy(
        "SUBWAY_TOOTER",
        dev.zwander.shared.R.string.fcm,
        "https://github.com/tateisu/SubwayTooter",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "jp.juggler.subwaytooter",
            component = "jp.juggler.subwaytooter.ActCallback",
        ),
    )

    @Keep
    data object SubwayTooterFDroid : MastodonLaunchStrategy(
        "SUBWAY_TOOTER_FDROID",
        dev.zwander.shared.R.string.no_fcm,
        "https://github.com/tateisu/SubwayTooter",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "jp.juggler.subwaytooter.noFcm",
            component = "jp.juggler.subwaytooter.ActCallback",
        ),
    )
}

@Keep
data object Tooot : MastodonLaunchStrategyRootGroup(R.string.tooot) {
    @Keep
    data object ToootStable : MastodonLaunchStrategy(
        "TOOOT",
        dev.zwander.shared.R.string.main,
        "https://github.com/tooot-app/app",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.xmflsct.app.tooot",
            component = "com.xmflsct.app.tooot.MainActivity",
            scheme = "tooot",
        ),
    )
}

@Keep
data object Fedilab : MastodonLaunchStrategyRootGroup(R.string.fedilab) {
    sealed class FedilabBase(
        key: String,
        @StringRes labelRes: Int,
        pkg: String,
        componentName: String,
    ) : MastodonLaunchStrategy(
        key,
        labelRes,
        "https://codeberg.org/tom79/Fedilab",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = pkg,
            component = componentName,
        ),
    ) {
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
        pkg: String,
        componentName: String,
    ) : MastodonLaunchStrategy(
        key,
        labelRes,
        "https://github.com/LucasGGamerM/moshidon",
        LaunchIntentCreator.ComponentIntentCreator.ShareIntentCreator(
            pkg = pkg,
            component = componentName,
        ),
    ) {
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
        baseUrl: String,
    ) : MastodonLaunchStrategy(
        key,
        labelRes,
        baseUrl,
        LaunchIntentCreator.BaseUrlIntentCreator(baseUrl)
    ) {
        @Keep
        data object ElkStable :
            ElkBase("ELK", dev.zwander.shared.R.string.stable, "https://elk.zone/")

        @Keep
        data object ElkCanary :
            ElkBase("ELK_CANARY", dev.zwander.shared.R.string.canary, "https://main.elk.zone/")

        override fun Context.isInstalled(): Boolean {
            return true
        }
    }
}

@Keep
data object Mastodon : MastodonLaunchStrategyRootGroup(R.string.mastodon) {
    @Keep
    data object MastodonMain : MastodonLaunchStrategy(
        "MASTODON",
        dev.zwander.shared.R.string.main,
        "https://github.com/mastodon/mastodon-android",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "org.joinmastodon.android",
            component = "org.joinmastodon.android.MainActivity",
        ),
    )
}

@Keep
data object Trunks : MastodonLaunchStrategyRootGroup(R.string.trunks) {
    @Keep
    data object TrunksMain : MastodonLaunchStrategy(
        "TRUNKS",
        dev.zwander.shared.R.string.main,
        "https://mastodon.social/@trunksapp",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.decad3nce.trunks",
            component = "com.decad3nce.trunks.MainActivity",
            urlTransform = {
                "https://trunks.social/resolve?url=${it}"
            },
        ),
    )

    @Keep
    data object TrunksWeb : MastodonLaunchStrategy(
        "TRUNKS_WEB",
        dev.zwander.shared.R.string.web,
        "https://mastodon.social/@trunksapp",
        LaunchIntentCreator.BaseUrlIntentCreator("https://trunks.social/resolve?url="),
    ) {
        override fun Context.isInstalled(): Boolean {
            return true
        }
    }
}

@Keep
data object Phanpy : MastodonLaunchStrategyRootGroup(R.string.phanpy) {
    @Keep
    sealed class PhanpyBase(
        url: String,
        key: String,
        @StringRes
        labelRes: Int,
    ) : MastodonLaunchStrategy(
        "PHANPY_$key",
        labelRes,
        "https://hachyderm.io/@phanpy",
        LaunchIntentCreator.BaseUrlIntentCreator("$url/#/"),
    ) {
        @Keep
        data object PhanpyStable : PhanpyBase(
            "https://phanpy.social",
            "STABLE",
            dev.zwander.shared.R.string.stable,
        )

        @Keep
        data object PhanpyDev : PhanpyBase(
            "https://dev.phanpy.social",
            "DEV",
            dev.zwander.shared.R.string.dev,
        )

        override fun Context.isInstalled(): Boolean {
            return true
        }
    }
}
