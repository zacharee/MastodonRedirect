package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import dev.zwander.shared.RedirectActivity
import fe.linksheet.interconnect.LinkSheet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object LinkVerifyActions {
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
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        }
    }

    fun Context.enableWithLinkSheet(
        scope: CoroutineScope,
        linkSheet: LinkSheet?,
        linkSheetStatus: LinkSheetStatus,
        missingDomains: List<String>,
        refresh: () -> Unit,
    ) {
        when (linkSheetStatus) {
            LinkSheetStatus.NOT_INSTALLED -> {
                openLinkNaturally(Uri.parse("https://github.com/1fexd/LinkSheet"))
            }
            LinkSheetStatus.INSTALLED_NO_INTERCONNECT -> {
                linkSheet?.packageName?.let { pkg ->
                    try {
                        startActivity(
                            packageManager.getLaunchIntentForPackage(
                                pkg
                            )?.apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            },
                        )
                    } catch (e: Exception) {
                        Log.e(
                            "FediverseRedirect",
                            "Failed to open LinkSheet.",
                            e
                        )
                    }
                }
            }
            LinkSheetStatus.INSTALLED_WITH_INTERCONNECT -> {
                scope.launch(Dispatchers.IO) {
                    val component = ComponentName(
                        this@enableWithLinkSheet,
                        RedirectActivity::class.java,
                    )

                    try {
                        linkSheet?.bindService(this@enableWithLinkSheet)?.selectDomainsWithResult(
                            packageName = packageName,
                            domains = missingDomains,
                            componentName = component,
                        )

                        refresh()
                    } catch (e: Exception) {
                        linkSheet?.bindService(this@enableWithLinkSheet)?.selectDomains(
                            packageName = packageName,
                            domains = missingDomains,
                            componentName = component,
                        )
                    }
                }
            }
        }
    }
}
