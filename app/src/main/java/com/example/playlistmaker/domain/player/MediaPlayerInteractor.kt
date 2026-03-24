package com.example.playlistmaker.domain.player

class MediaPlayerInteractor(
    private val repository: MediaPlayerRepository
) {
    fun prepare(url: String, onPrepared: () -> Unit, onCompletion: () -> Unit) {
        repository.prepare(url, onPrepared, onCompletion)
    }

    fun play() {
        repository.play()
    }

    fun pause() {
        repository.pause()
    }

    fun release() {
        repository.release()
    }

    fun isPlaying(): Boolean = repository.isPlaying()

    fun getCurrentPosition(): Int = repository.getCurrentPosition()
}
