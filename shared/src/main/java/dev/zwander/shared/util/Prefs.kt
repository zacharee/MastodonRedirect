package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zwander.shared.LaunchStrategy
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

        const val SELECTED_APP = "selected_app"
        const val ENABLE_CRASH_REPORTS = "enable_crash_reports"
        const val OPEN_MEDIA_IN_BROWSER = "open_media_in_browser"
    }

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    var selectedApp: LaunchStrategy
        get() = with (appModel.launchStrategyUtils) {
            getLaunchStrategyForKey(preferences.getString(SELECTED_APP, appModel.defaultLaunchStrategy.key)) ?: appModel.defaultLaunchStrategy
        }
        set(value) {
            preferences.edit { putString(SELECTED_APP, value.key) }
        }

    var enableCrashReports: Boolean
        get() = preferences.getBoolean(ENABLE_CRASH_REPORTS, false)
        set(value) {
            preferences.edit { putBoolean(ENABLE_CRASH_REPORTS, value) }
        }

    var openMediaInBrowser: Boolean
        get() = preferences.getBoolean(OPEN_MEDIA_IN_BROWSER, false)
        set(value) {
            preferences.edit { putBoolean(OPEN_MEDIA_IN_BROWSER, value) }
        }
}
