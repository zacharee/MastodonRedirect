@file:Suppress("unused")

package dev.zwander.lemmyredirect.util

import android.content.ComponentName
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
) : LaunchStrategy(key, labelRes)

sealed class LemmyLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd)

@Keep
data object Jerboa : LemmyLaunchStrategyRootGroup(R.string.jerboa) {
    @Keep
    data object JerboaStable : LemmyLaunchStrategy("JERBOA", dev.zwander.shared.R.string.main) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    "com.jerboa",
                    ComponentName(
                        "com.jerboa",
                        "com.jerboa.MainActivity",
                    ),
                    url,
                ),
            )
        }
    }
}

@Keep
data object Summit : LemmyLaunchStrategyRootGroup(R.string.summit) {
    @Keep
    data object SummitStable : LemmyLaunchStrategy("SUMMIT", dev.zwander.shared.R.string.main) {
        override fun Context.createIntents(url: String): List<Intent> {
            val intent = LaunchStrategyUtils.createViewIntent(
                "com.idunnololz.summit",
                ComponentName(
                    "com.idunnololz.summit",
                    "com.idunnololz.summit.main.MainActivity",
                ),
                url,
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
    data object LiftoffStable : LemmyLaunchStrategy("LIFTOFF", dev.zwander.shared.R.string.main) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    "com.liftoffapp.liftoff",
                    ComponentName(
                        "com.liftoffapp.liftoff",
                        "com.liftoffapp.liftoff.MainActivity",
                    ),
                    url.replace("https://", "liftoff://")
                        .replace("http://", "liftoff://"),
                ),
            )
        }
    }
}

@Keep
data object Sync : LemmyLaunchStrategyRootGroup(R.string.sync) {
    @Keep
    data object SyncMain : LemmyLaunchStrategy("SYNC", dev.zwander.shared.R.string.main) {
        private fun createIntent(url: String, component: String): Intent {
            return Intent().apply {
                this.action = null
                addCategory(Intent.CATEGORY_LAUNCHER)
                putExtra("link", url)

                `package` = "io.syncapps.lemmy_sync"
                this.component = ComponentName(
                    "io.syncapps.lemmy_sync",
                    "io.syncapps.lemmy_sync.ui.activities.HomeActivity.$component",
                )
            }
        }

        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                "Default",
                "Blm",
                "Pride",
            ).map { createIntent(url, it) }
        }
    }
}

@Keep
data object Infinity : LemmyLaunchStrategyRootGroup(R.string.infinity) {
    @Keep
    data object InfinityMain : LemmyLaunchStrategy("INFINITY", dev.zwander.shared.R.string.main) {
        override fun Context.createIntents(url: String): List<Intent> {
            return listOf(
                LaunchStrategyUtils.createViewIntent(
                    "eu.toldi.infinityforlemmy",
                    ComponentName(
                        "eu.toldi.infinityforlemmy",
                        "eu.toldi.infinityforlemmy.activities.LinkResolverActivity",
                    ),
                    url,
                ),
            )
        }
    }
}
