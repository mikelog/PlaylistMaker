package com.example.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

fun TrackDto.toTrack(): Track? {
    val trackId = trackId ?: return null
    val trackName = trackName ?: return null
    val artistName = artistName ?: return null
    val trackTime = trackTimeMillis?.let {
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(it)
    } ?: "00:00"
    val collectionName = collectionName ?: return null
    val releaseDate = releaseDate ?: return null
    val primaryGenreName = primaryGenreName ?: return null
    val country = country ?: return null
    val artworkUrl100 = artworkUrl100 ?: return null

    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country =  country
    )
}