package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class LatAndLong(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)
