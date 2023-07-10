package dev.zwander.mastodonredirect

import android.annotation.SuppressLint
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import androidx.core.content.edit
import androidx.preference.PreferenceManager

val Context.prefs: Prefs
    get() = Prefs.getInstance(this)

class Prefs private constructor(context: Context) : ContextWrapper(context) {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private var instance: Prefs? = null

        fun getInstance(context: Context): Prefs {
            return instance ?: Prefs(context.applicationContext ?: context).apply {
                instance = this
            }
        }

        const val SELECTED_APP = "selected_app"
    }

    val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

    var selectedApp: LaunchStrategy
        get() = launchStrategies[preferences.getString(SELECTED_APP, Megalodon.key)] ?: Megalodon
        set(value) {
            preferences.edit { putString(SELECTED_APP, value.key) }
        }
}
