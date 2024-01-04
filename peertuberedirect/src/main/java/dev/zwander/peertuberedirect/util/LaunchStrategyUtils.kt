package dev.zwander.peertuberedirect.util

import dev.zwander.peertuberedirect.BuildConfig
import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils(
    applicationId = BuildConfig.APPLICATION_ID,
    baseGroupClass = PeerTubeLaunchStrategyRootGroup::class,
)
