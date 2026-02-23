package com.example.playlistmaker.domain.interactor

interface PlayerInteractor {
    enum class State { DEFAULT, PREPARED, PLAYING, PAUSED }

    val state: State
    fun prepare(url: String?)
    fun play()
    fun pause()
    fun release()
    fun setOnStateChangeListener(listener: (State) -> Unit)
    fun setOnProgressUpdateListener(listener: (Int) -> Unit)
}
