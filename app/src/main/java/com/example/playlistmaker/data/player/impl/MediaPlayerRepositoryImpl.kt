package com.example.playlistmaker.data.player.impl

import android.media.MediaPlayer
import com.example.playlistmaker.domain.player.MediaPlayerRepository

class MediaPlayerRepositoryImpl(
    private val mediaPlayer: MediaPlayer
) : MediaPlayerRepository {

    override fun prepare(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        mediaPlayer.apply {
            setDataSource(url)
            prepareAsync()
            setOnPreparedListener { onPrepared() }
            setOnCompletionListener { onCompletion() }
        }
    }

    override fun play() {
        mediaPlayer.start()
    }

    override fun pause() {
        mediaPlayer.pause()
    }

    override fun release() {
        mediaPlayer.release()
    }

    override fun isPlaying(): Boolean = mediaPlayer.isPlaying

    override fun getCurrentPosition(): Int = mediaPlayer.currentPosition
}
