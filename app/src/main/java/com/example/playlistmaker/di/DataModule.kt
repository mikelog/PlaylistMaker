package com.example.playlistmaker.di

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.player.impl.MediaPlayerRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.settings.ThemeRepositoryImpl
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

val dataModule = module {

    // Network
    single<ItunesApi> {
        Retrofit.Builder()
            .baseUrl("https://itunes.apple.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ItunesApi::class.java)
    }

    // SharedPreferences
    single(named("history_prefs")) {
        androidContext()
            .getSharedPreferences("playlist_prefs", Context.MODE_PRIVATE)
    }

    single(named("settings_prefs")) {
        androidContext()
            .getSharedPreferences("SETTINGS", Context.MODE_PRIVATE)
    }

    // Gson
    factory { Gson() }

    // Repositories
    single<TrackRepository> {
        TrackRepositoryImpl(get())
    }

    single<SearchHistoryRepository> {
        SearchHistoryRepositoryImpl(get(named("history_prefs")), get())
    }

    single<SettingsRepository> {
        ThemeRepositoryImpl(get(named("settings_prefs")))
    }

    factory<MediaPlayerRepository> {
        MediaPlayerRepositoryImpl()
    }

    // External Navigator
    single {
        ExternalNavigatorImpl(androidContext())
    }
}
