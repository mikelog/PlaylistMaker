package com.example.playlistmaker.di

import android.content.Context
import com.example.playlistmaker.ui.medialibrary.viewmodels.EditPlaylistViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.FavouriteTracksViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.MediaLibraryViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.NewPlaylistViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistsViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaLibraryModule = module {
    viewModel { MediaLibraryViewModel() }
    viewModel { FavouriteTracksViewModel(get()) }
    viewModel { PlaylistsViewModel(get()) }
    viewModel { NewPlaylistViewModel(get(), androidContext()) }
    viewModel { (playlistId: Long) -> EditPlaylistViewModel(playlistId, get(), androidContext()) }
}
