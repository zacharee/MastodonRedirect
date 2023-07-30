package dev.zwander.mastodonredirect.util

import dev.zwander.shared.util.BaseLaunchStrategyUtils

object LaunchStrategyUtils : BaseLaunchStrategyUtils() {
    override val launchAction = "dev.zwander.mastodonredirect.intent.action.OPEN_FEDI_LINK"

    override val groupedLaunchStrategies = MastodonLaunchStrategyRootGroup::class.sealedSubclasses
        .mapNotNull { it.objectInstance }.filter { it.autoAdd }
}
