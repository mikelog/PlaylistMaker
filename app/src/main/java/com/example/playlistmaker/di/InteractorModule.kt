package com.example.playlistmaker.di

import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractor
import com.example.playlistmaker.domain.interactor.SearchHistoryInteractorImpl
import com.example.playlistmaker.domain.interactor.SearchTracksInteractor
import com.example.playlistmaker.domain.interactor.SearchTracksInteractorImpl
import com.example.playlistmaker.domain.player.MediaPlayerInteractor
import com.example.playlistmaker.domain.settings.SettingsInteractor
import com.example.playlistmaker.domain.settings.impl.SettingsInteractorImpl
import com.example.playlistmaker.domain.sharing.SharingInteractor
import com.example.playlistmaker.domain.sharing.impl.SharingInteractorImpl
import com.example.playlistmaker.domain.sharing.model.EmailData
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val interactorModule = module {

    single<SearchTracksInteractor> {
        SearchTracksInteractorImpl(get())
    }

    single<SearchHistoryInteractor> {
        SearchHistoryInteractorImpl(get())
    }

    single<SettingsInteractor> {
        SettingsInteractorImpl(get())
    }

    factory<MediaPlayerInteractor> {
        MediaPlayerInteractor(get())
    }

    single<SharingInteractor> {
        val context = androidContext()
        SharingInteractorImpl(
            externalNavigator = get(),
            shareAppLink = context.getString(R.string.share_app_url),
            termsLink = context.getString(R.string.user_agreement_url),
            supportEmailData = EmailData(
                email = context.getString(R.string.support_email),
                subject = context.getString(R.string.support_subject),
                body = context.getString(R.string.support_body)
            )
        )
    }
}