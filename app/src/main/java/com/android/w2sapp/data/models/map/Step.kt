package com.android.w2sapp.data.models.map

import com.google.gson.annotations.SerializedName

data class Step(
    @SerializedName("distance")
    val distance: Distance,
    @SerializedName("duration")
    val duration: Duration,
    @SerializedName("end_location")
    val endLocation: LatAndLong,
    @SerializedName("html_instructions")
    val htmlInstructions: String,
    @SerializedName("start_location")
    val startLocation: LatAndLong,
    @SerializedName("travel_mode")
    val travelMode: String,
    @SerializedName("polyline")
    val polyline: RoutePolyline,
    @SerializedName("steps")
    val steps: List<Step>?
){
    fun isContainStepList(): Boolean {
        return steps != null && steps.isNotEmpty()
    }
}
