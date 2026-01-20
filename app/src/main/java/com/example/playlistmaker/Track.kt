package com.example.playlistmaker
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException

data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val artworkUrl100: String,
    val collectionName: String,      // альбом
    val releaseDate: String,         // дата релиза
    val primaryGenreName: String,     // жанр
    val country: String
){
    fun getCoverArtwork() = artworkUrl100.replaceAfterLast('/',"512x512bb.jpg")
    fun getReleaseYear(): String {
        return try {
            Instant.parse(releaseDate)
                .atZone(ZoneId.systemDefault())
                .year
                .toString()
        } catch (e: DateTimeParseException) {
            ""
        }
    }
}

