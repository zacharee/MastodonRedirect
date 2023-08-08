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
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object LinkVerificationModel {
    private val _refreshFlow = MutableStateFlow(0)

    val refreshFlow: StateFlow<Int>
        get() = _refreshFlow

    fun refresh() {
        _refreshFlow.value += 1
    }
}

@Suppress("DEPRECATION")
object LinkVerifyUtils {
    @SuppressLint("InlinedApi")
    fun Context.launchManualVerification() {
        val qDirect = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
        val pDirect = "android.settings.APPLICATION_DETAILS_SETTINGS_OPEN_BY_DEFAULT_PAGE"
        val appDetails = Settings.ACTION_APPLICATION_DETAILS_SETTINGS

        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("package:$packageName"))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

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

    fun unverifyAllLinks(packageName: String) {
        val pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"))

        pm.updateIntentVerificationStatus(
            packageName,
            PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS_ASK,
            UserHandle.myUserId(),
        )
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    @Composable
    fun rememberLinkVerificationAsState(): State<LinkVerificationStatus> {
        val context = LocalContext.current
        val lifecycle = LocalLifecycleOwner.current.lifecycle

        var lifecycleState by remember {
            mutableStateOf(lifecycle.currentState)
        }
        
        val refresh by LinkVerificationModel.refreshFlow.collectAsState()

        val verificationStatus = remember {
            mutableStateOf(LinkVerificationStatus())
        }

        DisposableEffect(null) {
            val observer = LifecycleEventObserver { _, event ->
                lifecycleState = event.targetState
            }

            lifecycle.addObserver(observer)

            onDispose {
                lifecycle.removeObserver(observer)
            }
        }

        LaunchedEffect(key1 = lifecycleState, key2 = refresh) {
            Log.e("MastodonRedirect", "$refresh, $lifecycleState")

            if (lifecycleState >= Lifecycle.State.RESUMED) {
                launch(Dispatchers.IO) {
                    val newMissingDomains = mutableListOf<String>()

                    val newVerified = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        val domain =
                            context.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as DomainVerificationManager

                        domain.getDomainVerificationUserState(context.packageName)
                            ?.hostToStateMap
                            ?.toSortedMap()
                            ?.filter { (_, state) ->
                                state == DomainVerificationUserState.DOMAIN_STATE_NONE
                            }
                            ?.let {
                                newMissingDomains.addAll(it.keys)
                            }

                        newMissingDomains.isEmpty()
                    } else {
                        context.packageManager.getIntentVerificationStatusAsUser(
                            context.packageName,
                            UserHandle.myUserId(),
                        ) == PackageManager.INTENT_FILTER_DOMAIN_VERIFICATION_STATUS_ALWAYS
                    }

                    verificationStatus.value = verificationStatus.value.copy(
                        verified = newVerified,
                        missingDomains = newMissingDomains,
                    )
                }
            }
        }

        return verificationStatus
    }
}

data class LinkVerificationStatus(
    val verified: Boolean = true,
    val missingDomains: List<String> = listOf(),
)
