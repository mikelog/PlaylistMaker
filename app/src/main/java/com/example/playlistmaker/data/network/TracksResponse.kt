package com.example.playlistmaker.data.network

import com.example.playlistmaker.data.dto.TrackDto

data class TracksResponse(
    val resultCount: Int,
    val results: List<TrackDto>
)
