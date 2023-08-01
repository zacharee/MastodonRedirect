package dev.zwander.lemmyredirect.util

import dev.zwander.shared.LaunchStrategyRootGroup
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import kotlin.reflect.KClass

object LaunchStrategyUtils : BaseLaunchStrategyUtils() {
    override val launchAction = "dev.zwander.lemmyredirect.intent.action.OPEN_FEDI_LINK"

    override val baseGroupClass: KClass<out LaunchStrategyRootGroup>
        get() = LemmyLaunchStrategyRootGroup::class
}
