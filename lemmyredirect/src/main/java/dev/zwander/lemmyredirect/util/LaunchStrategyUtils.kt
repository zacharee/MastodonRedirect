package dev.zwander.lemmyredirect.util

import dev.zwander.lemmyredirect.BuildConfig
import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    applicationId = BuildConfig.APPLICATION_ID,
    baseGroupClass = LemmyLaunchStrategyRootGroup::class,
)
