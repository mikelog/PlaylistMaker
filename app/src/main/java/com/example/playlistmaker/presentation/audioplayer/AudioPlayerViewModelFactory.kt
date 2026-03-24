package com.example.playlistmaker.presentation.audioplayer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.player.MediaPlayerInteractor

class AudioPlayerViewModelFactory(
    private val track: Track,
    private val playerInteractor: MediaPlayerInteractor
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == AudioPlayerViewModel::class.java)
        return AudioPlayerViewModel(track, playerInteractor) as T
    }
}
