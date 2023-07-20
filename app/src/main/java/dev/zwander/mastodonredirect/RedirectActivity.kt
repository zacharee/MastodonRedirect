package dev.zwander.mastodonredirect

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import dev.zwander.mastodonredirect.util.prefs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class RedirectActivity : ComponentActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.data?.toString()?.replace("web+activity+", "")

        if (url?.contains("oauth/authorize") == true) {
            launchInBrowser()
        } else {
            prefs.selectedApp.run {
                if (url == null) {
                    launchInBrowser()
                } else {
                    createIntents(url).forEach {
                        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

                        try {
                            startActivity(it)
                            // Found a working launcher, short circuit out of process.
                            return@run
                        } catch (e: Exception) {
                            Log.e("MastodonRedirect", "Error launching.", e)
                        }
                    }

                    // Didn't find any working launchers, open browser.
                    launchInBrowser()
                }
            }
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }

    private fun launchInBrowser() {
        startActivity(
            Intent(Intent.ACTION_VIEW).apply {
                addCategory(Intent.CATEGORY_BROWSABLE)
                data = intent?.data
                selector = Intent(Intent.ACTION_VIEW).apply {
                    addCategory(Intent.CATEGORY_BROWSABLE)
                    data = Uri.parse("https://")
                }
            }
        )
    }
}