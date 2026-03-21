package com.example.playlistmaker.presentation.audioplayer

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.playlistmaker.domain.models.Track
import java.text.SimpleDateFormat
import java.util.Locale

class AudioPlayerViewModel(private val track: Track) : ViewModel() {

    // ---- Состояние плеера
    data class PlayerScreenState(
        val isPlaying: Boolean = false,
        val isPlayEnabled: Boolean = false,
        val progress: String = "00:00"
    )

    private val _screenState = MutableLiveData(PlayerScreenState())
    val screenState: LiveData<PlayerScreenState> = _screenState

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private val updateRunnable = object : Runnable {
        override fun run() {
            val player = mediaPlayer ?: return
            if (player.isPlaying) {
                _screenState.value = _screenState.value?.copy(
                    progress = formatTime(player.currentPosition)
                )
                handler.postDelayed(this, 300L)
            }
        }
    }

    init {
        preparePlayer()
    }

    fun onPlayPauseClicked() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            pause()
        } else {
            play()
        }
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

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()

            setOnPreparedListener {
                _screenState.value = _screenState.value?.copy(isPlayEnabled = true)
            }

            setOnCompletionListener {
                handler.removeCallbacks(updateRunnable)
                _screenState.value = PlayerScreenState(
                    isPlaying = false,
                    isPlayEnabled = true,
                    progress = "00:00"
                )
            }
        }
    }

    private fun play() {
        mediaPlayer?.start()
        _screenState.value = _screenState.value?.copy(isPlaying = true)
        handler.post(updateRunnable)
    }

    private fun pause() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
        }
        handler.removeCallbacks(updateRunnable)
        _screenState.value = _screenState.value?.copy(isPlaying = false)
    }

    private fun formatTime(ms: Int): String =
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(ms)

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
