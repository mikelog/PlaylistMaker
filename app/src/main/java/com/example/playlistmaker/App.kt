package com.example.playlistmaker

import android.app.Application
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.util.ResourceProvider

class App : Application() {

    companion object {
        lateinit var instance: App
            private set
    }

    lateinit var resourceProvider: ResourceProvider
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        resourceProvider = ResourceProvider(this)

        // Восстанавливаем тему при запуске приложения
        val settingsInteractor = Creator.provideSettingsInteractor(this)
        settingsInteractor.updateThemeSetting(settingsInteractor.getThemeSettings())
    }
}
