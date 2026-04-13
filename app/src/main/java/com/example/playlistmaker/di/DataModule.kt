package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.player.impl.MediaPlayerRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.settings.ThemeRepositoryImpl
import com.example.playlistmaker.data.sharing.ExternalNavigator
import com.example.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.example.playlistmaker.domain.player.MediaPlayerRepository
import com.example.playlistmaker.domain.repository.SearchHistoryRepository
import com.example.playlistmaker.domain.repository.TrackRepository
import com.example.playlistmaker.domain.settings.SettingsRepository
import com.google.gson.Gson
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

private const val ITUNES_BASE_URL = "https://itunes.apple.com"
private const val SHARED_PREFS_HISTORY = "history_prefs"
private const val SHARED_PREFS_SETTINGS = "settings_prefs"
private const val SHARED_PREFS_HISTORY_NAME = "playlist_prefs"
private const val SHARED_PREFS_SETTINGS_NAME = "SETTINGS"

val dataModule = module {

    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl(ITUNES_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    single(named(SHARED_PREFS_HISTORY)) {
        androidContext()
            .getSharedPreferences(SHARED_PREFS_HISTORY_NAME, Context.MODE_PRIVATE)
    }

    single(named(SHARED_PREFS_SETTINGS)) {
        androidContext()
            .getSharedPreferences(SHARED_PREFS_SETTINGS_NAME, Context.MODE_PRIVATE)
    }

    factory { Gson() }

    single<TrackRepository> {
        TrackRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(named(SHARED_PREFS_HISTORY)), get())
    }

    single<SettingsRepository> {
        ThemeRepositoryImpl(get(named(SHARED_PREFS_SETTINGS)))
    }

    factory<MediaPlayerRepository> {
        MediaPlayerRepositoryImpl()
    }

    // External Navigator
    single<ExternalNavigator> {
        ExternalNavigatorImpl(androidContext())
    }
}
