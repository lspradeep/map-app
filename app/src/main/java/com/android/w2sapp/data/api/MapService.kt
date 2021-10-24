package com.android.w2sapp.data.api

import com.android.w2sapp.data.models.map.DirectionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface MapService {
    @GET("directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String, @Query("destination") destination: String,
        @Query("key") key: String
    ): DirectionResponse
}