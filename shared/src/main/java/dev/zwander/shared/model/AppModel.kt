package dev.zwander.shared.model

import androidx.compose.runtime.compositionLocalOf
import dev.zwander.shared.IShizukuService
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.util.BaseLaunchStrategyUtils
import dev.zwander.shared.util.Prefs
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

val LocalAppModel = compositionLocalOf<AppModel> { throw IllegalStateException("No app model provided!") }

interface AppModel {
    val launchStrategyUtils: BaseLaunchStrategyUtils
    val defaultLaunchStrategy: LaunchStrategy
    val fetchActivity: Class<*>

    val versionName: String
    val appName: String

    val prefs: Prefs

    fun postShizukuCommand(context: CoroutineContext = EmptyCoroutineContext, command: IShizukuService.() -> Unit)
}