package dev.zwander.peertuberedirect

import dev.zwander.peertuberedirect.util.FetchInstancesActivity
import dev.zwander.peertuberedirect.util.LaunchStrategyUtils
import dev.zwander.peertuberedirect.util.NewPipe
import dev.zwander.shared.App

class MainApp : App(
    launchStrategyUtils = LaunchStrategyUtils,
    fetchActivity = FetchInstancesActivity::class.java,
    defaultLaunchStrategy = NewPipe.Base.Release,
    appNameRes = R.string.app_name,
)
