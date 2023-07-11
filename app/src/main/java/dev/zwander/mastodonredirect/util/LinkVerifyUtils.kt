package dev.zwander.mastodonredirect.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.IPackageManager
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.content.pm.verify.domain.IDomainVerificationManager
import android.os.Build
import android.os.ServiceManager
import android.os.UserHandle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import rikka.shizuku.ShizukuBinderWrapper

@Suppress("DEPRECATION")
object LinkVerifyUtils {
    fun verifyAllLinks(packageName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val domainVerifier = IDomainVerificationManager.Stub.asInterface(
                ShizukuBinderWrapper(ServiceManager.getService(Context.DOMAIN_VERIFICATION_SERVICE))
            )

            domainVerifier.setDomainVerificationLinkHandlingAllowed(
                packageName,
                true,
                UserHandle.USER_ALL
            )
        } else {
            val pm = IPackageManager.Stub.asInterface(
                ShizukuBinderWrapper(ServiceManager.getService("package"))
            )

            pm.updateIntentVerificationStatus(
                packageName,
                PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS,
                UserHandle.USER_ALL,
            )
        }
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    @Composable
    fun rememberLinkVerificationAsState(): Pair<State<Boolean>, () -> Unit> {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle
        val verified = remember {
            mutableStateOf(false)
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
            mutableStateOf(0)
        }

        LaunchedEffect(key1 = lifecycleState, key2 = refreshCounter) {
            if (lifecycleState >= Lifecycle.State.RESUMED) {
                verified.value = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val domain = context.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as DomainVerificationManager

                    domain.getDomainVerificationUserState(context.packageName)?.hostToStateMap?.all { (_, state) ->
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

        return verified to {
            refreshCounter++
        }
    }
}