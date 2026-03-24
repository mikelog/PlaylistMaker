package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.creator.Creator

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        Creator.initWith(this)

        // Восстанавливаем тему при запуске приложения
        val settingsInteractor = Creator.provideSettingsInteractor(this)
        settingsInteractor.updateThemeSetting(settingsInteractor.getThemeSettings())
    }
}
