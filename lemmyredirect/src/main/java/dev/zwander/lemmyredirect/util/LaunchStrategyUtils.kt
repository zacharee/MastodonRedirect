package dev.zwander.lemmyredirect.util

import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils() {
    override val launchAction = "dev.zwander.lemmyredirect.intent.action.OPEN_FEDI_LINK"

    override val groupedLaunchStrategies = LemmyLaunchStrategyRootGroup::class.sealedSubclasses
        .mapNotNull { it.objectInstance }.filter { it.autoAdd }
}
