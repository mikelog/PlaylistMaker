package com.example.playlistmaker.creator

import android.content.Context
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.settings.ThemeRepositoryImpl
import com.example.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.impl.SharingInteractorImpl
import com.google.gson.Gson
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object Creator {

    private const val PREFS_SETTINGS = "SETTINGS"
    private const val BASE_URL = "https://itunes.apple.com"
    private const val PREFS_HISTORY = "playlist_prefs"

    // ---- Network ----

    private fun provideItunesApi(): ItunesApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    // ---- Search ----

    fun provideSearchTracksInteractor(): SearchTracksInteractor {
        val repository = TrackRepositoryImpl(provideItunesApi())
        return SearchTracksInteractorImpl(repository)
    }

    fun provideSearchHistoryInteractor(context: Context): SearchHistoryInteractor {
        val prefs = context.getSharedPreferences(PREFS_HISTORY, Context.MODE_PRIVATE)
        val repository = SearchHistoryRepositoryImpl(prefs, Gson())
        return SearchHistoryInteractorImpl(repository)
    }

    // ---- Settings ----

    fun provideSettingsInteractor(context: Context): SettingsInteractor {
        val prefs = context.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE)
        val repository = ThemeRepositoryImpl(prefs)
        return SettingsInteractorImpl(repository)
    }

    // ---- Sharing ----

    fun provideSharingInteractor(context: Context): SharingInteractor {
        val navigator = ExternalNavigatorImpl(context.applicationContext)
        return SharingInteractorImpl(navigator)
    }
}
