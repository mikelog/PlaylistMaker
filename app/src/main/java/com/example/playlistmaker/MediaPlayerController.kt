package com.example.playlistmaker

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

// MediaPlayerController.kt — реализация
class MediaPlayerController : PlayerController {

    override var state: PlayerController.State = PlayerController.State.DEFAULT
        private set

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val updateRunnable = object : Runnable {
        override fun run() {
            if (state == PlayerController.State.PLAYING) {
                mediaPlayer?.currentPosition?.let { onProgressUpdate?.invoke(it) }
                handler.postDelayed(this, 300L)
            }
        }
    }

    private var onStateChange: ((PlayerController.State) -> Unit)? = null
    private var onProgressUpdate: ((Int) -> Unit)? = null

    override fun setOnStateChangeListener(listener: (PlayerController.State) -> Unit) {
        onStateChange = listener
    }

    override fun setOnProgressUpdateListener(listener: (Int) -> Unit) {
        onProgressUpdate = listener
    }

    override fun prepare(url: String?) {
        if (url.isNullOrBlank()) {
            state = PlayerController.State.DEFAULT
            onStateChange?.invoke(state)
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                state = PlayerController.State.PREPARED
                onStateChange?.invoke(state)
            }
            setOnCompletionListener {
                state = PlayerController.State.PREPARED
                onStateChange?.invoke(state)
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun play() {
        if (state == PlayerController.State.PREPARED || state == PlayerController.State.PAUSED) {
            mediaPlayer?.start()
            state = PlayerController.State.PLAYING
            onStateChange?.invoke(state)
            handler.post(updateRunnable)
        }
    }

    override fun pause() {
        if (state == PlayerController.State.PLAYING) {
            mediaPlayer?.pause()
            state = PlayerController.State.PAUSED
            onStateChange?.invoke(state)
            handler.removeCallbacks(updateRunnable)
        }
    }

    override fun release() {
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        state = PlayerController.State.DEFAULT
        onStateChange?.invoke(state)
    }
}
