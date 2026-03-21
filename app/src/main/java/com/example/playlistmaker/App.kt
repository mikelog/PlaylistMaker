package com.example.playlistmaker

import android.app.Application
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate

const val STORED_PREFERENCES = "SETTINGS"
const val THEME_SWITCHER_VAL = "darkTheme"

class App : Application() {
    private lateinit var sharedPrefs: SharedPreferences
    var darkTheme = false
        private set

    override fun onCreate() {
        super.onCreate()
        sharedPrefs = getSharedPreferences(STORED_PREFERENCES, MODE_PRIVATE)
        darkTheme = sharedPrefs.getBoolean(THEME_SWITCHER_VAL, false)
        switchTheme(darkTheme)
    }

    fun switchTheme(darkThemeEnabled: Boolean) {
        darkTheme = darkThemeEnabled
        AppCompatDelegate.setDefaultNightMode(
            if (darkThemeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        sharedPrefs.edit()
            .putBoolean(THEME_SWITCHER_VAL, darkThemeEnabled)
            .apply()
    }
}