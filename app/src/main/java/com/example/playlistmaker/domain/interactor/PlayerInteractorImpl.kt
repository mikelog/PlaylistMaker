package com.example.playlistmaker.domain.interactor

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper

class PlayerInteractorImpl : PlayerInteractor {

    override var state: PlayerInteractor.State = PlayerInteractor.State.DEFAULT
        private set

    private var mediaPlayer: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())

    private var onStateChange: ((PlayerInteractor.State) -> Unit)? = null
    private var onProgressUpdate: ((Int) -> Unit)? = null

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (state == PlayerInteractor.State.PLAYING) {
                mediaPlayer?.currentPosition?.let { onProgressUpdate?.invoke(it) }
                handler.postDelayed(this, 300L)
            }
        }
    }

    override fun setOnStateChangeListener(listener: (PlayerInteractor.State) -> Unit) {
        onStateChange = listener
    }

    override fun setOnProgressUpdateListener(listener: (Int) -> Unit) {
        onProgressUpdate = listener
    }

    override fun prepare(url: String?) {
        if (url.isNullOrBlank()) {
            state = PlayerInteractor.State.DEFAULT
            onStateChange?.invoke(state)
            return
        }
        mediaPlayer = MediaPlayer().apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener {
                state = PlayerInteractor.State.PREPARED
                onStateChange?.invoke(state)
            }
            setOnCompletionListener {
                state = PlayerInteractor.State.PREPARED
                onStateChange?.invoke(state)
                handler.removeCallbacks(updateRunnable)
            }
        }
    }

    override fun play() {
        if (state == PlayerInteractor.State.PREPARED || state == PlayerInteractor.State.PAUSED) {
            mediaPlayer?.start()
            state = PlayerInteractor.State.PLAYING
            onStateChange?.invoke(state)
            handler.post(updateRunnable)
        }
    }

    override fun pause() {
        if (state == PlayerInteractor.State.PLAYING) {
            mediaPlayer?.pause()
            state = PlayerInteractor.State.PAUSED
            onStateChange?.invoke(state)
            handler.removeCallbacks(updateRunnable)
        }
    }

    override fun release() {
        handler.removeCallbacks(updateRunnable)
        mediaPlayer?.release()
        mediaPlayer = null
        state = PlayerInteractor.State.DEFAULT
        onStateChange?.invoke(state)
    }
}
