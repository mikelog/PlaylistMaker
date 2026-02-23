package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.util.Creator

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        // Восстанавливаем тему при запуске приложения
        val themeInteractor = Creator.provideThemeInteractor(this)
        themeInteractor.setDarkTheme(themeInteractor.isDarkTheme())
    }
}
