package com.example.playlistmaker.data.network

import retrofit2.http.GET
import retrofit2.http.Query

interface ItunesApi {
    @GET("/search?entity=song")
    suspend fun search(@Query("term") text: String): TracksResponse

    @GET("/lookup")
    suspend fun lookup(@Query("id") id: Long): TracksResponse
}
