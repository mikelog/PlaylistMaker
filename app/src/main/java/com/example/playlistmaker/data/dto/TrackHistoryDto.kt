package com.example.playlistmaker.data.dto

data class TrackHistoryDto(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val trackTime: String?,       // уже отформатированная строка "mm:ss"
    val artworkUrl100: String?,
    val collectionName: String?,
    val releaseDate: String?,
    val primaryGenreName: String?,
    val country: String?,
    val previewUrl: String?
)
