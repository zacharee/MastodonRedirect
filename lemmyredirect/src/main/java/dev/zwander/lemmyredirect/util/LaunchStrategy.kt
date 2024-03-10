@file:Suppress("unused")

package dev.zwander.lemmyredirect.util

import android.content.Intent
import android.net.Uri
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.lemmyredirect.R
import dev.zwander.shared.LaunchIntentCreator
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
    intentCreator: LaunchIntentCreator,
) : LaunchStrategy(key, labelRes, intentCreator = intentCreator)

sealed class LemmyLaunchStrategyRootGroup(
    @StringRes labelRes: Int,
    autoAdd: Boolean = true,
    enabled: Boolean = true,
) : LaunchStrategyRootGroup(labelRes, autoAdd, enabled)

@Keep
data object Jerboa : LemmyLaunchStrategyRootGroup(R.string.jerboa) {
    @Keep
    data object JerboaStable : LemmyLaunchStrategy(
        "JERBOA",
        dev.zwander.shared.R.string.main,
        "https://github.com/dessalines/jerboa",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.jerboa",
            component = "com.jerboa.MainActivity",
        ),
    )
}

@Keep
data object Summit : LemmyLaunchStrategyRootGroup(R.string.summit) {
    @Keep
    data object SummitStable : LemmyLaunchStrategy(
        "SUMMIT",
        dev.zwander.shared.R.string.main,
        "https://play.google.com/store/apps/details?id=com.idunnololz.summit",
        LaunchIntentCreator.CustomIntentCreator { url ->
            val intent = LaunchStrategyUtils.createViewIntent(
                pkg = "com.idunnololz.summit",
                component = "com.idunnololz.summit.main.MainActivity",
                url = url,
                newTask = false,
            )

            // Summit currently doesn't follow deep links on a cold start,
            // so send the Intent twice.
            listOf(
                Intent(intent).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                Intent(intent),
            )
        },
    )
}

@Keep
data object Liftoff : LemmyLaunchStrategyRootGroup(R.string.liftoff) {
    @Keep
    data object LiftoffStable : LemmyLaunchStrategy(
        "LIFTOFF",
        dev.zwander.shared.R.string.main,
        "https://github.com/liftoff-app/liftoff",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.liftoffapp.liftoff",
            component = "com.liftoffapp.liftoff.MainActivity",
            scheme = "liftoff",
        ),
    )
}

@Keep
data object Sync : LemmyLaunchStrategyRootGroup(R.string.sync) {
    @Keep
    data object SyncMain : LemmyLaunchStrategy(
        "SYNC",
        dev.zwander.shared.R.string.main,
        "https://play.google.com/store/apps/details?id=io.syncapps.lemmy_sync",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "io.syncapps.lemmy_sync",
            component = "io.syncapps.lemmy_sync.ui.activities.IntentActivity",
        ),
    )
}

@Keep
data object Infinity : LemmyLaunchStrategyRootGroup(R.string.infinity) {
    @Keep
    data object InfinityMain : LemmyLaunchStrategy(
        "INFINITY",
        dev.zwander.shared.R.string.main,
        "https://codeberg.org/Bazsalanszky/Eternity",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "eu.toldi.infinityforlemmy",
            component = "eu.toldi.infinityforlemmy.activities.LinkResolverActivity",
        ),
    )
}

@Keep
data object Boost : LemmyLaunchStrategyRootGroup(R.string.boost) {
    @Keep
    data object BoostMain : LemmyLaunchStrategy(
        "BOOST",
        dev.zwander.shared.R.string.main,
        "https://play.google.com/store/apps/details?id=com.rubenmayayo.lemmy",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.rubenmayayo.lemmy",
            component = "com.rubenmayayo.reddit.ui.activities.DeepLinkingActivity",
        ),
    )
}

@Keep
data object Thunder : LemmyLaunchStrategyRootGroup(R.string.thunder) {
    @Keep
    data object ThunderMain : LemmyLaunchStrategy(
        "THUNDER",
        dev.zwander.shared.R.string.main,
        "https://github.com/thunder-app/thunder",
        LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
            pkg = "com.hjiangsu.thunder",
            component = "com.hjiangsu.thunder.MainActivity",
        ),
    )
}

@Keep
data object Voyager : LemmyLaunchStrategyRootGroup(R.string.voyager) {
    @Keep
    data object VoyagerMain : LemmyLaunchStrategy(
        "VOYAGER",
        dev.zwander.shared.R.string.main,
        "https://github.com/aeharding/voyager",
        LaunchIntentCreator.CustomIntentCreator { url ->
            listOf(
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    addCategory(Intent.CATEGORY_DEFAULT)

                    `package` = "app.vger.voyager"
                },
            )
        },
    )
}
