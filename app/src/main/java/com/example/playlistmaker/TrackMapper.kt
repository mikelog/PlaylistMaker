package com.example.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

fun TrackDto.toTrack(): Track {
    val time = trackTimeMillis?.let {
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(it)
    } ?: "00:00"

    return Track(
        trackName = trackName ?: "Unknown track",
        artistName = artistName ?: "Unknown artist",
        trackTime = time,
        artworkUrl100 = artworkUrl100
    )
}