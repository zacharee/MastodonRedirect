package dev.zwander.shared

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dev.zwander.shared.util.openLinkInBrowser
import dev.zwander.shared.util.prefs
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
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.net.URLConnection

class RedirectActivity : ComponentActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = if (intent?.action == Intent.ACTION_SEND) {
            intent.getStringExtra(Intent.EXTRA_TEXT)
        } else {
            intent?.data?.toString()?.replace("web+activity+", "")
        }

        runBlocking(Dispatchers.IO) {
            when {
                url.isNullOrBlank() || url.contains("oauth/authorize") -> launchInBrowser()
                prefs.openMediaInBrowser.currentValue(this) && isUrlMedia(url) -> launchInBrowser()
                else -> {
                    prefs.selectedApp.currentValue(this).run {
                        createIntents(url).forEach {
                            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                            try {
                                startActivity(it)

                                if (!sequentialLaunch) {
                                    // Found a working launcher, short circuit out of process.
                                    return@run
                                }
                            } catch (e: Exception) {
                                Log.e(packageName, "Error launching.", e)

                                if (sequentialLaunch) {
                                    launchInBrowser()
                                    return@run
                                }
                            }

                            if (sequentialLaunch) {
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

            finish()
        }
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
            val guessedType = URLConnection.guessContentTypeFromName(parsedUrl.fullPath)

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
}