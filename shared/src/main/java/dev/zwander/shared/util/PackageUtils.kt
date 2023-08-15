package dev.zwander.shared.util

import android.content.ComponentName
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle

val ActivityInfo.componentNameCompat: ComponentName
    get() = ComponentName(packageName, name)

@Composable
fun rememberLinkSheetInstallationStatus(): Boolean {
    val context = LocalContext.current

    fun checkInstalled(): Boolean {
        return try {
            context.packageManager.getApplicationInfo("fe.linksheet", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    val state = remember {
        mutableStateOf(checkInstalled())
    }

    LifecycleEffect(Lifecycle.State.RESUMED) {
        state.value = checkInstalled()
    }

    return state.value
}
