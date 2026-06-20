package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.presentation.audioplayer.AudioPlayerViewModel
import com.example.playlistmaker.presentation.search.SearchViewModel
import com.example.playlistmaker.presentation.settings.SettingsViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistDetailViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {

    viewModel {
        SearchViewModel(
            searchInteractor = get(),
            historyInteractor = get()
        )
    }

    viewModel { (track: Track) ->
        AudioPlayerViewModel(
            track = track,
            playerInteractor = get(),
            favouriteInteractor = get(),
            playlistInteractor = get()
        )
    }

    viewModel {
        SettingsViewModel(
            settingsInteractor = get(),
            sharingInteractor = get()
        )
    }

    viewModel { (playlistId: Long) ->
        PlaylistDetailViewModel(
            playlistId = playlistId,
            playlistInteractor = get(),
            context = get<Context>()
        )
    }
}
