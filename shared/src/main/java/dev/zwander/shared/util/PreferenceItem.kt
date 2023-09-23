package dev.zwander.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface PreferenceItem<T, K> {
    val key: Preferences.Key<K>
    val dataStore: DataStore<Preferences>
    val default: T
    val toValue: (T?) -> K?
    val fromValue: (K?) -> T?

    val value: Flow<T?>
        get() = dataStore.data.map { fromValue(it[key]) }

    fun asStateFlow(scope: CoroutineScope): StateFlow<T> {
        return value.map { it ?: default }.stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = value.firstBlocking() ?: default,
        )
    }

    fun currentValue(scope: CoroutineScope): T {
        return asStateFlow(scope).value
    }

    suspend fun set(newValue: T?) {
        dataStore.edit { prefs ->
            toValue(newValue)?.let {
                prefs[key] = it
            } ?: prefs.remove(key)
        }
    }
}

data class SimplePreferenceItem<T>(
    override val dataStore: DataStore<Preferences>,
    override val key: Preferences.Key<T>,
    override val default: T,
) : PreferenceItem<T, T> {
    override val toValue: (T?) -> T? = { it }
    override val fromValue: (T?) -> T? = { it }
}

data class ComplexPreferenceItem<T, K>(
    override val dataStore: DataStore<Preferences>,
    override val key: Preferences.Key<K>,
    override val default: T,
    override val toValue: (T?) -> K?,
    override val fromValue: (K?) -> T?,
) : PreferenceItem<T, K>

@Composable
fun <T, K> PreferenceItem<T, K>.rememberMutablePreferenceState(): MutableState<T> {
    val coroutineScope = rememberCoroutineScope()
    val state = value.collectAsState(
        initial = value.firstBlocking() ?: default
    )

    return remember {
        object : MutableState<T> {
            override var value: T
                get() = state.value ?: default
                set(value) {
                    coroutineScope.launch {
                        set(value)
                    }
                }

            override fun component1() = value
            override fun component2(): (T) -> Unit = { value = it }
        }
    }
}
