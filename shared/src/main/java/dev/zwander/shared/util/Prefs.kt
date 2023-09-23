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
        val LAST_HANDLED_LINK = stringPreferencesKey("last_handled_link")
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
        dataStore = dataStore,
        key = SELECTED_APP,
        default = appModel.defaultLaunchStrategy,
        toValue = { it?.key },
        fromValue = {
            with (appModel.launchStrategyUtils) {
                getLaunchStrategyForKey(it)
            }
        },
    )

    val enableCrashReports = SimplePreferenceItem(
        dataStore = dataStore,
        key = ENABLE_CRASH_REPORTS,
        default = false,
    )

    val openMediaInBrowser = SimplePreferenceItem(
        dataStore = dataStore,
        key = OPEN_MEDIA_IN_BROWSER,
        default = false,
    )

    val lastHandledLink = SimplePreferenceItem(
        dataStore = dataStore,
        key = LAST_HANDLED_LINK,
        default = "",
    )
}
