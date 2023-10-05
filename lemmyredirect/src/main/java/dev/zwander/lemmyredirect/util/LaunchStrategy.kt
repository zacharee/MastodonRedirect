@file:Suppress("unused")

package dev.zwander.lemmyredirect.util

import android.content.Context
import android.content.Intent
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.lemmyredirect.R
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.LaunchStrategyRootGroup

/**
 * This file contains the supported launch strategies.
 *
 * To add your own strategy, create a new data object extending [LemmyLaunchStrategyRootGroup].
 * Then, create a nested data object extending [LemmyLaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 * Newly added strategies will be automatically included in the UI.
 */

sealed class LemmyLaunchStrategy(
    key: String,
    @StringRes labelRes: Int,
    override val sourceUrl: String?,
) : LaunchStrategy(key, labelRes)

sealed class LemmyLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd)

@Keep
data object Jerboa : LemmyLaunchStrategyRootGroup(R.string.jerboa) {
    @Keep
    data object JerboaStable : LemmyLaunchStrategy("JERBOA", dev.zwander.shared.R.string.main, "https://github.com/dessalines/jerboa") {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    pkg = "com.jerboa",
                    component = "com.jerboa.MainActivity",
                    url = url,
                ),
            )
        }
    }
}

@Keep
data object Summit : LemmyLaunchStrategyRootGroup(R.string.summit) {
    @Keep
    data object SummitStable : LemmyLaunchStrategy("SUMMIT", dev.zwander.shared.R.string.main, "https://play.google.com/store/apps/details?id=com.idunnololz.summit") {
        override fun Context.createIntents(url: String): List<Intent> {
            val intent = LaunchStrategyUtils.createViewIntent(
                pkg = "com.idunnololz.summit",
                component = "com.idunnololz.summit.main.MainActivity",
                url = url,
            )

            // Summit currently doesn't follow deep links on a cold start,
            // so send the Intent twice.
            return listOf(
                Intent(intent),
                Intent(intent),
            )
        }
    }
}

@Keep
data object Liftoff : LemmyLaunchStrategyRootGroup(R.string.liftoff) {
    @Keep
    data object LiftoffStable : LemmyLaunchStrategy("LIFTOFF", dev.zwander.shared.R.string.main, "https://github.com/liftoff-app/liftoff") {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    pkg = "com.liftoffapp.liftoff",
                    component = "com.liftoffapp.liftoff.MainActivity",
                    url = url.replace("https://", "liftoff://")
                        .replace("http://", "liftoff://"),
                ),
            )
        }
    }
}

@Keep
data object Sync : LemmyLaunchStrategyRootGroup(R.string.sync) {
    @Keep
    data object SyncMain : LemmyLaunchStrategy("SYNC", dev.zwander.shared.R.string.main, "https://play.google.com/store/apps/details?id=io.syncapps.lemmy_sync") {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    pkg = "io.syncapps.lemmy_sync",
                    component = "io.syncapps.lemmy_sync.ui.activities.IntentActivity",
                    url = url,
                ),
            )
        }
    }
}

@Keep
data object Infinity : LemmyLaunchStrategyRootGroup(R.string.infinity) {
    @Keep
    data object InfinityMain : LemmyLaunchStrategy("INFINITY", dev.zwander.shared.R.string.main, "https://codeberg.org/Bazsalanszky/Eternity") {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    pkg = "eu.toldi.infinityforlemmy",
                    component = "eu.toldi.infinityforlemmy.activities.LinkResolverActivity",
                    url = url,
                ),
            )
        }
    }
}

@Keep
data object Boost : LemmyLaunchStrategyRootGroup(R.string.boost) {
    @Keep
    data object BoostMain : LemmyLaunchStrategy("BOOST", dev.zwander.shared.R.string.main, "https://play.google.com/store/apps/details?id=com.rubenmayayo.lemmy") {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    pkg = "com.rubenmayayo.lemmy",
                    component = "com.rubenmayayo.reddit.ui.activities.DeepLinkingActivity",
                    url = url,
                ),
            )
        }
    }
}
