package dev.zwander.lemmyredirect

import dev.zwander.lemmyredirect.util.Jerboa
import dev.zwander.lemmyredirect.util.LaunchStrategyUtils
import dev.zwander.shared.App
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.util.BaseLaunchStrategyUtils

class LemmyRedirectApp : App() {
    override val versionCode: Int
        get() = BuildConfig.VERSION_CODE
    override val versionName: String
        get() = BuildConfig.VERSION_NAME
    override val appName: String
        get() = resources.getString(R.string.app_name)
    override val launchStrategyUtils: BaseLaunchStrategyUtils
        get() = LaunchStrategyUtils
    override val defaultLaunchStrategy: LaunchStrategy
        get() = Jerboa.JerboaStable
}