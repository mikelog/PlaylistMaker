package com.example.playlistmaker.ui.medialibrary.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }
}