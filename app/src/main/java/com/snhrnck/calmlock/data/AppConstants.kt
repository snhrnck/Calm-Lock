package com.snhrnck.calmlock.data

import androidx.compose.ui.graphics.Color

val BgSlate = Color(0xFF1C2228)
val CardSlate = Color(0xFF2A323B)
val AccentSage = Color(0xFF6B9E8A)
val AccentSand = Color(0xFFD4B595)
val TextCream = Color(0xFFF2EFE9)
val TextMuted = Color(0xFF9AA5B1)
val DangerCoral = Color(0xFFD9776C)

object AppConstants {
    const val WALLPAPER_DIR = "wallpapers"
    const val PREFS_NAME = "calm_prefs"

    const val MODE_OFF = "OFF"
    const val MODE_TIMER = "TIMER"
    const val MODE_SCREEN_OFF = "SCREEN_OFF"

    const val SCREEN_OFF_COOLDOWN_MS = 10_000L

    const val KEY_LANG = "lang"
    const val KEY_MODE = "mode"
    const val KEY_HOURS = "hours"
    const val KEY_MINUTES = "minutes"
    const val KEY_LAST_FILE = "last_file"
    const val KEY_SEQUENTIAL = "is_sequential"
    const val KEY_SHOW_BATTERY_PROMPT = "show_battery_prompt"
    const val KEY_LAST_CHANGE_TIME = "last_change_time"

    const val CRYPTO_WALLET = "0x0E9ECfe7eE9E0e2FEf6462A39e9B7a3e271BE7E9"
    const val CRYPTO_NETWORK = "Polygon / Base / BNB Chain"
}