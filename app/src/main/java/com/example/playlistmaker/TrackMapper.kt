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

    return Track(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100
    )
}