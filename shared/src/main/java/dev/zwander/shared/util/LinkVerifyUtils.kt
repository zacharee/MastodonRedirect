package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.os.ServiceManager
import android.os.UserHandle
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner

@Suppress("DEPRECATION")
object LinkVerifyUtils {
    @SuppressLint("InlinedApi")
    fun Context.launchManualVerification() {
        val qDirect = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
        val pDirect = "android.settings.APPLICATION_DETAILS_SETTINGS_OPEN_BY_DEFAULT_PAGE"
        val appDetails = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("package:$packageName"))

        arrayOf(qDirect, pDirect, appDetails).forEach { action ->
            try {
                intent.action = action
                startActivity(intent)
                return
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    fun verifyAllLinks(packageName: String) {
        val pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))

        pm.updateIntentVerificationStatus(
            packageName,
            PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS,
            UserHandle.myUserId(),
        )
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    @Composable
    fun rememberLinkVerificationAsState(): Triple<State<Boolean>, SnapshotStateList<String>, () -> Unit> {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val verified = remember {
            mutableStateOf(true)
        }
        val missingDomains = remember {
            mutableStateListOf<String>()
        }

        var lifecycleState by remember {
            mutableStateOf(Lifecycle.State.DESTROYED)
        }

        DisposableEffect(null) {
            val observer = object : DefaultLifecycleObserver {
                override fun onCreate(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.CREATED
                }

                override fun onDestroy(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.DESTROYED
                }

                override fun onPause(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.CREATED
                }

                override fun onResume(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.RESUMED
                }

                override fun onStart(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.STARTED
                }

                override fun onStop(owner: LifecycleOwner) {
                    lifecycleState = Lifecycle.State.INITIALIZED
                }
            }

            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        var refreshCounter by remember {
            mutableIntStateOf(0)
        }

        LaunchedEffect(key1 = lifecycleState, key2 = refreshCounter) {
            if (lifecycleState >= Lifecycle.State.RESUMED) {
                verified.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    missingDomains.clear()

                    val domain = context.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as DomainVerificationManager

                    domain.getDomainVerificationUserState(context.packageName)?.hostToStateMap?.all { (host, state) ->
                        if (state == DomainVerificationUserState.DOMAIN_STATE_NONE) {
                            missingDomains.add(host)
                        }

                        state != DomainVerificationUserState.DOMAIN_STATE_NONE
                    } == true
                } else {
                    context.packageManager.getIntentVerificationStatusAsUser(
                        context.packageName,
                        UserHandle.myUserId(),
                    ) == PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS
                }
            }
        }

        return Triple(verified,missingDomains) {
            refreshCounter++
        }
    }
}