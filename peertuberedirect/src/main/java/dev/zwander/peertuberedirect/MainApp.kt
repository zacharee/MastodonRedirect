package dev.zwander.peertuberedirect

import dev.zwander.peertuberedirect.util.LaunchStrategyUtils
import dev.zwander.peertuberedirect.util.NewPipe
import dev.zwander.shared.App

class MainApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    defaultLaunchStrategy = NewPipe.Base.Release,
    appNameRes = R.string.app_name,
)
