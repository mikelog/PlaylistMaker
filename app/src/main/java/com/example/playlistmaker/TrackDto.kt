package com.example.playlistmaker

data class TrackDto(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?,
    val collectionName: String?,      // альбом
    val releaseDate: String?,         // дата релиза
    val primaryGenreName: String,     // жанр
    val country: String?,
    val previewUrl: String?
)

