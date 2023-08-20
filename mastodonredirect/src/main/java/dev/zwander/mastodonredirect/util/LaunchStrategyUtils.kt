package dev.zwander.mastodonredirect.util

import dev.zwander.mastodonredirect.BuildConfig
import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    applicationId = BuildConfig.APPLICATION_ID,
    baseGroupClass = MastodonLaunchStrategyRootGroup::class,
    defaultLaunchStrategy = Megalodon.MegalodonStable,
)
