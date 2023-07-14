package dev.zwander.mastodonredirect.ui.theme

import androidx.compose.runtime.Composable
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun MastodonRedirectTheme(
    content: @Composable () -> Unit
) {
    Mdc3Theme(
        content = content,
    )
}