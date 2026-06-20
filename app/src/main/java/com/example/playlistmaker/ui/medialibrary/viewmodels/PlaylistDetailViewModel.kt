package com.example.playlistmaker.ui.medialibrary.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.util.SingleLiveEvent
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistDetailViewModel(
    private val playlistId: Long,
    private val playlistInteractor: PlaylistInteractor,
    private val context: Context
) : ViewModel() {

    private val _playlist = MutableLiveData<Playlist>()
    val playlist: LiveData<Playlist> = _playlist

    private val _tracks = MutableLiveData<List<Track>>(emptyList())
    val tracks: LiveData<List<Track>> = _tracks

    private val _totalDuration = MutableLiveData<String>("0")
    val totalDuration: LiveData<String> = _totalDuration

    private val _shareText = SingleLiveEvent<String?>()
    val shareText: LiveData<String?> = _shareText

    private val _playlistDeleted = SingleLiveEvent<Unit>()
    val playlistDeleted: LiveData<Unit> = _playlistDeleted

    init {
        loadPlaylist()
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            playlistInteractor.getPlaylistById(playlistId).collect { playlist ->
                _playlist.value = playlist
                loadTracks(playlist.trackIds)
            }
        }
    }

    private fun loadTracks(trackIds: List<Long>) {
        viewModelScope.launch {
            playlistInteractor.getTracksForPlaylist(trackIds).collect { tracks ->
                _tracks.value = tracks
                _totalDuration.value = calcDuration(tracks)
            }
        }
    }

    fun sharePlaylist() {
        val playlist = _playlist.value ?: return
        val tracks = _tracks.value ?: emptyList()
        if (tracks.isEmpty()) {
            _shareText.value = null
            return
        }
        val sb = StringBuilder()
        sb.appendLine(playlist.name)
        if (playlist.description.isNotBlank()) sb.appendLine(playlist.description)
        sb.appendLine(context.resources.getQuantityString(R.plurals.track_count, tracks.size, tracks.size))
        tracks.forEachIndexed { index, track ->
            sb.appendLine("${index + 1}. ${track.artistName} - ${track.trackName} (${track.trackTime})")
        }
        _shareText.value = sb.toString().trimEnd()
    }

    fun deletePlaylist() {
        val playlist = _playlist.value ?: return
        viewModelScope.launch {
            playlistInteractor.deletePlaylist(playlist)
            _playlistDeleted.value = Unit
        }
    }

    fun removeTrack(trackId: Long) {
        val playlist = _playlist.value ?: return
        viewModelScope.launch {
            playlistInteractor.removeTrackFromPlaylist(trackId, playlist)
            loadTracks(playlist.trackIds.filter { it != trackId })
        }
    }

    private fun calcDuration(tracks: List<Track>): String {
        val totalMs = tracks.sumOf { track ->
            try {
                SimpleDateFormat("mm:ss", Locale.getDefault()).parse(track.trackTime)?.time ?: 0L
            } catch (e: Exception) { 0L }
        }
        return SimpleDateFormat("mm", Locale.getDefault()).format(totalMs)
    }
}
