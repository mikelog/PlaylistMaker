package com.example.playlistmaker.presentation.audioplayer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.player.MediaPlayerInteractor
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerViewModel(
    private val track: Track,
    private val playerInteractor: MediaPlayerInteractor
) : ViewModel() {

    data class PlayerScreenState(
        val isPlaying: Boolean = false,
        val isPlayEnabled: Boolean = false,
        val progress: String = "00:00"
    )

    private val _screenState = MutableLiveData(PlayerScreenState())
    val screenState: LiveData<PlayerScreenState> = _screenState

    private var progressJob: Job? = null

    init {
        preparePlayer()
    }

    fun onPlayPauseClicked() {
        if (playerInteractor.isPlaying()) pause() else play()
    }

    fun onActivityPaused() {
        pause()
    }

    private fun preparePlayer() {
        val url = track.previewUrl
        if (url.isBlank()) {
            _screenState.value = PlayerScreenState(isPlayEnabled = false)
            return
        }
        playerInteractor.prepare(
            url = url,
            onPrepared = {
                _screenState.value = _screenState.value?.copy(isPlayEnabled = true)
            },
            onCompletion = {
                stopProgressUpdates()
                _screenState.value = PlayerScreenState(
                    isPlaying = false,
                    isPlayEnabled = true,
                    progress = "00:00"
                )
            }
        )
    }

    private fun play() {
        playerInteractor.play()
        _screenState.value = _screenState.value?.copy(isPlaying = true)
        startProgressUpdates()
    }

    private fun pause() {
        playerInteractor.pause()
        stopProgressUpdates()
        _screenState.value = _screenState.value?.copy(isPlaying = false)
    }

    private fun startProgressUpdates() {
        progressJob?.cancel()
        progressJob = viewModelScope.launch {
            while (isActive) {
                _screenState.value = _screenState.value?.copy(
                    progress = formatTime(playerInteractor.getCurrentPosition())
                )
                delay(300L)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun formatTime(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(ms)

    override fun onCleared() {
        super.onCleared()
        playerInteractor.release()
    }
}
