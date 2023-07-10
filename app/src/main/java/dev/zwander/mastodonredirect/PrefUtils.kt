package dev.zwander.mastodonredirect

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun <T> Context.rememberPreferenceState(
    key: String,
    value: () -> T,
    onChanged: (T) -> Unit
): MutableState<T> {
    val state = remember(key) {
        mutableStateOf(value())
    }

    LaunchedEffect(key1 = state.value) {
        onChanged(state.value)
    }

    DisposableEffect(key1 = key) {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, k ->
            if (key == k) {
                state.value = value()
            }
        }

        prefs.preferences.registerOnSharedPreferenceChangeListener(listener)

        onDispose {
            prefs.preferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }

    return state
}

@Composable
fun Context.rememberBooleanPreferenceState(
    key: String,
    enabled: () -> Boolean,
    onEnabledChanged: (Boolean) -> Unit
): MutableState<Boolean> {
    return rememberPreferenceState(
        key = key,
        value = enabled,
        onChanged = onEnabledChanged
    )
}