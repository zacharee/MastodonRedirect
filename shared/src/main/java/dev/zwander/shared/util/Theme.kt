package dev.zwander.shared.util

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
fun RedirectorTheme(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val isDarkTheme = isSystemInDarkTheme()
    val isAtLeastAndroid12 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    MaterialTheme(
        colorScheme = when {
            isAtLeastAndroid12 && isDarkTheme -> dynamicDarkColorScheme(context)
            isAtLeastAndroid12 && !isDarkTheme -> dynamicLightColorScheme(context)
            !isAtLeastAndroid12 && isDarkTheme -> darkColorScheme()
            else -> lightColorScheme()
        },
        content = content,
    )
}