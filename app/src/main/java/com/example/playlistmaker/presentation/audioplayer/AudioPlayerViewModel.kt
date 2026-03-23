package com.example.playlistmaker.presentation.audioplayer

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.player.MediaPlayerRepository
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerViewModel(
    private val track: Track,
    private val playerRepository: MediaPlayerRepository
) : ViewModel() {

    data class PlayerScreenState(
        val isPlaying: Boolean = false,
        val isPlayEnabled: Boolean = false,
        val progress: String = "00:00"
    )

    private val _screenState = MutableLiveData(PlayerScreenState())
    val screenState: LiveData<PlayerScreenState> = _screenState

    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (playerRepository.isPlaying()) {
                _screenState.value = _screenState.value?.copy(
                    progress = formatTime(playerRepository.getCurrentPosition())
                )
                handler.postDelayed(this, 300L)
            }
        }
    }

    init {
        preparePlayer()
    }

    fun onPlayPauseClicked() {
        if (playerRepository.isPlaying()) pause() else play()
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
        playerRepository.prepare(
            url = url,
            onPrepared = {
                _screenState.value = _screenState.value?.copy(isPlayEnabled = true)
            },
            onCompletion = {
                handler.removeCallbacks(updateRunnable)
                _screenState.value = PlayerScreenState(
                    isPlaying = false,
                    isPlayEnabled = true,
                    progress = "00:00"
                )
            }
        )
    }

    private fun play() {
        playerRepository.play()
        _screenState.value = _screenState.value?.copy(isPlaying = true)
        handler.post(updateRunnable)
    }

    private fun pause() {
        playerRepository.pause()
        handler.removeCallbacks(updateRunnable)
        _screenState.value = _screenState.value?.copy(isPlaying = false)
    }

    private fun formatTime(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(ms)

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(updateRunnable)
        playerRepository.release()
    }
}
