package dev.zwander.shared.util

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import tk.zwander.linksheet.interconnect.LinkSheet

val ActivityInfo.componentNameCompat: ComponentName
    get() = ComponentName(packageName, name)

@Composable
fun rememberLinkSheetInstallationStatus(): LinkSheetStatus {
    val context = LocalContext.current

    fun checkStatus(): LinkSheetStatus {
        with (LinkSheet) {
            val installed = context.isLinkSheetInstalled()

            if (!installed) {
                return LinkSheetStatus.NOT_INSTALLED
            }

            val interconnect = context.supportsInterconnect()

            if (!interconnect) {
                return LinkSheetStatus.INSTALLED_NO_INTERCONNECT
            }

            return LinkSheetStatus.INSTALLED_WITH_INTERCONNECT
        }
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
