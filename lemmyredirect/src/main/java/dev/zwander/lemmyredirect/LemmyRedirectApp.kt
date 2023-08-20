package dev.zwander.lemmyredirect

import dev.zwander.lemmyredirect.util.FetchInstancesActivity
import dev.zwander.lemmyredirect.util.LaunchStrategyUtils
import dev.zwander.shared.App

class LemmyRedirectApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    fetchActivity = FetchInstancesActivity::class.java,
)
