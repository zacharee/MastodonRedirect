package dev.zwander.shared

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.zwander.shared.util.openLinkInBrowser
import dev.zwander.shared.util.prefs
import fe.linksheet.interconnect.LinkSheetConnector
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.http.fullPath
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.net.URLConnection

class RedirectActivity : BaseActivity(), CoroutineScope by MainScope() {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val density = LocalDensity.current

        LaunchedEffect(null) {
            withContext(Dispatchers.IO) {
                handleLink()
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter,
        ) {
            ModalBottomSheet(
                onDismissRequest = {
                    finish()
                },
                sheetState = remember {
                    SheetState(
                        skipPartiallyExpanded = true,
                        density = density,
                        initialValue = SheetValue.Expanded,
                        confirmValueChange = { false },
                    )
                },
                dragHandle = {},
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.size(16.dp))

                Text(
                    text = stringResource(id = R.string.opening_link),
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    style = MaterialTheme.typography.titleLarge,
                )

                Spacer(modifier = Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 128.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }

    private suspend fun handleLink() {
        val url = if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            intent?.dataString
                ?.replace("web+activity+http://", "http://")
                ?.replace("web+activity+https://", "https://")
                ?.replace("web+ap://", "https://")
        }

        val realReferrer = intent?.let {
            LinkSheetConnector.getLinkSheetReferrer(intent)
        } ?: referrer

        val lastHandledLinkIsTheSame = prefs.lastHandledLink.currentValue(this) == url

        val skipUrl = url?.let {
            prefs.blocklistedDomains.currentValue(this).contains(Uri.parse(it).host)
        } == true

        when {
            url.isNullOrBlank() || isSpecialUrl(url) || skipUrl -> launchInBrowser()
            prefs.openMediaInBrowser.currentValue(this) && isUrlMedia(url) -> launchInBrowser()
            else -> {
                prefs.selectedApp.currentValue(this).run {
                    val intents = createIntents(url)

                    if (intents.any { it.`package` == realReferrer?.host } && lastHandledLinkIsTheSame) {
                        launchInBrowser()
                        return@run
                    }

                    intents.forEachIndexed { index, intent ->
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        try {
                            startActivity(intent)

                            if (!sequentialLaunch) {
                                // Found a working launcher, short circuit out of process.
                                return@run
                            }
                        } catch (e: Exception) {
                            Log.e(packageName, "Error launching.", e)

                            if (!sequentialLaunch) {
                                launchInBrowser()
                                return@run
                            }
                        }

                        if (index < intents.lastIndex) {
                            withContext(Dispatchers.IO) {
                                delay(500)
                            }
                        }
                    }

                    if (!sequentialLaunch) {
                        // Didn't find any working launchers, open browser.
                        launchInBrowser()
                    }
                }
            }
        }

        prefs.lastHandledLink.set(url)

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun launchInBrowser() {
        openLinkInBrowser(intent?.data)
    }

    private suspend fun isUrlMedia(url: String): Boolean {
        val parsedUrl = try {
            Url(url)
        } catch (e: Exception) {
            null
        }

        parsedUrl?.let {
            val guessedType = URLConnection.guessContentTypeFromName(parsedUrl.fullPath) ?: ""

            if (guessedType.startsWith("video") ||
                guessedType.startsWith("image") ||
                guessedType.startsWith("audio")) {
                return true
            }
        }

        val response = HttpClient().get(url)
        val returnedType = response.contentType()?.contentType

        return returnedType == "video" ||
                returnedType == "image" ||
                returnedType == "audio"
    }

    private fun isSpecialUrl(url: String): Boolean {
        val parsedUrl = Uri.parse(url)
        val path = parsedUrl.path

        val specialStarters = arrayOf(
            "oauth",
            "auth",
            "miauth",
            "api",
            "api-doc",
        )

        return specialStarters.any { path?.startsWith("/$it") == true }
    }
}