package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import dev.zwander.shared.LaunchStrategy
import dev.zwander.shared.app
import dev.zwander.shared.appModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

val Context.prefs: Prefs
    get() = Prefs.getInstance(this)

class Prefs private constructor(
    context: Context,
) : ContextWrapper(context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Prefs? = null

        fun getInstance(context: Context): Prefs {
            return instance ?: Prefs(context.app).apply {
                instance = this
            }
        }

        val SELECTED_APP = stringPreferencesKey("selected_app")
        val ENABLE_CRASH_REPORTS = booleanPreferencesKey("enable_crash_reports")
        val OPEN_MEDIA_IN_BROWSER = booleanPreferencesKey("open_media_in_browser")
    }

    val dataStore by preferencesDataStore(
        name = "redirector_prefs",
        produceMigrations = {
            listOf(
                SharedPreferencesMigration(
                    { PreferenceManager.getDefaultSharedPreferences(this) },
                )
            )
        },
    )

    val selectedApp: PreferenceItem<LaunchStrategy, String>
        get() = PreferenceItem(
            key = SELECTED_APP,
            value = dataStore.data.map {
                with (appModel.launchStrategyUtils) {
                    getLaunchStrategyForKey(it[SELECTED_APP])
                }
            },
            default = appModel.defaultLaunchStrategy,
            transform = { it.key },
        )

    val enableCrashReports: SimplePreferenceItem<Boolean>
        get() = SimplePreferenceItem(
            key = ENABLE_CRASH_REPORTS,
            value = dataStore.data.map { it[ENABLE_CRASH_REPORTS] },
            default = false,
        )

    val openMediaInBrowser: SimplePreferenceItem<Boolean>
        get() = SimplePreferenceItem(
            key = OPEN_MEDIA_IN_BROWSER,
            value = dataStore.data.map { it[OPEN_MEDIA_IN_BROWSER] },
            default = false,
        )
}

class SimplePreferenceItem<T>(
    key: Preferences.Key<T>,
    value: Flow<T?>,
    default: T,
) : PreferenceItem<T, T>(
    key = key,
    value = value,
    default = default,
    transform = { it },
)

open class PreferenceItem<T, K>(
    val key: Preferences.Key<K>,
    val value: Flow<T?>,
    val default: T,
    val transform: (T) -> K,
) {
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
