package dev.zwander.mastodonredirect

import dev.zwander.mastodonredirect.util.FetchInstancesActivity
import dev.zwander.mastodonredirect.util.LaunchStrategyUtils
import dev.zwander.mastodonredirect.util.Megalodon
import dev.zwander.shared.App

class MastodonRedirectApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    defaultLaunchStrategy = Megalodon.MegalodonStable,
) {
    override val fetchActivity by lazy { FetchInstancesActivity::class.java }
}
