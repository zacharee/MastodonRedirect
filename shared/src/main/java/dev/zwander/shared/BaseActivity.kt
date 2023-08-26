package dev.zwander.shared

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import dev.zwander.shared.components.MainContent
import dev.zwander.shared.model.LocalAppModel
import dev.zwander.shared.util.locals.LocalLinkSheet
import fe.linksheet.interconnect.LinkSheetConnector

open class BaseActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            CompositionLocalProvider(
                LocalAppModel provides appModel,
                LocalLinkSheet provides with (LinkSheetConnector) { getLinkSheet() },
            ) {
                WindowCompat.getInsetsController(window, window.decorView).apply {
                    isAppearanceLightStatusBars = !isSystemInDarkTheme()
                    isAppearanceLightNavigationBars = isAppearanceLightStatusBars
                }

                Content()
            }
        }
    }

    @Composable
    open fun Content() {
        MainContent()
    }
}