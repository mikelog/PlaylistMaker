package com.example.playlistmaker.presentation.audioplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.interactor.FavouriteTracksInteractor
import com.example.playlistmaker.domain.interactor.PlaylistInteractor
import com.example.playlistmaker.domain.models.Playlist
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.player.PlayerServiceInterface
import com.example.playlistmaker.util.SingleLiveEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

sealed class AddToPlaylistResult {
    data class Success(val playlistName: String) : AddToPlaylistResult()
    data class AlreadyAdded(val playlistName: String) : AddToPlaylistResult()
}

class AudioPlayerViewModel(
    private val track: Track,
    private val favouriteInteractor: FavouriteTracksInteractor,
    private val playlistInteractor: PlaylistInteractor
) : ViewModel() {

    data class PlayerScreenState(
        val isPlaying: Boolean = false,
        val isPlayEnabled: Boolean = false,
        val progress: String = "00:00"
    )

    private val _screenState = MutableLiveData(PlayerScreenState())
    val screenState: LiveData<PlayerScreenState> = _screenState

    private val _isFavorite = MutableLiveData(track.isFavorite)
    val isFavorite: LiveData<Boolean> = _isFavorite

    private val _playlists = MutableLiveData<List<Playlist>>(emptyList())
    val playlists: LiveData<List<Playlist>> = _playlists

    private val _addToPlaylistResult = SingleLiveEvent<AddToPlaylistResult>()
    val addToPlaylistResult: LiveData<AddToPlaylistResult> = _addToPlaylistResult

    private var playerService: PlayerServiceInterface? = null
    private var stateObserverJob: Job? = null

    init {
        checkIfFavorite()
    }

    fun onServiceConnected(service: PlayerServiceInterface) {
        playerService = service
        stateObserverJob?.cancel()
        stateObserverJob = viewModelScope.launch {
            service.getPlayerState().collect { state ->
                _screenState.value = PlayerScreenState(
                    isPlaying = state.isPlaying,
                    isPlayEnabled = state.isPlayEnabled,
                    progress = formatTime(state.progressMs)
                )
            }
        }
    }

    fun onServiceDisconnected() {
        stateObserverJob?.cancel()
        stateObserverJob = null
        playerService = null
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            playlistInteractor.getAllPlaylists().collect { list ->
                _playlists.postValue(list)
            }
        }
    }

    fun addTrackToPlaylist(playlist: Playlist) {
        if (playlist.trackIds.contains(track.trackId)) {
            _addToPlaylistResult.value = AddToPlaylistResult.AlreadyAdded(playlist.name)
        } else {
            viewModelScope.launch {
                playlistInteractor.addTrackToPlaylist(track, playlist)
                _addToPlaylistResult.postValue(AddToPlaylistResult.Success(playlist.name))
            }
        }
    }

    fun onPlayPauseClicked() {
        val service = playerService ?: return
        if (service.getPlayerState().value.isPlaying) service.pause() else service.play()
    }

    fun onScreenBackgrounded() {
        val service = playerService ?: return
        if (service.getPlayerState().value.isPlaying) {
            service.startForegroundNotification()
        }
    }

    fun onScreenForegrounded() {
        playerService?.stopForegroundNotification()
    }

    fun onFavoriteClicked() {
        val currentTrack = track.copy(isFavorite = _isFavorite.value ?: false)
        viewModelScope.launch {
            if (currentTrack.isFavorite) {
                favouriteInteractor.deleteTrack(currentTrack)
            } else {
                favouriteInteractor.addTrack(currentTrack)
            }
            _isFavorite.postValue(!currentTrack.isFavorite)
        }
    }

    private fun checkIfFavorite() {
        viewModelScope.launch {
            val favoriteIds = favouriteInteractor.getAllTracks()
            favoriteIds.collect { tracks ->
                _isFavorite.postValue(tracks.any { it.trackId == track.trackId })
            }
        }
    }

    private fun formatTime(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(ms)

    override fun onCleared() {
        super.onCleared()
        stateObserverJob?.cancel()
    }
}
