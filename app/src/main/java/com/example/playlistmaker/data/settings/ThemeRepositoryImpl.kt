package com.example.playlistmaker.data.settings

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatDelegate
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.example.playlistmaker.domain.settings.model.ThemeSettings

class ThemeRepositoryImpl(
    private val sharedPreferences: SharedPreferences
) : SettingsRepository {

    override fun getThemeSettings(): ThemeSettings {
        return ThemeSettings(
            isDarkTheme = sharedPreferences.getBoolean(KEY_DARK_THEME, false)
        )
    }

    override fun updateThemeSetting(settings: ThemeSettings) {
        sharedPreferences.edit()
            .putBoolean(KEY_DARK_THEME, settings.isDarkTheme)
            .apply()
        AppCompatDelegate.setDefaultNightMode(
            if (settings.isDarkTheme) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }

    companion object {
        private const val KEY_DARK_THEME = "darkTheme"
    }
}
