package com.example.playlistmaker.di

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

    single<MediaPlayerInteractor> {
        MediaPlayerInteractor(get())
    }

    single<SharingInteractor> {
        SharingInteractorImpl(
            externalNavigator = get(),
            shareAppLink = androidContext().getString(com.example.playlistmaker.R.string.share_app_url),
            termsLink = androidContext().getString(com.example.playlistmaker.R.string.user_agreement_url),
            supportEmailData = EmailData(
                email = androidContext().getString(com.example.playlistmaker.R.string.support_email),
                subject = androidContext().getString(com.example.playlistmaker.R.string.support_subject),
                body = androidContext().getString(com.example.playlistmaker.R.string.support_body)
            )
        )
    }
}
