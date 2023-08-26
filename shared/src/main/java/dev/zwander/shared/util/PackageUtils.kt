package dev.zwander.shared.util

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import dev.zwander.shared.util.locals.LocalLinkSheet

val ActivityInfo.componentNameCompat: ComponentName
    get() = ComponentName(packageName, name)

@Composable
fun rememberLinkSheetInstallationStatus(): LinkSheetStatus {
    val linkSheet = LocalLinkSheet.current

    fun checkStatus(): LinkSheetStatus {
        if (linkSheet == null) {
            return LinkSheetStatus.NOT_INSTALLED
        }

        if (!linkSheet.supportsInterconnect) {
            return LinkSheetStatus.INSTALLED_NO_INTERCONNECT
        }

        return LinkSheetStatus.INSTALLED_WITH_INTERCONNECT
    }

    val state = remember {
        mutableStateOf(checkStatus())
    }

    LifecycleEffect(Lifecycle.State.RESUMED) {
        state.value = checkStatus()
    }

    return state.value
}

enum class LinkSheetStatus {
    NOT_INSTALLED,
    INSTALLED_NO_INTERCONNECT,
    INSTALLED_WITH_INTERCONNECT,
}
