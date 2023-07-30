package dev.zwander.shared.util

import androidx.compose.runtime.Composable
import com.google.accompanist.themeadapter.material3.Mdc3Theme

@Composable
fun RedirectorTheme(
    content: @Composable () -> Unit
) {
    Mdc3Theme(
        content = content,
    )
}