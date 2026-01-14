package com.example.playlistmaker

import java.text.SimpleDateFormat
import java.util.Locale

fun TrackDto.toTrack(): Track? {
    val name = trackName ?: return null
    val artist = artistName ?: return null
    val time = trackTimeMillis?.let {
        SimpleDateFormat("mm:ss", Locale.getDefault()).format(it)
    } ?: "00:00"

    return Track(
        trackName = name,
        artistName = artist,
        trackTime = time,
        artworkUrl100 = artworkUrl100
    )
}