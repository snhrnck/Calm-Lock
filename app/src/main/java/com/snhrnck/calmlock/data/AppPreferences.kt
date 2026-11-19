package com.snhrnck.calmlock.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences(AppConstants.PREFS_NAME, Context.MODE_PRIVATE)

    var language: String
        get() = prefs.getString(AppConstants.KEY_LANG, "EN") ?: "EN"
        set(value) = prefs.edit().putString(AppConstants.KEY_LANG, value).apply()

    var mode: String
        get() = prefs.getString(AppConstants.KEY_MODE, AppConstants.MODE_OFF) ?: AppConstants.MODE_OFF
        set(value) = prefs.edit().putString(AppConstants.KEY_MODE, value).apply()

    var hours: Int
        get() = prefs.getInt(AppConstants.KEY_HOURS, 1)
        set(value) = prefs.edit().putInt(AppConstants.KEY_HOURS, value).apply()

    var minutes: Int
        get() = prefs.getInt(AppConstants.KEY_MINUTES, 0)
        set(value) = prefs.edit().putInt(AppConstants.KEY_MINUTES, value).apply()

    var lastFile: String
        get() = prefs.getString(AppConstants.KEY_LAST_FILE, "") ?: ""
        set(value) = prefs.edit().putString(AppConstants.KEY_LAST_FILE, value).apply()

    var isSequential: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_SEQUENTIAL, false)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_SEQUENTIAL, value).apply()

    var showBatteryPrompt: Boolean
        get() = prefs.getBoolean(AppConstants.KEY_SHOW_BATTERY_PROMPT, true)
        set(value) = prefs.edit().putBoolean(AppConstants.KEY_SHOW_BATTERY_PROMPT, value).apply()

    var lastChangeTime: Long
        get() = prefs.getLong(AppConstants.KEY_LAST_CHANGE_TIME, 0L)
        set(value) = prefs.edit().putLong(AppConstants.KEY_LAST_CHANGE_TIME, value).apply()

    fun clearMode() {
        mode = AppConstants.MODE_OFF
    }
}