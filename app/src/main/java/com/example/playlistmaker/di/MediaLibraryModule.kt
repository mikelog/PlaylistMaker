package com.example.playlistmaker.di

import com.example.playlistmaker.ui.medialibrary.viewmodels.FavouriteTracksViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.MediaLibraryViewModel
import com.example.playlistmaker.ui.medialibrary.viewmodels.PlaylistsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val mediaLibraryModule = module {
    viewModel { MediaLibraryViewModel() }
    viewModel { FavouriteTracksViewModel() }
    viewModel { PlaylistsViewModel() }
}