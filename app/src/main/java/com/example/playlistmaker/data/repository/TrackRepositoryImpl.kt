package com.example.playlistmaker.data.repository

import com.example.playlistmaker.data.mapper.toTrack
import com.example.playlistmaker.data.network.ItunesApi
import com.example.playlistmaker.data.network.TracksResponse
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.repository.TrackRepository
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TrackRepositoryImpl(
    private val api: ItunesApi
) : TrackRepository {

    override fun search(query: String, callback: (List<Track>?, isNetworkError: Boolean) -> Unit) {
        api.search(query).enqueue(object : Callback<TracksResponse> {
            override fun onResponse(call: Call<TracksResponse>, response: Response<TracksResponse>) {
                if (response.isSuccessful) {
                    val tracks = response.body()?.results
                        ?.mapNotNull { it.toTrack() }
                        ?: emptyList()
                    callback(tracks, false)
                } else {
                    callback(null, true)
                }
            }

            override fun onFailure(call: Call<TracksResponse>, t: Throwable) {
                callback(null, true)
            }
        })
    }
}
