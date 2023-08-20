package dev.zwander.mastodonredirect

import dev.zwander.mastodonredirect.util.FetchInstancesActivity
import dev.zwander.mastodonredirect.util.LaunchStrategyUtils
import dev.zwander.shared.App

class MastodonRedirectApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    fetchActivity = FetchInstancesActivity::class.java,
)
