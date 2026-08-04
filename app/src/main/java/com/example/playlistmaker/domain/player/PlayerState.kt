package com.example.playlistmaker.domain.player

data class PlayerState(
    val isPlaying: Boolean = false,
    val isPlayEnabled: Boolean = false,
    val progressMs: Int = 0
)
