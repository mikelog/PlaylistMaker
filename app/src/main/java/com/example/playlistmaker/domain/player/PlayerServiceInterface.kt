package com.example.playlistmaker.domain.player

import kotlinx.coroutines.flow.StateFlow

interface PlayerServiceInterface {
    fun getPlayerState(): StateFlow<PlayerState>
    fun play()
    fun pause()
    fun startForegroundNotification()
    fun stopForegroundNotification()
}
