package dev.zwander.mastodonredirect

import dev.zwander.mastodonredirect.util.LaunchStrategyUtils
import dev.zwander.mastodonredirect.util.Megalodon
import dev.zwander.shared.App
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.util.BaseLaunchStrategyUtils

class MastodonRedirectApp : App() {
    override val versionCode: Int
        get() = BuildConfig.VERSION_CODE
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
    override val appName: String
        get() = resources.getString(R.string.app_name)
    override val launchStrategyUtils: BaseLaunchStrategyUtils
        get() = LaunchStrategyUtils
    override val defaultLaunchStrategy: LaunchStrategy
        get() = Megalodon.MegalodonStable
}