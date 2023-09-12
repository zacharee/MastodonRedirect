package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.verify.domain.DomainVerificationManager
import android.content.pm.verify.domain.DomainVerificationUserState
import android.os.Build
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
import dev.zwander.shared.util.locals.LocalLinkSheet
import fe.linksheet.interconnect.LinkSheet
import fe.linksheet.interconnect.LinkSheetServiceConnection
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

object LinkVerificationModel {
    private val _refreshFlow = MutableStateFlow(0)

    val refreshFlow: StateFlow<Int>
        get() = _refreshFlow

    val isRefreshing = MutableStateFlow(false)

    fun refresh() {
        _refreshFlow.value += 1
    }
}

object LinkVerifyUtils {
    @SuppressLint("WrongConstant", "MissingPermission")
    @Composable
    fun rememberLinkVerificationAsState(): State<LinkVerificationStatus> {
        val context = LocalContext.current
        val linkSheet = LocalLinkSheet.current
        val refresh by LinkVerificationModel.refreshFlow.collectAsState()

        val verificationStatus = remember {
            mutableStateOf(LinkVerificationStatus())
        }

        LifecycleEffect(Lifecycle.State.RESUMED, keys = listOf(refresh)) {
            launch(Dispatchers.IO) {
                LinkVerificationModel.isRefreshing.value = true
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
                        val linkSheetStatus = context.checkLinkSheetStatus(linkSheet, allDomains)

                        if (linkSheetStatus == null) {
                            newMissingDomains.addAll(unverifiedDomains)
                            false
                        } else {
                            newMissingDomains.addAll(linkSheetStatus.missingDomains)
                            linkSheetStatus.verified
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

                LinkVerificationModel.isRefreshing.value = false
            }
        }

        return verificationStatus
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private suspend fun Context.checkLinkSheetStatus(linkSheet: LinkSheet?, allDomains: List<String>): LinkVerificationStatus? {
        return if (linkSheet != null && linkSheet.supportsInterconnect) {
            linkSheet.useService(this) { service ->
                val selectedDomains = service.getSelectedDomainsAsync(packageName).list
                val difference = allDomains - selectedDomains.toSet()

                LinkVerificationStatus(
                    verified = difference.isEmpty(),
                    missingDomains = difference,
                )
            }
        } else {
            null
        }
    }
}

data class LinkVerificationStatus(
    val verified: Boolean = true,
    val missingDomains: List<String> = listOf(),
)

suspend fun <T> LinkSheet.useService(
    context: Context,
    block: suspend (service: LinkSheetServiceConnection) -> T,
): T {
    val service = bindService(context)

    try {
        return block(service)
    } finally {
        service.disconnect()
    }
}
