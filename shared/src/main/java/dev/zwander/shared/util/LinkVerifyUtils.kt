package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import dev.zwander.shared.util.hiddenapi.PackageManager
import fe.linksheet.interconnect.LinkSheet
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
        PackageManager.setLinkVerificationState(
            packageName,
            PackageManager.VerificationStatus.ALWAYS,
        )
    }

    fun unverifyAllLinks(packageName: String) {
        PackageManager.setLinkVerificationState(
            packageName,
            PackageManager.VerificationStatus.ALWAYS_ASK,
        )
    }

    @SuppressLint("WrongConstant", "MissingPermission")
    @Composable
    fun rememberLinkVerificationAsState(): State<LinkVerificationStatus> {
        val context = LocalContext.current
        val refresh by LinkVerificationModel.refreshFlow.collectAsState()

        val verificationStatus = remember {
            mutableStateOf(LinkVerificationStatus())
        }

        LifecycleEffect(Lifecycle.State.RESUMED, keys = listOf(refresh)) {
            launch(Dispatchers.IO) {
                val newMissingDomains = mutableListOf<String>()

                val newVerified = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val domain =
                        context.getSystemService(Context.DOMAIN_VERIFICATION_SERVICE) as DomainVerificationManager
                    val allDomains = mutableListOf<String>()

                    val unverifiedDomains = domain.getDomainVerificationUserState(context.packageName)
                        ?.hostToStateMap
                        ?.toSortedMap()
                        ?.also {
                            allDomains.addAll(it.keys)
                        }
                        ?.filter { (_, state) ->
                            state == DomainVerificationUserState.DOMAIN_STATE_NONE
                        }
                        ?.keys ?: listOf()

                    if (unverifiedDomains.isEmpty()) {
                        true
                    } else {
                        context.checkLinkSheetStatus(allDomains)?.let {
                            newMissingDomains.addAll(it.missingDomains)
                            it.verified
                        } ?: run {
                            newMissingDomains.addAll(unverifiedDomains)
                            false
                        }
                    }
                } else {
                    PackageManager.getIntentVerificationStatus(
                        context.packageName,
                    ) == PackageManager.VerificationStatus.ALWAYS
                }

                verificationStatus.value = verificationStatus.value.copy(
                    verified = newVerified,
                    missingDomains = newMissingDomains,
                )
            }
        }

        return verificationStatus
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun Context.checkLinkSheetStatus(allDomains: List<String>): LinkVerificationStatus? {
        return with (LinkSheet) {
            if (isLinkSheetInstalled()) {
                val service = bindService()

                val selectedDomains = service.getSelectedDomains(packageName)

                val difference = allDomains - selectedDomains.list.toSet()

                LinkVerificationStatus(
                    verified = difference.isEmpty(),
                    missingDomains = difference,
                )
            } else {
                null
            }
        }
    }
}

data class LinkVerificationStatus(
    val verified: Boolean = true,
    val missingDomains: List<String> = listOf(),
)
