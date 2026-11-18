package dev.zwander.mastodonredirect

import dev.zwander.mastodonredirect.util.LaunchStrategyUtils
import dev.zwander.mastodonredirect.util.Megalodon
import dev.zwander.shared.App

class MainApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    defaultLaunchStrategy = Megalodon.MegalodonStable,
    appNameRes = R.string.app_name,
)
