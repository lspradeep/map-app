package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class DirectionResponse(
    @SerializedName("bounds")
    var bounds : Bounds?,
    @SerializedName("routes")
    val routes: List<Route>?,
    @SerializedName("status")
    val status: String
)
