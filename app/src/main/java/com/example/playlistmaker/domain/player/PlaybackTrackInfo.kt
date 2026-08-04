package com.example.playlistmaker.domain.player

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PlaybackTrackInfo(
    val previewUrl: String,
    val trackName: String,
    val artistName: String
) : Parcelable
