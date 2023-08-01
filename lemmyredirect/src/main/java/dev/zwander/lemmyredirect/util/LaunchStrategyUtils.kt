package dev.zwander.lemmyredirect.util

import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    launchAction = "dev.zwander.lemmyredirect.intent.action.OPEN_FEDI_LINK",
    baseGroupClass = LemmyLaunchStrategyRootGroup::class,
)
