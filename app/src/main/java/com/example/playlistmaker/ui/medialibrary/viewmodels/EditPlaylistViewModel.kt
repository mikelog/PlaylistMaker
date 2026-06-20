package com.example.playlistmaker.ui.medialibrary.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.launch

class EditPlaylistViewModel(
    private val playlistId: Long,
    playlistInteractor: PlaylistInteractor,
    context: Context
) : NewPlaylistViewModel(playlistInteractor, context) {

    private val _editingPlaylist = MutableLiveData<Playlist>()
    val editingPlaylist: LiveData<Playlist> = _editingPlaylist

    init {
        viewModelScope.launch {
            playlistInteractor.getPlaylistById(playlistId).take(1).collect {
                _editingPlaylist.value = it
            }
        }
    }

    fun saveEditedPlaylist(name: String, description: String) {
        val existing = _editingPlaylist.value ?: return
        viewModelScope.launch {
            val coverPath = if (_coverUri.value != null) {
                saveCoverToPrivateStorage(_coverUri.value!!)
            } else {
                existing.coverPath
            }
            val updated = existing.copy(name = name, description = description, coverPath = coverPath)
            playlistInteractor.updatePlaylist(updated)
            _playlistCreated.value = name
        }
    }

    fun setInitialCoverUri(uri: Uri) {
        _coverUri.value = uri
    }
}
