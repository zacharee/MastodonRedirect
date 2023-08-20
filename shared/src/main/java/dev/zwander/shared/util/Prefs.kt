package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.preference.PreferenceManager
import dev.zwander.shared.app
import dev.zwander.shared.appModel
import kotlinx.coroutines.flow.map

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
                ),
            )
        },
    )

    val selectedApp = ComplexPreferenceItem(
        key = SELECTED_APP,
        value = dataStore.data.map {
            with (appModel.launchStrategyUtils) {
                getLaunchStrategyForKey(it[SELECTED_APP])
            }
        },
        default = appModel.defaultLaunchStrategy,
        transform = { it.key },
    )

    val enableCrashReports = SimplePreferenceItem(
        key = ENABLE_CRASH_REPORTS,
        value = dataStore.data.map { it[ENABLE_CRASH_REPORTS] },
        default = false,
    )

    val openMediaInBrowser = SimplePreferenceItem(
        key = OPEN_MEDIA_IN_BROWSER,
        value = dataStore.data.map { it[OPEN_MEDIA_IN_BROWSER] },
        default = false,
    )
}
