package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName
import com.android.w2sapp.utils.DirectionConverter

import com.google.android.gms.maps.model.LatLng
data class Leg(
    @SerializedName("distance")
    val distance: Distance,
    @SerializedName("duration")
    val duration: Duration,
    @SerializedName("end_address")
    val endAddress: String,
    @SerializedName("end_location")
    val endLocation: LatAndLong,
    @SerializedName("start_address")
    val startAddress: String,
    @SerializedName("start_location")
    val startLocation: LatAndLong,
    @SerializedName("steps")
    val steps: List<Step>?
){
    fun getDirectionPoint(): List<LatLng>? {
        return DirectionConverter.getDirectionPoint(steps)
    }
}
