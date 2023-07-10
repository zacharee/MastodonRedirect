package dev.zwander.mastodonredirect

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

class RedirectActivity : ComponentActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val url = intent?.data?.toString()

        prefs.selectedApp.run {
            createIntents(url).forEach {
                try {
                    startActivity(it)
                } catch (e: Exception) {
                    Log.e("MastodonRedirect", "Error launching.", e)
                }
            }
        }

        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel()
    }
}