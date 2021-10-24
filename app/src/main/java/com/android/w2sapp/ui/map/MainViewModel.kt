package com.android.w2sapp.ui.map

import android.location.Address
import android.location.Geocoder
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.w2sapp.BuildConfig
import com.android.w2sapp.data.api.MapService
import com.android.w2sapp.data.models.Resource
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(private val mapService: MapService) : ViewModel() {

    private var _directions = MutableLiveData<Resource<MainDataWrapper?>>()
    val directions: LiveData<Resource<MainDataWrapper?>>
        get() = _directions

    fun getDirections(origin: String, destination: String) {
        viewModelScope.launch {
            _directions.value = Resource.Loading()
            try {
                val result = withContext(Dispatchers.Default) {
                    mapService.getDirections(origin, destination, BuildConfig.google_maps_key)
                }
                if (result.status == "OK" && (result.routes?.get(0)?.legs?.get(0)?.steps?.size
                        ?: 0) > 0
                ) {
                    val steps = result.routes?.get(0)?.legs?.get(0)?.getDirectionPoint().orEmpty()

                    val latLngBounds = LatLngBounds(
                        LatLng(
                            result.bounds?.southwest?.lat ?: 0.0,
                            result.bounds?.southwest?.lng ?: 0.0
                        ),
                        LatLng(
                            result.bounds?.northeast?.lat ?: 0.0,
                            result.bounds?.northeast?.lat ?: 0.0
                        )
                    )
                    _directions.value = Resource.Success(MainDataWrapper(steps, latLngBounds))
                } else {
                    _directions.value = Resource.Empty()
                }
            } catch (e: Exception) {
                if (e is IOException) {
                    _directions.value = Resource.Offline()
                } else {
                    _directions.value = Resource.Error(e.message)
                }
            }
        }
    }

    suspend fun getLocationFromAddress(geocoder: Geocoder, strAddress: String?): LatLng? {
        var latLng: LatLng? = null
        withContext(Dispatchers.Default) {
            val address: List<Address>?
            var ltlg: LatLng? = null
            try {
                address = geocoder.getFromLocationName(strAddress, 5)
                if (address == null) {
                    latLng = null
                }
                val location: Address = address[0]
                location.latitude
                location.longitude
                ltlg = LatLng(location.latitude, location.longitude)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            latLng = ltlg
        }
        return latLng
    }

}