package dev.zwander.shared

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import dev.zwander.shared.components.MainContent
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.RedirectorTheme
import dev.zwander.shared.util.locals.LocalLinkSheet
import dev.zwander.shared.util.locals.rememberLinkSheet

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val linkSheet by rememberLinkSheet()

            CompositionLocalProvider(
                LocalAppModel provides appModel,
                LocalLinkSheet provides linkSheet,
            ) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isSystemInDarkTheme()
                    isAppearanceLightNavigationBars = isAppearanceLightStatusBars
                }

                RedirectorTheme {
                    Content()
                }
            }
        }
    }

    @Composable
    open fun Content() {
        MainContent()
    }
}