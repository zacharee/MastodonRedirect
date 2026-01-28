@file:Suppress("unused")

package io.github.turtlepaw.blueskyredirect.util

import android.content.Context
import androidx.annotation.Keep
import androidx.annotation.StringRes
import dev.zwander.shared.LaunchIntentCreator
import dev.zwander.shared.LaunchStrategy
import io.github.turtlepaw.blueskyredirect.R

/**
 * This file contains the supported launch strategies.
 *
 * To add your own strategy, create a new data object extending [BlueskyLaunchStrategyRootGroup].
 * Then, create a nested data object extending [BlueskyClientLaunchStrategy].
 * Examples for groups with only one strategy and with multiple are available below.
 * Newly added strategies will be automatically included in the UI.
 */

sealed class BlueskyClientLaunchStrategy(
    key: String,
    @StringRes labelRes: Int,
    override val sourceUrl: String?,
    intentCreator: LaunchIntentCreator,
) : LaunchStrategy(key, labelRes, intentCreator = intentCreator)

@Keep
data object BlueskySocialApp : BlueskyClientLaunchStrategy(
    "BLUESKY_SOCIAL_APP",
    R.string.bsky_social_app,
    "https://github.com/bluesky-social/social-app/",
    LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
        pkg = "xyz.blueskyweb.app",
        component = "xyz.blueskyweb.app.MainActivity",
    ),
)

@Keep
data object CatskyAndroid : BlueskyClientLaunchStrategy(
    "CATSKY_ANDROID",
    R.string.catsky,
    "https://github.com/NekoDrone/catsky-social/",
    LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
        pkg = "social.catsky",
        component = "social.catsky.MainActivity",
    ),
)

@Keep
data object DeerAndroid : BlueskyClientLaunchStrategy(
    "DEER_ANDROID",
    R.string.deer,
    "https://github.com/a-viv-a/deer-social",
    LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
        pkg = "social.deer",
        component = "social.catsky.MainActivity",
    ),
)

@Keep
data object DeerAyla : BlueskyClientLaunchStrategy(
    "DEER_AYLA_ANDROID",
    R.string.deer_ayla,
    "https://github.com/ayla6/deer-social-test",
    LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
        pkg = "top.aylac.deer",
        component = "top.aylac.deer.MainActivity",
    ),
)

@Keep
data object Witchsky : BlueskyClientLaunchStrategy(
    "WITCHSKY_APP",
    R.string.witchsky,
    "https://tangled.org/jollywhoppers.com/witchsky.app",
    LaunchIntentCreator.ComponentIntentCreator.ViewIntentCreator(
        pkg = "app.witchsky",
        component = "app.witchsky.MainActivity",
    ),
)

@Keep
data object AskEveryTime : BlueskyClientLaunchStrategy(
    "ASK_EVERY_TIME",
    dev.zwander.shared.R.string.ask_every_time,
    null,
    LaunchIntentCreator.ComponentIntentCreator.AskEveryTimeIntentCreator()
) {
    override fun Context.isInstalled(): Boolean {
        return true
    }
}