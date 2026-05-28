package com.example.playlistmaker.domain.models
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeParseException
@Parcelize
data class Track(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String,
    val isFavorite: Boolean = false
): Parcelable {
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
