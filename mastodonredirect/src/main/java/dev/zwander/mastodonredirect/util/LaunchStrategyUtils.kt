package dev.zwander.mastodonredirect.util

import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    launchAction = "dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK",
    baseGroupClass = MastodonLaunchStrategyRootGroup::class,
)
