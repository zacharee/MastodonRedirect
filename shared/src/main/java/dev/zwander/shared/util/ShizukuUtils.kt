package dev.zwander.shared.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import dev.zwander.shared.IShizukuService
import dev.zwander.shared.appModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuProvider
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object ShizukuUtils {
    @Composable
    fun rememberHasPermissionAsState(): State<Boolean> {
        val hasPermission = remember {
            mutableStateOf(Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED)
        }

        DisposableEffect(null) {
            val listener = Shizuku.OnRequestPermissionResultListener { _, grantResult ->
                hasPermission.value = grantResult == PackageManager.PERMISSION_GRANTED
            }

            Shizuku.addRequestPermissionResultListener(listener)

            onDispose {
                Shizuku.removeRequestPermissionResultListener(listener)
            }
        }

        return hasPermission
    }

    val isShizukuRunning: Boolean
        get() = Shizuku.pingBinder()

    val Context.isShizukuInstalled: Boolean
        get() = try {
            @Suppress("DEPRECATION")
            packageManager.getPackageInfo(ShizukuProvider.MANAGER_APPLICATION_ID, 0)
            true
        } catch (e: Throwable) {
            false
        }

    suspend fun Context.runShizukuCommand(
        coroutineContext: CoroutineContext = EmptyCoroutineContext,
        block: IShizukuService.() -> Unit,
    ): ShizukuCommandResult = coroutineScope {
        val shizukuPermission = Shizuku.pingBinder() && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED

        if (isShizukuRunning) {
            if (shizukuPermission) {
                appModel.postShizukuCommand(coroutineContext, block)
                ShizukuCommandResult.POSTED
            } else {
                withContext(Dispatchers.IO) {
                    val granted = suspendCoroutine { cont ->
                        val listener = object : Shizuku.OnRequestPermissionResultListener {
                            override fun onRequestPermissionResult(
                                requestCode: Int,
                                grantResult: Int
                            ) {
                                Shizuku.removeRequestPermissionResultListener(this)
                                cont.resume(grantResult == PackageManager.PERMISSION_GRANTED)
                            }
                        }
                        Shizuku.addRequestPermissionResultListener(listener)
                        Shizuku.requestPermission(100)
                    }

                    if (granted) {
                        appModel.postShizukuCommand(coroutineContext, block)
                        ShizukuCommandResult.POSTED
                    } else {
                        ShizukuCommandResult.PERMISSION_DENIED
                    }
                }
            }
        } else {
            val installed = isShizukuInstalled

            if (installed) {
                ShizukuCommandResult.INSTALLED_NOT_RUNNING
            } else {
                ShizukuCommandResult.NOT_INSTALLED
            }
        }
    }
}

enum class ShizukuCommandResult {
    NOT_INSTALLED,
    INSTALLED_NOT_RUNNING,
    PERMISSION_DENIED,
    POSTED,
}
