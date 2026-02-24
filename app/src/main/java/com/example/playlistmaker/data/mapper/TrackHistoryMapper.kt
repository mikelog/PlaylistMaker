package com.example.playlistmaker.data.mapper

import com.example.playlistmaker.data.dto.TrackHistoryDto
import com.example.playlistmaker.domain.models.Track

fun Track.toHistoryDto(): TrackHistoryDto {
    return TrackHistoryDto(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun TrackHistoryDto.toTrack(): Track? {
    return Track(
        trackId = trackId ?: return null,
        trackName = trackName ?: return null,
        artistName = artistName ?: return null,
        trackTime = trackTime ?: return null,
        artworkUrl100 = artworkUrl100 ?: return null,
        collectionName = collectionName ?: return null,
        releaseDate = releaseDate ?: return null,
        primaryGenreName = primaryGenreName ?: return null,
        country = country ?: return null,
        previewUrl = previewUrl ?: return null
    )
}
