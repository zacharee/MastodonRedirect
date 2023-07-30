package dev.zwander.shared.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.compose.runtime.compositionLocalOf
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import dev.zwander.shared.LaunchStrategy

val LocalPrefs = compositionLocalOf<Prefs> { throw IllegalStateException("Prefs local not provided!") }

fun Context.prefs(strategyUtils: BaseLaunchStrategyUtils, defaultStrategy: LaunchStrategy): Prefs {
    return Prefs.getInstance(this, strategyUtils, defaultStrategy)
}

class Prefs private constructor(
    context: Context,
    private val strategyUtils: BaseLaunchStrategyUtils,
    private val defaultStrategy: LaunchStrategy,
) : ContextWrapper(context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Prefs? = null

        fun getInstance(context: Context, strategyUtils: BaseLaunchStrategyUtils, defaultStrategy: LaunchStrategy): Prefs {
            return instance ?: Prefs(
                context.applicationContext ?: context,
                strategyUtils,
                defaultStrategy
            ).apply {
                instance = this
            }
        }

        const val SELECTED_APP = "selected_app"
        const val ENABLE_CRASH_REPORTS = "enable_crash_reports"
    }

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    var selectedApp: LaunchStrategy
        get() = with (strategyUtils) {
            getLaunchStrategyForKey(preferences.getString(SELECTED_APP, defaultStrategy.key)) ?: defaultStrategy
        }
        set(value) {
            preferences.edit { putString(SELECTED_APP, value.key) }
        }

    var enableCrashReports: Boolean
        get() = preferences.getBoolean(ENABLE_CRASH_REPORTS, false)
        set(value) {
            preferences.edit { putBoolean(ENABLE_CRASH_REPORTS, value) }
        }
}
