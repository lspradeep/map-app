package com.android.w2sapp.data.models.map

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.google.maps.android.PolyUtil;


data class RoutePolyline(
    @SerializedName("points")
    val rawPointList: String
){
    fun getPointList(): List<LatLng?>? {
        return PolyUtil.decode(rawPointList)
    }
}
