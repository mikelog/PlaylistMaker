package com.example.playlistmaker

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

private const val PREF_NAME = "app_settings"
private const val KEY_THEME = "theme_mode"

const val MODE_SYSTEM = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
const val MODE_LIGHT = AppCompatDelegate.MODE_NIGHT_NO
const val MODE_DARK = AppCompatDelegate.MODE_NIGHT_YES

fun saveTheme(context: Context, mode: Int) {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    prefs.edit().putInt(KEY_THEME, mode).apply()
}

fun loadTheme(context: Context): Int {
    val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    return prefs.getInt(KEY_THEME, MODE_SYSTEM)
}