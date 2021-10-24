package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class Bounds(
    @SerializedName("northeast")
    val northeast: LatAndLong,
    @SerializedName("southwest")
    val southwest: LatAndLong
)
