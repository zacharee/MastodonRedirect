package dev.zwander.shared.util

import android.content.ComponentName
import android.content.pm.ActivityInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import fe.linksheet.interconnect.LinkSheet

val ActivityInfo.componentNameCompat: ComponentName
    get() = ComponentName(packageName, name)

@Composable
fun rememberLinkSheetInstallationStatus(): Boolean {
    val context = LocalContext.current

    val state = remember {
        mutableStateOf(with (LinkSheet) { context.isLinkSheetInstalled() })
    }

    LifecycleEffect(Lifecycle.State.RESUMED) {
        state.value = with (LinkSheet) { context.isLinkSheetInstalled() }
    }

    return state.value
}
