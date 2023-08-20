package dev.zwander.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

interface PreferenceItem<T, K> {
    val key: Preferences.Key<K>
    val value: Flow<T?>
    val default: T
    val transform: (T) -> K

    fun asStateFlow(scope: CoroutineScope): StateFlow<T> {
        return value.map { it ?: default }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = default,
        )
    }

    fun currentValue(scope: CoroutineScope): T {
        return asStateFlow(scope).value
    }
}

data class SimplePreferenceItem<T>(
    override val key: Preferences.Key<T>,
    override val value: Flow<T?>,
    override val default: T,
) : PreferenceItem<T, T> {
    override val transform: (T) -> T = { it }
}

data class ComplexPreferenceItem<T, K>(
    override val key: Preferences.Key<K>,
    override val value: Flow<T?>,
    override val default: T,
    override val transform: (T) -> K,
) : PreferenceItem<T, K>

@Composable
fun <T, K> PreferenceItem<T, K>.rememberMutablePreferenceState(): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val state = value.collectAsState(initial = default)

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value ?: default
                set(value) {
                    coroutineScope.launch {
                        context.prefs.dataStore.edit {
                            it[key] = transform(value)
                        }
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}
