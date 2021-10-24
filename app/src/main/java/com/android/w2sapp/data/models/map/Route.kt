package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("bounds")
    val bounds: Bounds,
    @SerializedName("legs")
    val legs: List<Leg>?
)
