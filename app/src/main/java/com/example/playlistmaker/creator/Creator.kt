package com.example.playlistmaker.creator

import android.content.Context
import android.media.MediaPlayer
import com.example.playlistmaker.R
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.player.impl.MediaPlayerRepositoryImpl
import com.example.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.example.playlistmaker.data.repository.TrackRepositoryImpl
import com.example.playlistmaker.data.settings.ThemeRepositoryImpl
import com.example.playlistmaker.data.sharing.ExternalNavigatorImpl
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.player.MediaPlayerRepository
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.impl.SharingInteractorImpl
import com.example.playlistmaker.domain.sharing.model.EmailData
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

    // ---- Player ----
    // MediaPlayer создаётся здесь и передаётся в репозиторий через конструктор

    fun provideMediaPlayerRepository(): MediaPlayerRepository {
        return MediaPlayerRepositoryImpl(MediaPlayer())
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
        val resources = com.example.playlistmaker.App.instance.resourceProvider
        return SharingInteractorImpl(
            externalNavigator = navigator,
            shareAppLink = resources.getString(R.string.share_app_url),
            termsLink = resources.getString(R.string.user_agreement_url),
            supportEmailData = EmailData(
                email = resources.getString(R.string.support_email),
                subject = resources.getString(R.string.support_subject),
                body = resources.getString(R.string.support_body)
            )
        )
    }
}
