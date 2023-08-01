@file:Suppress("unused")

package dev.zwander.lemmyredirect.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
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
                Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addCategory(Intent.CATEGORY_BROWSABLE)

                    `package` = "com.jerboa"
                    component = ComponentName("com.jerboa", "com.jerboa.MainActivity")
                },
            )
        }
    }
}

@Keep
data object Summit : LemmyLaunchStrategyRootGroup(R.string.summit) {
    @Keep
    data object SummitStable : LemmyLaunchStrategy("SUMMIT", dev.zwander.shared.R.string.main) {
        override fun Context.createIntents(url: String): List<Intent> {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addCategory(Intent.CATEGORY_DEFAULT)
                addCategory(Intent.CATEGORY_BROWSABLE)

                `package` = "com.idunnololz.summit"
                component = ComponentName("com.idunnololz.summit", "com.idunnololz.summit.main.MainActivity")
            }

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
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(url.replace("https://", "liftoff://")
                        .replace("http://", "liftoff://"))
                ).apply {
                    addCategory(Intent.CATEGORY_DEFAULT)
                    addCategory(Intent.CATEGORY_BROWSABLE)

                    `package` = "com.liftoffapp.liftoff"
                    component = ComponentName("com.liftoffapp.liftoff", "com.liftoffapp.liftoff.MainActivity")
                },
            )
        }
    }
}
