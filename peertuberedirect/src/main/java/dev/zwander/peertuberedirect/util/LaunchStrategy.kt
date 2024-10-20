@file:Suppress("unused")

package dev.zwander.peertuberedirect.util

import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.peertuberedirect.R
import dev.zwander.shared.LaunchIntentCreator
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup

/**
 * This file contains the supported launch strategies.
 *
 * To add your own strategy, create a new data object extending [PeerTubeLaunchStrategyRootGroup].
 * Then, create a nested data object extending [PeerTubeLaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 * Newly added strategies will be automatically included in the UI.
 */

sealed class PeerTubeLaunchStrategy(
    key: String,
    @StringRes labelRes: Int,
    override val sourceUrl: String?,
    intentCreator: LaunchIntentCreator,
) : LaunchStrategy(key, labelRes, intentCreator = intentCreator)

sealed class PeerTubeLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
    enabled: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd, enabled)

@Keep
data object NewPipe : PeerTubeLaunchStrategyRootGroup(R.string.newpipe) {
    sealed class Base(
        keySuffix: String,
        @StringRes labelRes: Int,
        pkg: String,
    ) : PeerTubeLaunchStrategy(
        "PEERTUBE_$keySuffix",
        labelRes,
        "https://github.com/TeamNewPipe/NewPipe",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = pkg,
            component = "$pkg.RouterActivity",
        ),
    ) {
        @Keep
        data object Release : Base("RELEASE", dev.zwander.shared.R.string.release, "org.schabi.newpipe")

        @Keep
        data object Debug : Base("DEBUG", dev.zwander.shared.R.string.debug, "org.schabi.newpipe.debug")
    }
}

@Keep
data object Tubular : PeerTubeLaunchStrategyRootGroup(R.string.tubular) {
    sealed class Base(
        keySuffix: String,
        @StringRes labelRes: Int,
        pkg: String,
    ) : PeerTubeLaunchStrategy(
        "TUBULAR_$keySuffix",
        labelRes,
        "https://github.com/polymorphicshade/Tubular",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = pkg,
            component = "$pkg.RouterActivity",
        ),
    ) {
        @Keep
        data object Release : Base("RELEASE", dev.zwander.shared.R.string.release, "org.polymorphicshade.tubular")

        @Keep
        data object Debug : Base("DEBUG", dev.zwander.shared.R.string.debug, "org.polymorphicshade.tubular.debug")
    }
}

@Keep
data object Fedilab : PeerTubeLaunchStrategyRootGroup(R.string.fedilab, enabled = false) {
    sealed class FedilabBase(
        key: String,
        @StringRes labelRes: Int,
        pkg: String,
        componentName: String,
    ) : PeerTubeLaunchStrategy(
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
data object Grayjay : PeerTubeLaunchStrategyRootGroup(R.string.grayjay, enabled = false) {
    sealed class Base(
        key: String,
        @StringRes labelRes: Int,
        pkg: String,
    ) : PeerTubeLaunchStrategy(
        "GRAYJAY_$key",
        labelRes,
        "https://grayjay.app",
        LaunchIntentCreator.ComponentIntentCreator.ShareIntentCreator(
            pkg = pkg,
            component = "$pkg.activities.MainActivity",
        ),
    ) {
        @Keep
        data object Stable : Base("STABLE", dev.zwander.shared.R.string.stable, "com.futo.platformplayer")

        @Keep
        data object Unstable : Base("UNSTABLE", dev.zwander.shared.R.string.unstable, "com.futo.platformplayer.d")

        @Keep
        data object PlayStore : Base("GOOGLE_PLAY", dev.zwander.shared.R.string.google_play, "com.futo.platformplayer.playstore")
    }
}
