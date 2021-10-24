package com.android.w2sapp.ui.map

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds

data class MainDataWrapper(
    val steps: List<LatLng>?,
    val bound: LatLngBounds?
)
