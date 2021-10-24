package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class Duration(
    @SerializedName("text")
    val text: String,
    @SerializedName("value")
    val value: Int

)